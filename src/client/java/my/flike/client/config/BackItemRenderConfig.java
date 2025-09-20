package my.flike.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import my.flike.client.BackslotLogic;
import my.flike.client.render.BackItemTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackItemRenderConfig {

    public enum TypeKey {
        rotation("вращение"), rot("вращение"), r("вращение"),
        position("позиция"), pos("позиция"), p("позиция"),
        scale("масштаб"), scl("масштаб"), s("масштаб"),
        render("метод рендера"), ren("метод рендера"), m("метод рендера"),
        scales("равномерный масштаб (см. ниже)");

        private final String description;
        TypeKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return valueOf(key).getDescription(); } catch (IllegalArgumentException e) { return null; }
        }

        /**
         * Возвращает канонический тип: position, rotation или scale.
         */
        public TypeKey canonical() {
            return switch (this) {
                case rot, r -> rotation;
                case pos, p -> position;
                case scl, s -> scale;
                case ren, m -> render;
                default -> this;
            };
        }

        /**
         * Преобразует строку в TypeKey, выбрасывая ошибку, если тип неизвестен.
         */
        public static TypeKey fromString(String value) throws CommandSyntaxException {
            for (TypeKey key : values()) {
                if (key.name().equalsIgnoreCase(value)) {
                    return key;
                }
            }
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(Text.literal("Wrong backslot_transform type argument: " + value)),
                    Text.literal("Wrong type: " + value + ". Available: " + String.join(", ", getList()))
            );
        }

        /**
         * Возвращает список всех допустимых строковых значений.
         */
        public static Iterable<String> getList() {
            return Arrays.stream(values())
                    .map(TypeKey::canonical)
                    .map(Enum::name)
                    .distinct()
                    .toList();
        }

    }

    public enum AxisKey {
        x("Ось X"),
        y("Ось Y"),
        z("Ось Z");

        private final String description;
        AxisKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return valueOf(key).getDescription(); } catch (IllegalArgumentException e) { return null; }
        }

        /**
         * Преобразует строку в AxisKey, выбрасывая ошибку, если ключ неизвестен.
         */
        public static AxisKey fromString(String value) throws CommandSyntaxException {
            for (AxisKey key : values()) {
                if (key.name().equalsIgnoreCase(value)) return key;
            }
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(Text.literal("Недопустимая ось: " + value)),
                    Text.literal("Недопустимая ось: " + value + ". Допустимые: " + String.join(", ", getList()))
            );
        }

        /**
         * Возвращает список всех допустимых значений.
         */
        public static Iterable<String> getList() {
            return Arrays.stream(values()).map(Enum::name).toList();
        }
    }

    public enum ModeKey {
        FALSE("Выключить рендер"),
        NONE("Рендер по умолчанию"),
        LEFT_HAND("Рендер левой руки от третьего лица"),
        RIGHT_HAND("Рендер правой руки от третьего лица"),
        HAND("Рендер правой руки от третьего лица"),
        LEFT_HOLD("Рендер левой руки от первого лица"),
        RIGHT_HOLD("Рендер правой руки от первого лица"),
        HOLD("Рендер правой руки от первого лица"),
        HEAD("Рендер головы"),
        GUI("Рендер интерфейса"),
        GROUND("Рендер выброшенного предмета"),
        FIXED("Рендер рамки для предметов");

        private final String description;
        ModeKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return valueOf(key).getDescription(); } catch (IllegalArgumentException e) { return null; }
        }

        /**
         * Возвращает канонический режим трансформации.
         */
        public ModeKey canonical() {
            return switch (this) {
                case HAND -> RIGHT_HAND;
                case HOLD -> RIGHT_HOLD;
                default -> this;
            };
        }

        public ModelTransformationMode toVanilla() {
            return switch (canonical()) {
                case LEFT_HAND -> ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
                case RIGHT_HAND, HAND -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
                case LEFT_HOLD -> ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
                case RIGHT_HOLD, HOLD -> ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
                case HEAD -> ModelTransformationMode.HEAD;
                case GUI -> ModelTransformationMode.GUI;
                case GROUND -> ModelTransformationMode.GROUND;
                case FIXED -> ModelTransformationMode.FIXED;
                case NONE, FALSE -> ModelTransformationMode.NONE;

            };
        }

        /**
         * Преобразует строку в AxisKey, выбрасывая ошибку, если ключ неизвестен.
         */
        public static ModeKey fromString(String value) throws CommandSyntaxException {
            for (ModeKey key : values()) {
                if (key.name().equalsIgnoreCase(value)) return key;
            }
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(Text.literal("Недопустимый тип рендера: " + value)),
                    Text.literal("Недопустимый тип рендера: " + value + ". Допустимые: " + String.join(", ", getList()))
            );
        }

        /**
         * Возвращает список всех допустимых значений.
         */
        public static Iterable<String> getList() {
            return Arrays.stream(values())
                    .map(ModeKey::canonical)
                    .map(Enum::name)
                    .distinct()
                    .toList();
        }
    }

    private static final ConcurrentHashMap<Identifier, BackItemTransform> backItemTransforms = new ConcurrentHashMap<>();

    public static Map<Identifier, BackItemTransform> getBackItemTransforms() {
        return Collections.unmodifiableMap(backItemTransforms);
    }

    public static BackItemTransform getBackItemTransform(ItemStack stack) {
        if (stack == null) return new BackItemTransform();

        Identifier itemId = Registries.ITEM.getId(stack.getItem());

        // Быстрый путь: если уже есть — вернуть без создания
        BackItemTransform existing = backItemTransforms.get(itemId);
        if (existing != null) return existing;

        // Создаем через фабрику, но защищаемся от гонки putIfAbsent
        BackItemTransform created = BackItemTransform.of(stack);
        BackItemTransform raced = backItemTransforms.putIfAbsent(itemId, created);
        return raced != null ? raced : created;
    }



    private static void setBackItemTransform(Identifier id, BackItemTransform backItemTransform ){
        backItemTransforms.put(id, backItemTransform);
    }

    private static void setBackItemTransform(Item item, BackItemTransform backItemTransform ){
        setBackItemTransform(Registries.ITEM.getId(item), backItemTransform);
    }

    private static void setBackItemTransform(ItemStack stack, BackItemTransform backItemTransform ){
        setBackItemTransform(stack.getItem(), backItemTransform);
    }

    public static void updateBackItemTransformTranslation(TypeKey typeKey, AxisKey axisKey, float value) {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return ;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) return;

        BackItemTransform  transform = getBackItemTransform(backStack);

        switch (typeKey.canonical()){
            case rotation -> {
                switch (axisKey) {
                    case x -> transform.transform.rotation.x = value;
                    case y -> transform.transform.rotation.y = value;
                    case z -> transform.transform.rotation.z = value;
                }
            }
            case position -> {
                switch (axisKey) {
                    case x -> transform.transform.translation.x = value;
                    case y -> transform.transform.translation.y = value;
                    case z -> transform.transform.translation.z = value;
                }
            }
            case scale -> {
                switch (axisKey) {
                    case x -> transform.transform.scale.x = value;
                    case y -> transform.transform.scale.y = value;
                    case z -> transform.transform.scale.z = value;
                }
            }
        }
        setBackItemTransform(backStack, transform);

    }

    public static void updateBackItemTransformTranslation(TypeKey typeKey, float value) {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return ;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) return;

        BackItemTransform  transform = getBackItemTransform(backStack);

        if (typeKey.canonical() == TypeKey.scales) {
            transform.transform.scale.x = value;
            transform.transform.scale.y = value;
            transform.transform.scale.z = value;
        }
        setBackItemTransform(backStack, transform);

    }

    public static void updateBackItemTransformMode(ModeKey modeKey) {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) return;

        BackItemTransform transform = getBackItemTransform(backStack);
        transform.transform_mode = modeKey.toVanilla();

        transform.enabled = modeKey != ModeKey.FALSE;

        setBackItemTransform(backStack, transform);

    }

    public static int showBackItemTransform() {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) {
            player.sendMessage(Text.literal("Backslot: no item in back").formatted(Formatting.YELLOW), false);
            return 0;
        }

        BackItemTransform transform = getBackItemTransform(backStack);

        Transformation t = transform.transform;
        ModelTransformationMode mode = transform.transform_mode;
        boolean enabled = transform.enabled;

        String rot   = String.format("rotation:     x=%.1f y=%.1f z=%.1f", t.rotation.x, t.rotation.y, t.rotation.z);
        String transl = String.format("translation: x=%.3f y=%.3f z=%.3f", t.translation.x, t.translation.y, t.translation.z);
        String scale = String.format("scale:        x=%.3f y=%.3f z=%.3f", t.scale.x, t.scale.y, t.scale.z);
        String modeStr = "mode: " + (mode != null ? mode.name() : "null");
        String enabledeStr = "enabled: " + enabled;

        player.sendMessage(Text.literal("Backslot transform for " + backStack.getItem()).formatted(Formatting.AQUA), false);
        player.sendMessage(Text.literal(rot).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(transl).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(scale).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(modeStr).formatted(Formatting.LIGHT_PURPLE), false);
        player.sendMessage(Text.literal(enabledeStr).formatted(Formatting.DARK_PURPLE), false);

        JsonObject root = new JsonObject();
        root.add("item", new JsonPrimitive(backStack.toString()));

        JsonObject rotation = new JsonObject();
        rotation.add("x", new JsonPrimitive(t.rotation.x));
        rotation.add("y", new JsonPrimitive(t.rotation.y));
        rotation.add("z", new JsonPrimitive(t.rotation.z));
        root.add("rotation", rotation);

        JsonObject translation = new JsonObject();
        translation.add("x", new JsonPrimitive(t.translation.x));
        translation.add("y", new JsonPrimitive(t.translation.y));
        translation.add("z", new JsonPrimitive(t.translation.z));
        root.add("translation", translation);

        JsonObject jscale = new JsonObject();
        jscale.add("x", new JsonPrimitive(t.scale.x));
        jscale.add("y", new JsonPrimitive(t.scale.y));
        jscale.add("z", new JsonPrimitive(t.scale.z));
        root.add("scale", jscale);

        root.add("mode", new JsonPrimitive(mode != null ? mode.name().toLowerCase() : "null"));
        root.add("enabled", new JsonPrimitive(enabled));

        String json = root.toString();

        Text button = Text.literal(" [Копировать]").styled(s ->s
                .withColor(Formatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, json))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Скопировать JSON в буфер обмена").formatted(Formatting.GOLD)))
        );
        player.sendMessage(button, false);

        return 1;
    }

    /**
     * Загружает файл конфигурации и применяет его в кэш backItemTransforms.
     * Вызывает внутренний метод, который создаёт BackItemTransform объекты и кладёт их в cache.
     */
    public static void loadBackItemTransformsFromDisk() {
        Map<Identifier, BackItemTransformStorage.BackItemTransformData> loaded = BackItemTransformStorage.loadBackItemTransforms();
        if (loaded == null || loaded.isEmpty()) return;

        // Применяем загруженные данные в наш ConcurrentHashMap атомарно:
        // создаём временную карту и затем переназначаем/вносим элементы в backItemTransforms.
        // Это предотвращает частичные изменения в случае ошибок при создании Transform.
        ConcurrentHashMap<Identifier, BackItemTransform> tmp = new ConcurrentHashMap<>();

        // applyLoadedToCache умеет собирать BackItemTransform и класть в Map<Item, BackItemTransform>.
        // Наша версия applyLoadedToCache у тебя ранее принимала Map<Identifier, BackItemTransformData> и Map<Item, BackItemTransform>.
        // Здесь мы адаптируем: создаём временную Map<Item, BackItemTransform> и затем переносим в backItemTransforms по Identifier.
        Map<Identifier, BackItemTransform> itemMap = new HashMap<>();
        BackItemTransformStorage.applyLoadedToCache(loaded, itemMap);

        // Переносим из itemMap (Item -> BackItemTransform) в tmp (Identifier -> BackItemTransform)
        for (Map.Entry<Identifier, BackItemTransform> e : itemMap.entrySet()) {
            Identifier id = e.getKey();
            BackItemTransform t = e.getValue();
            if (id != null) tmp.put(id, t);
        }

        // Атомарно обновляем основной кэш: очищаем и кладём новую карту
        backItemTransforms.clear();
        backItemTransforms.putAll(tmp);
    }
}
