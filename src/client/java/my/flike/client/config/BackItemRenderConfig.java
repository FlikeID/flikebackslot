package my.flike.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import my.flike.BackSlotLogic;
import my.flike.client.BackSlotClientLogic;
import my.flike.client.render.BackItemTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
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
        scales("равномерный масштаб (см. ниже)"), json("загрузка параметров из json-строки"),
        translation("синоним position");
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
                case translation, pos, p -> position;
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

        // Возвращаем трансформацию по умолчанию для данного типа предмета
        return BackItemTransform.of(stack);
    }



    public static void setBackItemTransform(Identifier id, BackItemTransform backItemTransform ){
        backItemTransforms.put(id, backItemTransform);
    }

    public static void setBackItemTransform(Item item, BackItemTransform backItemTransform ){
        setBackItemTransform(Registries.ITEM.getId(item), backItemTransform);
    }

    public static void setBackItemTransform(ItemStack stack, BackItemTransform backItemTransform ){
        setBackItemTransform(stack.getItem(), backItemTransform);
    }

    public static void setBackItemTransform(ClientPlayerEntity player, BackItemTransform backItemTransform ){
        setBackItemTransform(BackSlotLogic.getBackItemStack(player), backItemTransform);
    }

    public static void updateBackItemTransformTranslation(TypeKey typeKey, AxisKey axisKey, float value) {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return ;

        ItemStack backStack = BackSlotLogic.getBackItemStack(player);
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

        ItemStack backStack = BackSlotLogic.getBackItemStack(player);
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

        ItemStack backStack = BackSlotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) return;

        BackItemTransform transform = getBackItemTransform(backStack);
        transform.transform_mode = modeKey.toVanilla();

        transform.enabled = modeKey != ModeKey.FALSE;

        setBackItemTransform(backStack, transform);

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
