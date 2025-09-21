package my.flike.client.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import my.flike.client.render.BackItemTransform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Сохранение/загрузка backItemTransforms в config/flike/backslot.json
 * <p>
 * Формат на диске: список записей
 * [
 *   {
 *     "item": "mod_id:item_name",
 *     "rotation": { "x": 90.0, "y": 220.0, "z": 90.0 },
 *     "translation": { "x": 0.1, "y": 0.4, "z": 0.17 },
 *     "scale": { "x": 1.0, "y": 1.0, "z": 1.0 },
 *     "mode": "third_person_right_hand",
 *     "enabled": true
 *   },
 *   ...,
 * ]
 * </p>
 */
public final class BackItemTransformStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("flike");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("backslot.json");

    private BackItemTransformStorage() {}

    /* --- POJO for storage (uses nested objects for x,y,z) --- */

    public static final class Vec3 {
        public float x;
        public float y;
        public float z;

        public Vec3(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
    }

    public static final class BackItemTransformData {
        public Vec3 translation = new Vec3(0f, 0f, 0f);
        public Vec3 rotation = new Vec3(0f, 0f, 0f);
        public Vec3 scale = new Vec3(1f, 1f, 1f);
        public String mode = "ground";
        public boolean enabled = true;
    }

    public static final class BackItemTransformEntry {
        public String item;
        // flatten for convenience on disk: keep transform fields at the same level as item
        public Vec3 rotation;
        public Vec3 translation;
        public Vec3 scale;
        public String mode;
        public Boolean enabled;
    }

    /* --- Public API --- */

    /**
     * Сериализует и сохраняет map<ItemId, BackItemTransform> в файл (атомарно).
     * Конвертирование BackItemTransform -> BackItemTransformData производится здесь.
     */
    public static void saveBackItemTransforms(Map<Identifier, BackItemTransform> map) {
        try {
            Files.createDirectories(CONFIG_DIR);
            List<BackItemTransformEntry> out = new ArrayList<>();

            for (Map.Entry<Identifier, BackItemTransform> e : map.entrySet()) {
                Identifier id = e.getKey();
                BackItemTransform t = e.getValue();
                if (id == null || t == null) continue;

                BackItemTransformEntry entry = new BackItemTransformEntry();
                entry.item = id.toString();

                // defaults
                entry.translation = new Vec3(0f, 0f, 0f);
                entry.rotation = new Vec3(0f, 0f, 0f);
                entry.scale = new Vec3(1f, 1f, 1f);
                entry.mode = "ground";
                entry.enabled = true;

                // try to extract fields from BackItemTransform (best-effort)
                try {
                    if (t.transform != null) {
                        // Transformation uses Vector3f like fields; adapt if your BackItemTransform differs
                        Transformation tf = t.transform;
                        // Transformation stores rotation, translation, scale as javax.vecmath or Vector3f-like,
                        // here we attempt to read via reflection if necessary, but most builds expose fields
                        // In many setups Transformation has rotation/translation/scale as Vector3f-like with x,y,z float fields
                        // Try common access patterns:
                        entry.translation = tryExtractVec3FromTransformation(tf, "translation", entry.translation);
                        entry.rotation = tryExtractVec3FromTransformation(tf, "rotation", entry.rotation);
                        entry.scale = tryExtractVec3FromTransformation(tf, "scale", entry.scale);
                    }
                } catch (Throwable ignored) {
                    // leave defaults if extraction fails
                }

                try {
                    if (t.transform_mode != null) {
                        entry.mode = t.transform_mode.name().toLowerCase(Locale.ROOT);
                    }
                } catch (Throwable ignored) {}

                try {
                    entry.enabled = t.enabled;
                } catch (Throwable ignored) { entry.enabled = true; }

                out.add(entry);
            }

            Path tmp = CONFIG_DIR.resolve("backslot.json.tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(out, writer);
            }
            Files.move(tmp, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException amnse) {
            try {
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

    /**
     * Загружает конфигурацию с диска и возвращает map Identifier -> BackItemTransformData (сырые данные).
     * Не конвертирует в BackItemTransform.
     */
    public static Map<Identifier, BackItemTransformData> loadBackItemTransforms() {
        Map<Identifier, BackItemTransformData> result = new HashMap<>();
        if (!Files.exists(CONFIG_FILE)) return result;

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<BackItemTransformEntry>>() {}.getType();
            List<BackItemTransformEntry> list = GSON.fromJson(reader, listType);
            if (list == null) return result;

            for (BackItemTransformEntry e : list) {
                if (e == null || e.item == null) continue;
                Identifier id = Identifier.tryParse(e.item);
                if (id == null) continue;

                BackItemTransformData d = new BackItemTransformData();
                if (e.translation != null) d.translation = e.translation;
                if (e.rotation != null) d.rotation = e.rotation;
                if (e.scale != null) d.scale = e.scale;
                if (e.mode != null) d.mode = e.mode;
                d.enabled = e.enabled == null || e.enabled;

                result.put(id, d);
            }
        } catch (IOException | JsonSyntaxException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Загружает и конвертирует в BackItemTransform для кеша, пропуская некорректные записи.
     */
    public static Map<Identifier, BackItemTransform> loadBackItemTransformsToCache() {
        Map<Identifier, BackItemTransform> out = new HashMap<>();
        Map<Identifier, BackItemTransformData> loaded = loadBackItemTransforms();
        for (Map.Entry<Identifier, BackItemTransformData> e : loaded.entrySet()) {
            Identifier id = e.getKey();
            BackItemTransformData d = e.getValue();
            try {
                validateTransformData(d);
                BackItemTransform t = convertDataToTransform(d);
                out.put(id, t);
            } catch (Exception ex) {
                // log and skip invalid entries
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }
        }
        return out;
    }

    /**
     * Применяет уже загруженные данные (BackItemTransformData) в кеш (конвертация + валидация).
     */
    public static void applyLoadedToCache(Map<Identifier, BackItemTransformData> loaded, Map<Identifier, BackItemTransform> cache) {
        if (loaded == null || cache == null) return;
        for (Map.Entry<Identifier, BackItemTransformData> e : loaded.entrySet()) {
            Identifier id = e.getKey();
            BackItemTransformData d = e.getValue();
            if (id == null || d == null) continue;
            try {
                validateTransformData(d);
                BackItemTransform t = convertDataToTransform(d);
                cache.put(id, t);
            } catch (Exception ex) {
                //noinspection CallToPrintStackTrace
                ex.printStackTrace();
            }
        }
    }

    /**
     * Разбор JSON-строки, валидация и возвращение BackItemTransform.
     * (использует parseTransformJson -> validateTransformData -> convertDataToTransform)
     */
    public static BackItemTransform applyTransformFromJson(String json)
            throws JsonSyntaxException, IllegalArgumentException {
        BackItemTransformData d = parseTransformJson(json);
        validateTransformData(d);
        return convertDataToTransform(d);
    }

    /* --- Вспомогательные, повторно используемые методы --- */

    /**
     * Парсит строку JSON в BackItemTransformData.
     * Допускает формат с объектами rotation/translation/scale с полями x,y,z
     */
    private static BackItemTransformData parseTransformJson(String json) throws JsonSyntaxException {
        if (json == null) throw new JsonSyntaxException("json is null");
        JsonElement el = JsonParser.parseString(json);
        if (!el.isJsonObject()) throw new JsonSyntaxException("Transform JSON must be an object");
        JsonObject obj = el.getAsJsonObject();

        BackItemTransformData data = new BackItemTransformData();

        if (obj.has("rotation") && obj.get("rotation").isJsonObject()) {
            JsonObject r = obj.getAsJsonObject("rotation");
            data.rotation.x = getFloatSafe(r, "x", data.rotation.x);
            data.rotation.y = getFloatSafe(r, "y", data.rotation.y);
            data.rotation.z = getFloatSafe(r, "z", data.rotation.z);
        }

        if (obj.has("translation") && obj.get("translation").isJsonObject()) {
            JsonObject tr = obj.getAsJsonObject("translation");
            data.translation.x = getFloatSafe(tr, "x", data.translation.x);
            data.translation.y = getFloatSafe(tr, "y", data.translation.y);
            data.translation.z = getFloatSafe(tr, "z", data.translation.z);
        }

        if (obj.has("scale") && obj.get("scale").isJsonObject()) {
            JsonObject s = obj.getAsJsonObject("scale");
            data.scale.x = getFloatSafe(s, "x", data.scale.x);
            data.scale.y = getFloatSafe(s, "y", data.scale.y);
            data.scale.z = getFloatSafe(s, "z", data.scale.z);
        }

        if (obj.has("mode")) data.mode = obj.get("mode").getAsString();
        if (obj.has("enabled")) data.enabled = obj.get("enabled").getAsBoolean();

        return data;
    }

    private static float getFloatSafe(JsonObject obj, String name, float def) {
        if (!obj.has(name)) return def;
        try { return obj.get(name).getAsFloat(); }
        catch (Exception e) { throw new JsonSyntaxException("Field '" + name + "' must be a number"); }
    }

    private static void validateTransformData(BackItemTransformData d) {
        if (d == null) throw new IllegalArgumentException("data is null");

        // scale
        if (Float.isNaN(d.scale.x) || Float.isNaN(d.scale.y) || Float.isNaN(d.scale.z))
            throw new IllegalArgumentException("scale contains NaN");
        if (d.scale.x <= 0f || d.scale.y <= 0f || d.scale.z <= 0f)
            throw new IllegalArgumentException("scale components must be > 0");

        // translation reasonable bounds
        if (Float.isNaN(d.translation.x) || Float.isNaN(d.translation.y) || Float.isNaN(d.translation.z))
            throw new IllegalArgumentException("translation contains NaN");
        if (Math.abs(d.translation.x) > 10f || Math.abs(d.translation.y) > 10f || Math.abs(d.translation.z) > 10f)
            throw new IllegalArgumentException("translation too large");

        // rotation bounds sanity
        if (Float.isNaN(d.rotation.x) || Float.isNaN(d.rotation.y) || Float.isNaN(d.rotation.z))
            throw new IllegalArgumentException("rotation contains NaN");
        if (Math.abs(d.rotation.x) > 10000f || Math.abs(d.rotation.y) > 10000f || Math.abs(d.rotation.z) > 10000f)
            throw new IllegalArgumentException("rotation suspiciously large");

        // mode is optional but normalise to non-null
        if (d.mode == null || d.mode.isEmpty()) d.mode = "ground";
    }

    private static BackItemTransform convertDataToTransform(BackItemTransformData d) {
        BackItemTransform t = new BackItemTransform();
        try {
            // Transformation constructor: (rotation, translation, scale) using Vector3f
            t.transform = new Transformation(
                    new Vector3f(d.rotation.x, d.rotation.y, d.rotation.z),
                    new Vector3f(d.translation.x, d.translation.y, d.translation.z),
                    new Vector3f(d.scale.x, d.scale.y, d.scale.z)
            );
        } catch (Throwable ignored) {
            // if constructor not available or BackItemTransform differs, leave default transform
        }

        try {
            if (d.mode != null) {
                // map to ModelTransformationMode if possible
                t.transform_mode = ModelTransformationMode.valueOf(d.mode.toUpperCase(Locale.ROOT));
            }
        } catch (IllegalArgumentException iae) {
            // invalid mode — fallback to existing/default in BackItemTransform
        }

        t.enabled = d.enabled;
        return t;
    }

    /* --- Small helpers --- */

    // Try to extract transformation vector by field name from Transformation via reflection-safe access
    private static Vec3 tryExtractVec3FromTransformation(Transformation tf, String fieldName, Vec3 fallback) {
        if (tf == null) return fallback;
        try {
            // common getters/fields
            java.lang.reflect.Field f = Transformation.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            Object vec = f.get(tf);
            if (vec == null) return fallback;
            // try common types: com.mojang.math.Vector3f OR org.joml.Vector3f OR arrays
            try {
                java.lang.reflect.Field fx = vec.getClass().getField("x");
                java.lang.reflect.Field fy = vec.getClass().getField("y");
                java.lang.reflect.Field fz = vec.getClass().getField("z");
                float x = ((Number) fx.get(vec)).floatValue();
                float y = ((Number) fy.get(vec)).floatValue();
                float z = ((Number) fz.get(vec)).floatValue();
                return new Vec3(x, y, z);
            } catch (NoSuchFieldException nsf) {
                // try if it's an array-like structure
                if (vec instanceof float[] arr) {
                    if (arr.length >= 3) return new Vec3(arr[0], arr[1], arr[2]);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // fallback: try public getter methods like getRotation etc (not standard) - skip
        } catch (Throwable ignored) {}
        return fallback;
    }
}
