package my.flike.client.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import my.flike.BackSlotLogic;
import my.flike.Backslot;
import my.flike.client.config.BackItemRenderConfig;
import my.flike.client.render.BackItemTransform;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class BackSlotCommandsHandler {

    public static void sendModInfo(FabricClientCommandSource src) {
        src.sendFeedback(Text.of(Formatting.BLUE + "BackSlot"));


        Text prefixVer = Text.literal("  Версия: ");
        Text commandVer = Text.literal(Backslot.getModVersion())
                .styled(s -> s.withColor(Formatting.GRAY));

        Text prefixAuthor = Text.literal("  Автор: ");
        Text commandAuthor = Text.literal(Backslot.getModAuthors())
                .styled(s -> s.withColor(Formatting.GRAY));

        Text prefixDesc = Text.literal("  Описание: ");
        Text commandDesc = Text.literal(Backslot.getModDescription())
                .styled(s -> s.withColor(Formatting.GRAY));

        String gitUrl = Backslot.getModContact("sources");
        Text prefixRepo = Text.literal("  Репозиторий: ");
        Text commandRepo = Text.literal(gitUrl)
                .styled(s -> s.withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, gitUrl))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Открыть в браузере"))));

        src.sendFeedback(Text.empty().append(prefixVer).append(commandVer));
        src.sendFeedback(Text.empty().append(prefixAuthor).append(commandAuthor));
        src.sendFeedback(Text.empty().append(prefixDesc).append(commandDesc));
        src.sendFeedback(Text.empty().append(prefixRepo).append(commandRepo));

        Text prefix = Text.literal("Используй ");
        Text command = Text.literal("/backslot help")
                .styled(s -> s.withColor(  Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/backslot help"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Открыть список команд"))));
        Text suffix = Text.literal(" для списка команд");

        src.sendFeedback(Text.empty().append(prefix).append(command).append(suffix));
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
        Text prefix = Text.literal(  Formatting.AQUA +"  /backslot transform render ").styled(
                s->s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform render"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text modePlaceholder = Text.literal("<mode>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                        .withColor(Formatting.GREEN)); // цвет для выделения
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
        Text prefixPos = Text.literal(  Formatting.AQUA +"  /backslot transform ").styled(
                s->s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text typePlaceholder = Text.literal("<type>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, typeHoverText))
                        .withColor(  Formatting.GREEN));
        Text space = Text.literal(" ");
        Text axisPlaceholder = Text.literal("<axis>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, axisHoverText))
                        .withColor(  Formatting.GREEN));
        Text valuePlaceholder = Text.literal("<value>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, valueHoverTextWithRotation))
                        .withColor(  Formatting.GREEN));
        Text suffixPos = Text.literal(" — Установить position по оси");


        // Основная строка с hover на части "<value>"
        Text prefixScales = Text.literal(  Formatting.AQUA +"  /backslot transform scales ").styled(
                s->s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform scales"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text valueScalesPlaceholder = Text.literal("<value>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, valueHoverText))
                        .withColor(  Formatting.GREEN));
        Text suffixScales = Text.literal(" — Установить одинаковый масштаб по всем осям (X/Y/Z)");

        // Основная строка с hover на части "<jsonString>"
        Text jsonHoverText = Text.literal("""
                Json строка формата:\s
                {
                  "rotation":{
                    "x":90.0,
                    "y":50.0,
                    "z":90.0
                  },
                  "translation":{
                    "x":0.1,
                    "y":0.6,
                    "z":0.17
                  },
                  "scale":{
                    "x":1.0,
                    "y":1.0,
                    "z":1.0
                  },
                  "mode":"ground",
                  "enabled":true
                }""").styled(s -> s.withColor(Formatting.GRAY));
        Text prefixJson = Text.literal(  Formatting.AQUA +"  /backslot transform json ").styled(
                s->s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform json"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text valueJsonPlaceholder = Text.literal("<jsonString>")
                .styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, jsonHoverText))
                        .withColor(  Formatting.GREEN));
        Text suffixJson = Text.literal(" — Загрузить трансформацию из json");


        Text prefixSave = Text.literal("  ");
        Text commandSave = Text.literal("/backslot save")
                .styled(s -> s
                        .withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot save"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text suffixSave = Text.literal(" — Сохранить текущие трансформы в config/flike/backslot.json");

        Text prefixReload = Text.literal("  ");
        Text commandReload = Text.literal("/backslot reload")
                .styled(s -> s
                        .withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot reload"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text suffixReload = Text.literal(" — Загрузить конфигурации из config/flike/backslot.json");

        Text prefixHelp = Text.literal("  ");
        Text commandHelp = Text.literal("/backslot help")
                .styled(s -> s
                        .withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot help"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text suffixHelp = Text.literal(" — Показать эту справку");

        Text prefixTransform = Text.literal("  ");
        Text commandTransform = Text.literal("/backslot transform")
                .styled(s -> s
                        .withColor(Formatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду"))));
        Text suffixTransform = Text.literal(" — Показать текущие настройки для выбранного типа/стека");


        src.sendFeedback(Text.of(Formatting.WHITE +"Backslot commands:"));
        src.sendFeedback(Text.empty().append(prefixSave).append(commandSave).append(suffixSave));
        src.sendFeedback(Text.empty().append(prefixReload).append(commandReload).append(suffixReload));
        src.sendFeedback(Text.empty().append(prefixHelp).append(commandHelp).append(suffixHelp));
        src.sendFeedback(Text.empty().append(prefixTransform).append(commandTransform).append(suffixTransform));
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
        src.sendFeedback(Text.empty().append(prefixJson).append(valueJsonPlaceholder).append(suffixJson));
        src.sendFeedback(Text.literal(Formatting.WHITE +"Примеры:"));
        src.sendFeedback(Text.literal(Formatting.AQUA +"  /backslot transform render fixed").styled(s -> s
                .withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform render fixed"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду")))));
        src.sendFeedback(Text.literal(Formatting.AQUA +"  /backslot transform position x 0.25").styled(s -> s
                .withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform position x 0.25"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду")))));
        src.sendFeedback(Text.literal(Formatting.AQUA +"  /backslot transform scales 1.5").styled(s -> s
                .withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/backslot transform scales 1.5"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Вставить команду")))));
    }

    public static int showBackItemTransform() {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;

        ItemStack backStack = BackSlotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) {
            player.sendMessage(Text.literal("Backslot: no item in back").formatted(Formatting.YELLOW), false);
            return 0;
        }

        BackItemTransform transform = BackItemRenderConfig.getBackItemTransform(backStack);

        Transformation t = transform.transform;
        ModelTransformationMode mode = transform.transform_mode;
        boolean enabled = transform.enabled;

        String rot   = String.format("rotation:     x=%.1f y=%.1f z=%.1f", t.rotation.x, t.rotation.y, t.rotation.z);
        String transl = String.format("translation: x=%.3f y=%.3f z=%.3f", t.translation.x, t.translation.y, t.translation.z);
        String scale = String.format("scale:        x=%.3f y=%.3f z=%.3f", t.scale.x, t.scale.y, t.scale.z);
        String modeStr = "mode: " + (mode != null ? mode.name() : "null");
        String enabledeStr = "enabled: " + enabled;

        player.sendMessage(Text.literal("Backslot transform for " + backStack.getItem()).formatted(  Formatting.GREEN), false);
        player.sendMessage(Text.literal(rot).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(transl).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(scale).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(modeStr).formatted(Formatting.LIGHT_PURPLE), false);
        player.sendMessage(Text.literal(enabledeStr).formatted(Formatting.DARK_PURPLE), false);

        JsonObject root = new JsonObject();
        Identifier id = Registries.ITEM.getId(backStack.getItem());
        root.add("item", new JsonPrimitive(id.toString()));

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
}
