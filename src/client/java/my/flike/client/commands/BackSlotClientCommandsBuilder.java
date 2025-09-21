package my.flike.client.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import my.flike.BackSlotLogic;
import my.flike.client.config.BackItemRenderConfig;
import my.flike.client.config.BackItemTransformStorage;
import my.flike.client.render.BackItemTransform;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class BackSlotClientCommandsBuilder {

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(buildRoot())
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRoot() {
        return literal("backslot").executes(modInfoExecutor())
                .then(buildHelpCommand())
                .then(buildTransformCommand())
                .then(buildSaveCommand())
                .then(buildReloadCommand());
    }

    // подкоманда json
    private static LiteralArgumentBuilder<FabricClientCommandSource> buildJsonCommand() {
        return literal("json")
                .then(argument("jsonString", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String jsonString = StringArgumentType.getString(ctx, "jsonString");
                            FabricClientCommandSource src = ctx.getSource();
                            try {
                                ClientPlayerEntity player = ctx.getSource().getPlayer();
                                if (BackSlotLogic.getBackItemStack(player).isEmpty()){
                                    src.sendFeedback(Text.of(Formatting.YELLOW + "Backslot: no item in back"));
                                    return 0;
                                }
                                // Получаем BackItemTransform
                                BackItemTransform backItemTransform = BackItemTransformStorage.applyTransformFromJson(jsonString);
                                // Применяем BackItemTransform к ClientPlayer (берётся из контекста FabricClientCommandSource)
                                BackItemRenderConfig.setBackItemTransform(player, backItemTransform);

                                src.sendFeedback(Text.of(Formatting.GREEN + "Backslot transform applied from JSON."));
                                return 1;
                            } catch (com.google.gson.JsonParseException jex) {
                                src.sendError(Text.of(Formatting.RED + "Invalid JSON: " + jex.getMessage()));
                                return 0;
                            } catch (IllegalArgumentException iex) {
                                src.sendError(Text.of(Formatting.RED + "Invalid transform: " + iex.getMessage()));
                                return 0;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                src.sendError(Text.of(Formatting.RED + "Failed to apply transform: " + ex.getMessage()));
                                return 0;
                            }
                        })
                );
    }

    // возвращает исполнителя, который можно передать в .executes(...)
    private static Command<FabricClientCommandSource> modInfoExecutor() {
        return ctx -> {
            BackSlotCommandsHandler.sendModInfo(ctx.getSource());
            return 1;
        };
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildHelpCommand() {
        return literal("help").executes(ctx -> {
            BackSlotCommandsHandler.sendBackslotHelp(ctx.getSource());
            return 1;
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildTransformCommand() {
        return literal("transform")
                .executes(ctx -> BackSlotCommandsHandler.showBackItemTransform())
                .then(buildJsonCommand())
                .then(buildRenderCommand())
                .then(buildScalesCommand())
                .then(buildTranslationCommand());
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildReloadCommand() {
        return literal("reload")
                .executes(ctx -> {
                    try {
                        BackItemRenderConfig.loadBackItemTransformsFromDisk();
                        ctx.getSource().sendFeedback(Text.of("Backslot config reloaded."));
                    } catch (Exception ex) {
                        //noinspection CallToPrintStackTrace
                        ex.printStackTrace();
                        ctx.getSource().sendError(Text.of("Failed to reload backslot config: " + ex.getMessage()));
                    }
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildSaveCommand() {
        return literal("save")
                .executes(ctx -> {
                    try {
                        BackItemTransformStorage.saveBackItemTransforms(BackItemRenderConfig.getBackItemTransforms()); // ты реализировал этот метод ранее
                        ctx.getSource().sendFeedback(Text.of("Backslot config saved.")); // отправить фидбек игроку
                    } catch (Exception ex) {
                        // логирование и информирование пользователя
                        //noinspection CallToPrintStackTrace
                        ex.printStackTrace();
                        ctx.getSource().sendError(Text.of("Failed to save backslot config: " + ex.getMessage()));
                    }
                    return 1;
                });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRenderCommand() {
        return literal("render")
                .then(argument("mode", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(BackItemRenderConfig.ModeKey.getList(), builder))
                        .executes(ctx -> {
                            String modeStr = StringArgumentType.getString(ctx, "mode");
                            BackItemRenderConfig.ModeKey modeKey = BackItemRenderConfig.ModeKey.fromString(modeStr);
                            BackItemRenderConfig.updateBackItemTransformMode(modeKey);
                            return 1;
                        })
                );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildScalesCommand() {
        return literal("scales")
                .then(argument("value", FloatArgumentType.floatArg())
                        .executes(ctx -> {
                            float value = FloatArgumentType.getFloat(ctx, "value");
                            // применяем ко всем типам/текущему типу — выбери семантику, здесь пример для текущего айтема игрока
                            BackItemRenderConfig.updateBackItemTransformTranslation(
                                    BackItemRenderConfig.TypeKey.scales,
                                    value
                            );
                            return 1;
                        })
                );
    }

    // <-- обратите внимание: общий возвращаемый тип ArgumentBuilder
    private static ArgumentBuilder<FabricClientCommandSource, ?> buildTranslationCommand() {
        return argument("type", StringArgumentType.word())
                .suggests((ctx, builder) -> CommandSource.suggestMatching(BackItemRenderConfig.TypeKey.getList(), builder))
                .then(argument("axis", StringArgumentType.word())
                        .suggests((ctx, builder) -> CommandSource.suggestMatching(BackItemRenderConfig.AxisKey.getList(), builder))
                        .then(argument("value", FloatArgumentType.floatArg())
                                .executes(ctx -> {
                                    String typeStr = StringArgumentType.getString(ctx, "type");
                                    String axisStr = StringArgumentType.getString(ctx, "axis");
                                    float value = FloatArgumentType.getFloat(ctx, "value");

                                    BackItemRenderConfig.TypeKey typeKey = BackItemRenderConfig.TypeKey.fromString(typeStr);
                                    BackItemRenderConfig.AxisKey axisKey = BackItemRenderConfig.AxisKey.fromString(axisStr);

                                    BackItemRenderConfig.updateBackItemTransformTranslation(typeKey, axisKey, value);
                                    return 1;
                                })
                        )
                );
    }
}
