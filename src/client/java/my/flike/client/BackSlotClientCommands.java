package my.flike.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import my.flike.client.config.BackItemRenderConfig;
import my.flike.client.config.BackItemTransformStorage;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class BackSlotClientCommands {

    public static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(buildRoot())
        );
    }

    public static void sendModInfo(FabricClientCommandSource src) {
        src.sendFeedback(Text.of("BackSlot"));
        src.sendFeedback(Text.of("  Версия: 1.0.0")); // замени на актуальную версию
        src.sendFeedback(Text.of("  Автор: FlikeID")); // замени на реального автора
        src.sendFeedback(Text.of("  Описание: Слоты спины для оружия с поддержкой кастомных трансформаций"));
        src.sendFeedback(Text.of("  Репозиторий: https://github.com/your/repo")); // опционально
        src.sendFeedback(Text.of("Используй /backslot help для списка команд"));
    }

    public static void sendBackslotHelp(FabricClientCommandSource src) {

        // Собираем hover‑подсказку для <mode>
        List<String> modeKeys = (List<String>) BackItemRenderConfig.ModeKey.getList(); // ["fixed","thirdperson","firstperson", ...]
        String hoverContent = modeKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.ModeKey.getDescription(k);
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        // Текст подсказки как Text (многострочный)
        Text hoverText = Text.literal(hoverContent).styled(s -> s.withColor(Formatting.GRAY));

        // Основная строка с hover на части "<mode>"
        Text prefix = Text.literal("  /backslot transform render ");
        Text modePlaceholder = Text.literal("<mode>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                        .withColor(Formatting.AQUA)); // цвет для выделения
        Text suffix = Text.literal(" — Установить метод рендера");

        // Собираем hover‑подсказку для <type> (TypeKey)
        List<String> typeKeys = (List<String>) BackItemRenderConfig.TypeKey.getList(); // e.g. ["position","rotation","scale","render",...]
        String typeHoverContent = typeKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.TypeKey.getDescription(k);
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        Text typeHoverText = Text.literal(typeHoverContent).styled(s -> s.withColor(Formatting.GRAY));

        // Собираем hover‑подсказку для <axis> (AxisKey)
        List<String> axisKeys = (List<String>) BackItemRenderConfig.AxisKey.getList();
        String axisHoverContent = axisKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.AxisKey.getDescription(k); // реализуй метод в enum если ещё нет
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        Text axisHoverText = Text.literal(axisHoverContent).styled(s -> s.withColor(Formatting.GRAY));
        // Value hover text
        Text valueHoverText = Text.literal("Значений с плавающей точкой (float, десятичная дробь)").styled(s -> s.withColor(Formatting.GRAY));
        // Value hover text
        Text valueHoverTextWithRotation = Text.empty()
                .append(valueHoverText)
                .append(Text.literal("\nВращения указываются в градусах\nМетод рендера использует фиксированные значения (см. ниже)")
                        .styled(s -> s.withColor(Formatting.GRAY)));

        // Основная строка с hover на части "<type>"
        Text prefixPos = Text.literal("  /backslot transform ");
        Text typePlaceholder = Text.literal("<type>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, typeHoverText))
                        .withColor(Formatting.AQUA));
        Text space = Text.literal(" ");
        Text axisPlaceholder = Text.literal("<axis>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, axisHoverText))
                        .withColor(Formatting.AQUA));
        Text valuePlaceholder = Text.literal("<value>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, valueHoverTextWithRotation))
                        .withColor(Formatting.AQUA));
        Text suffixPos = Text.literal(" — Установить position по оси");


        // Основная строка с hover на части "<value>"
        Text prefixScales = Text.literal("  /backslot transform scales ");
        Text valueScalesPlaceholder = Text.literal("<value>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, valueHoverText))
                        .withColor(Formatting.AQUA));
        Text suffixScales = Text.literal(" — Установить одинаковый масштаб по всем осям (X/Y/Z)");

        src.sendFeedback(Text.of("Backslot commands:"));
        src.sendFeedback(Text.of("  /backslot save — Сохранить текущие трансформы в config/flike/backslot.json"));
        src.sendFeedback(Text.of("  /backslot reload — Перезапустить загрузку конфигурации из config/flike/backslot.json"));
        src.sendFeedback(Text.of("  /backslot help — Показать эту справку"));
        src.sendFeedback(Text.of("  /backslot transform — Показать текущие настройки для выбранного типа/стека"));
        src.sendFeedback(Text.empty()
                .append(prefixPos)
                .append(typePlaceholder)
                .append(space)
                .append(axisPlaceholder)
                .append(space)
                .append(valuePlaceholder)
                .append(suffixPos)
        );
        src.sendFeedback(Text.empty().append(prefix).append(modePlaceholder).append(suffix));
        src.sendFeedback(Text.empty().append(prefixScales).append(valueScalesPlaceholder).append(suffixScales));
        src.sendFeedback(Text.of("Примеры:"));
        src.sendFeedback(Text.of("  /backslot transform render fixed"));
        src.sendFeedback(Text.of("  /backslot transform position x 0.25"));
        src.sendFeedback(Text.of("  /backslot transform scales 1.5"));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildRoot() {
        return literal("backslot").executes(modInfoExecutor())
                .then(buildHelpCommand())
                .then(buildTransformCommand())
                .then(buildSaveCommand())
                .then(buildReloadCommand());
    }

    // возвращает исполнителя, который можно передать в .executes(...)
    private static Command<FabricClientCommandSource> modInfoExecutor() {
        return ctx -> {
            sendModInfo(ctx.getSource());
            return 1;
        };
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildHelpCommand() {
        return literal("help").executes(ctx -> {
            sendBackslotHelp(ctx.getSource());
            return 1;
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildTransformCommand() {
        return literal("transform")
                .executes(ctx -> BackItemRenderConfig.showBackItemTransform())
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
