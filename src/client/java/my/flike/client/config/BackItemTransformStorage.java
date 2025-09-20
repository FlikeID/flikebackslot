package my.flike.client.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import my.flike.client.render.BackItemTransform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Сохранение/загрузка backItemTransforms в config/flike/backslot.json
 */
public final class BackItemTransformStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("flike");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("backslot.json");

    private BackItemTransformStorage() {}

    // Вспомогательный POJO для сериализации
    public static final class BackItemTransformData {
        public float[] translation = new float[]{0f, 0f, 0f};
        public float[] rotation = new float[]{0f, 0f, 0f};
        public float[] scale = new float[]{1f, 1f, 1f};
        public String transformMode = "fixed";
        public boolean enabled = true;
    }

    // Сохранить map<Item, BackItemTransform> в файл (атомарно)
    public static void saveBackItemTransforms(Map<Identifier, BackItemTransform> map) {
        try {
            Files.createDirectories(CONFIG_DIR);

            // конвертируем в Map<String, BackItemTransformData>
            Map<String, BackItemTransformData> out = new HashMap<>();
            for (Map.Entry<Identifier, BackItemTransform> e : map.entrySet()) {
                BackItemTransform t = e.getValue();
                Identifier id = e.getKey();
                if (id == null) continue; // игнорируем неизвестные
                String key = id.toString();
                BackItemTransformData d = new BackItemTransformData();

                // считываем поля трансформации из BackItemTransform; тут предполагается, что
                // BackItemTransform хранит Transformation transform с полями translation/rotation/scale как Vector3f
                try {
                    // адаптируй под свою структуру BackItemTransform
                    // пример: t.transform.translation.x
                    d.translation = new float[]{
                        t.transform.translation.x,
                        t.transform.translation.y,
                        t.transform.translation.z
                    };
                    d.rotation = new float[]{
                        t.transform.rotation.x,
                        t.transform.rotation.y,
                        t.transform.rotation.z
                    };
                    d.scale = new float[]{
                        t.transform.scale.x,
                        t.transform.scale.y,
                        t.transform.scale.z
                    };
                } catch (Throwable ex) {
                    // fallback: оставить дефолтные
                }

                d.transformMode = t.transform_mode == null ? "none" : t.transform_mode.name().toLowerCase();
                d.enabled = t.enabled;
                out.put(key, d);
            }

            // записать в временный файл, затем атомарно заменить
            Path tmp = CONFIG_DIR.resolve("backslot.json.tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(out, writer);
            }
            // атомарная замена (если FS поддерживает)
            Files.move(tmp, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            try {
                // fallback: обычный move/replace
                Files.move(CONFIG_DIR.resolve("backslot.json.tmp"), CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } catch (IOException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
    }

    // Загрузить конфиг: возвращает Map<Identifier, BackItemTransformData>
    public static Map<Identifier, BackItemTransformData> loadBackItemTransforms() {
        Map<Identifier, BackItemTransformData> result = new HashMap<>();
        if (!Files.exists(CONFIG_FILE)) return result;
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            Type mapType = new TypeToken<Map<String, BackItemTransformData>>() {}.getType();
            Map<String, BackItemTransformData> in = GSON.fromJson(reader, mapType);
            if (in == null) return result;
            for (Map.Entry<String, BackItemTransformData> e : in.entrySet()) {
                String key = e.getKey();
                BackItemTransformData d = e.getValue();
                Identifier id = Identifier.tryParse(key);
                if (id == null) continue;
                result.put(id, d);
            }
        } catch (IOException | JsonSyntaxException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
        return result;
    }

    // Удобная функция: загрузить и применить в кэш (пример)
    public static void applyLoadedToCache(Map<Identifier, BackItemTransformData> loaded, Map<Identifier, BackItemTransform> cache) {
        for (Map.Entry<Identifier, BackItemTransformData> e : loaded.entrySet()) {
            Identifier id = e.getKey();
            BackItemTransformData d = e.getValue();
            if (id == null) continue;
            // создать BackItemTransform из данных
            BackItemTransform t = new BackItemTransform(); // адаптируй конструктор под свои поля
            try {
                // заполняем поля transform и mode; адаптируй под структуру
                t.transform = new net.minecraft.client.render.model.json.Transformation(
                    new org.joml.Vector3f(d.rotation[0], d.rotation[1], d.rotation[2]),
                    new org.joml.Vector3f(d.translation[0], d.translation[1], d.translation[2]),
                    new org.joml.Vector3f(d.scale[0], d.scale[1], d.scale[2])
                );
            } catch (Throwable ex) {
                // ignore
            }
            try {
                t.transform_mode = d.transformMode == null ? t.transform_mode : net.minecraft.client.render.model.json.ModelTransformationMode.valueOf(d.transformMode.toUpperCase());
            } catch (IllegalArgumentException iae) {
                // ignore invalid mode
            }
            t.enabled = d.enabled;
            cache.put(id, t);
        }
    }
}
