package my.flike.client.render;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import my.flike.client.BackslotLogic;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.HashMap;

public class BackItemRenderConfig {

    public enum TypeKey {
        position, pos, p,
        rotation, rot, r,
        scale, scl, s,
        render, ren, m;

        /**
         * Возвращает канонический тип: position, rotation или scale.
         */
        public TypeKey canonical() {
            return switch (this) {
                case pos, p -> position;
                case rot, r -> rotation;
                case scl, s -> scale;
                case ren, m -> render;
                default -> this;
            };
        }

        /**
         * Проверяет, существует ли такой тип.
         */
        public static boolean isValid(String value) {
            return Arrays.stream(values())
                    .anyMatch(k -> k.name().equalsIgnoreCase(value));
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
        x, y, z;

        /**
         * Проверяет, существует ли такой ключ.
         */
        public static boolean isValid(String value) {
            return Arrays.stream(values())
                    .anyMatch(k -> k.name().equalsIgnoreCase(value));
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
        NONE,
        LEFT_HAND,
        RIGHT_HAND, HAND,
        LEFT_HOLD,
        RIGHT_HOLD, HOLD,
        HEAD,
        GUI,
        GROUND,
        FIXED;

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

        public ModelTransformationMode toVanilla() throws CommandSyntaxException {
            return switch (canonical()) {
                case LEFT_HAND -> ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
                case RIGHT_HAND -> ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
                case LEFT_HOLD -> ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
                case RIGHT_HOLD -> ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
                case HEAD -> ModelTransformationMode.HEAD;
                case GUI -> ModelTransformationMode.GUI;
                case GROUND -> ModelTransformationMode.GROUND;
                case FIXED -> ModelTransformationMode.FIXED;
                case NONE -> ModelTransformationMode.NONE;
                default -> throw new CommandSyntaxException(
                        new SimpleCommandExceptionType(Text.literal("Недопустимый тип рендера: " + this.name())),
                        Text.literal("Недопустимый тип рендера: " + this.name() + ". Допустимые: " + String.join(", ", getList()))
                );
            };
        }

        /**
         * Проверяет, существует ли такой ключ.
         */
        public static boolean isValid(String value) {
            return Arrays.stream(values())
                    .anyMatch(k -> k.name().equalsIgnoreCase(value));
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


    private static final HashMap<ItemStack,BackItemTransform> backItemTransforms = new HashMap<>();

    public static BackItemTransform getBackItemTransform(ItemStack stack) {
        return backItemTransforms.getOrDefault(stack, new BackItemTransform());
    }

    private static void setBackItemTransform(ItemStack stack, BackItemTransform backItemTransform ){
        backItemTransforms.put(stack, backItemTransform);
    }

    private static void setBackItemTransform(ItemStack stack, Transformation transform){
        backItemTransforms.put(stack, new BackItemTransform(transform));
    }
    private static void setBackItemTransform(ItemStack stack, ModelTransformationMode transform_mode){
        backItemTransforms.put(stack, new BackItemTransform(transform_mode));
    }

    private static void updateBackItemTransformTranslation(String type, String axis, float value) throws CommandSyntaxException {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return ;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        BackItemTransform  transform = getBackItemTransform(backStack);

        switch (TypeKey.fromString(type).canonical()){
            case position:
                switch (AxisKey.fromString(axis)) {
                    case x:
                        transform.transform.translation.x = value;
                    case y:
                        // логика для y
                        transform.transform.translation.y = value;
                    case z:
                        // логика для z
                        transform.transform.translation.z = value;
                }
            case rotation:
                switch (AxisKey.fromString(axis)) {
                    case x:
                        transform.transform.rotation.x = value;
                    case y:
                        // логика для y
                        transform.transform.rotation.y = value;
                    case z:
                        // логика для z
                        transform.transform.rotation.z = value;
                }
            case scale:
                switch (AxisKey.fromString(axis)) {
                    case x:
                        transform.transform.scale.x = value;
                    case y:
                        // логика для y
                        transform.transform.scale.y = value;
                    case z:
                        // логика для z
                        transform.transform.scale.z = value;
                }
        }
        setBackItemTransform(backStack, transform);

    }

    private static void updateBackItemTransformMode(String value) throws CommandSyntaxException {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return ;

        ItemStack backStack = BackslotLogic.getBackItemStack(player);
        BackItemTransform  transform = getBackItemTransform(backStack);


        transform.transform_mode = ModeKey.fromString(value).toVanilla();


        setBackItemTransform(backStack, transform);

    }

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("backslot")
                    .then(ClientCommandManager.literal("transform")
                            // Вариант 1: transform position x 1
                            .then(ClientCommandManager.argument("type", StringArgumentType.word())
                                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                    TypeKey.getList(),
                                                    builder
                                            )).executes(ctx -> {
                                                String typeStr = StringArgumentType.getString(ctx, "type");
                                                TypeKey.fromString(typeStr);
                                                return 1;
                                            })
                                    .then(ClientCommandManager.argument("axis", StringArgumentType.word())
                                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                    AxisKey.getList(),
                                                    builder
                                            )).executes(ctx -> {
                                                String typeStr = StringArgumentType.getString(ctx, "type");
                                                AxisKey.fromString(typeStr);
                                                return 1;
                                            }).then(ClientCommandManager.argument("value", FloatArgumentType.floatArg())
                                                    .executes(ctx -> {

                                                        String type = StringArgumentType.getString(ctx, "type");
                                                        String axis = StringArgumentType.getString(ctx, "axis");
                                                        float value = FloatArgumentType.getFloat(ctx, "value");

                                                        BackItemRenderConfig.updateBackItemTransformTranslation(type,axis,value);

                                                        return 1;
                                                    }))))
                            // Вариант 2: transform render fixed
                            .then(ClientCommandManager.literal("render")
                                    .then(ClientCommandManager.argument("mode", StringArgumentType.word())
                                            .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                    ModeKey.getList(),
                                                    builder
                                            )).executes(ctx -> {
                                                String mode = StringArgumentType.getString(ctx, "mode");
                                                BackItemRenderConfig.updateBackItemTransformMode(mode);
                                                return 1;
                                            })))
                    )
                )
        );

    }






}