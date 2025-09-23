package my.flike.client.config;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import my.flike.BackSlotLogic;
import my.flike.client.render.BackItemTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BackItemRenderConfig {

    public enum TypeKey {
        rotation("chat.backslot-flike.rotation"), rot("chat.backslot-flike.rotation"), r("chat.backslot-flike.rotation"),
        position("chat.backslot-flike.position"), pos("chat.backslot-flike.position"), p("chat.backslot-flike.position"),
        scale("chat.backslot-flike.scale"), scl("chat.backslot-flike.scale"), s("chat.backslot-flike.scale"),
        render("chat.backslot-flike.render"), ren("chat.backslot-flike.render"), m("chat.backslot-flike.render"),
        scales("chat.backslot-flike.scales"), json("chat.backslot-flike.json"),
        translation("chat.backslot-flike.translation");
        private final String description;
        TypeKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return Text.translatable(valueOf(key).getDescription()).getString(); } catch (IllegalArgumentException e) { return null; }
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
                    new SimpleCommandExceptionType(Text.translatable("chat.backslot-flike.wrong_argument", value)),
                    Text.translatable("chat.backslot-flike.available_arguments", value, String.join(", ", getList()))
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
        x("chat.backslot-flike.x_axis"),
        y("chat.backslot-flike.y_axis"),
        z("chat.backslot-flike.z_axis");

        private final String description;
        AxisKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return Text.translatable(valueOf(key).getDescription()).getString(); } catch (IllegalArgumentException e) { return null; }
        }

        /**
         * Преобразует строку в AxisKey, выбрасывая ошибку, если ключ неизвестен.
         */
        public static AxisKey fromString(String value) throws CommandSyntaxException {
            for (AxisKey key : values()) {
                if (key.name().equalsIgnoreCase(value)) return key;
            }
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(Text.translatable("chat.backslot-flike.wrong_axis", value)),
                    Text.translatable("chat.backslot-flike.available_axis", value, String.join(", ", getList()))
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
        FALSE("chat.backslot-flike.false"),
        NONE("chat.backslot-flike.none"),
        LEFT_HAND("chat.backslot-flike.left_hand"),
        RIGHT_HAND("chat.backslot-flike.right_hand"),
        HAND("chat.backslot-flike.right_hand"),
        LEFT_HOLD("chat.backslot-flike.left_hold"),
        RIGHT_HOLD("chat.backslot-flike.right_hold"),
        HOLD("chat.backslot-flike.right_hold"),
        HEAD("chat.backslot-flike.head"),
        GUI("chat.backslot-flike.gui"),
        GROUND("chat.backslot-flike.ground"),
        FIXED("chat.backslot-flike.fixed");

        private final String description;
        ModeKey(String description) { this.description = description; }
        public String getDescription() { return description; }

        public static String getDescription(String key) {
            try { return Text.translatable(valueOf(key).getDescription()).getString(); } catch (IllegalArgumentException e) { return null; }
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
                    new SimpleCommandExceptionType(Text.translatable("chat.backslot-flike.wrong_render", value)),
                    Text.translatable("chat.backslot-flike.available_render", value, String.join(", ", getList()))
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
