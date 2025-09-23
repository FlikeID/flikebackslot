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
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Collectors;

public class BackSlotCommandsHandler {

    public static void sendModInfo(FabricClientCommandSource src) {
        src.sendFeedback(Text.translatable("category.backslot"));


        MutableText commandVer = Text.literal(Backslot.getModVersion())
                .setStyle(BackSlotCommandsStyles.description);
        MutableText prefixVer = Text.translatable("chat.backslot-flike.version", commandVer);

        MutableText commandAuthor = Text.literal(Backslot.getModAuthors())
                .setStyle(BackSlotCommandsStyles.description);
        MutableText prefixAuthor = Text.translatable("chat.backslot-flike.author", commandAuthor);

        MutableText commandDesc = Text.literal(Backslot.getModDescription())
                .setStyle(BackSlotCommandsStyles.description);
        MutableText prefixDesc = Text.translatable("chat.backslot-flike.description", commandDesc);

        String gitUrl = Backslot.getModContact("sources");
        MutableText commandRepo = Text.literal(gitUrl)
                .setStyle(BackSlotCommandsStyles.web_clickable(gitUrl,"chat.backslot-flike.browser_open"));
        MutableText prefixRepo = Text.translatable("chat.backslot-flike.source", commandRepo);

        src.sendFeedback(prefixVer);
        src.sendFeedback(prefixAuthor);
        src.sendFeedback(prefixDesc);
        src.sendFeedback(prefixRepo);

        MutableText helpCommand= Text.literal("/backslot help")
                .setStyle(BackSlotCommandsStyles.send_command("/backslot help", "chat.backslot-flike.show_commands"));
        MutableText useHelp = Text.translatable("chat.backslot-flike.use_help",helpCommand);

        src.sendFeedback(useHelp);
    }

    public static void sendBackslotHelp(FabricClientCommandSource src) {

        // Собираем hover‑подсказку для <mode>
        List<String> modeKeys = (List<String>) BackItemRenderConfig.ModeKey.getList();
        String hoverContent = modeKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.ModeKey.getDescription(k);
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        // Текст подсказки как MutableText (многострочный)
        MutableText hoverText = Text.literal(hoverContent)
                .setStyle(BackSlotCommandsStyles.description);

        // Основная строка с hover на части "<mode>"
        MutableText prefix = Text.literal("  /backslot transform render ")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform render"));
        MutableText modePlaceholder = Text.literal("<mode>")
                .setStyle(BackSlotCommandsStyles.hover_hint(hoverText)); // цвет для выделения
        MutableText suffix = Text.translatable("chat.backslot-flike.set_render").setStyle(Style.EMPTY);

        // Собираем hover‑подсказку для <type> (TypeKey)
        List<String> typeKeys = (List<String>) BackItemRenderConfig.TypeKey.getList(); // e.g. ["position","rotation","scale","render",...]
        String typeHoverContent = typeKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.TypeKey.getDescription(k);
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        MutableText typeHoverText = Text.literal(typeHoverContent)
                .setStyle(BackSlotCommandsStyles.description);

        // Собираем hover‑подсказку для <axis> (AxisKey)
        List<String> axisKeys = (List<String>) BackItemRenderConfig.AxisKey.getList();
        String axisHoverContent = axisKeys.stream()
                .map(k -> {
                    String desc = BackItemRenderConfig.AxisKey.getDescription(k); // реализуй метод в enum если ещё нет
                    if (desc == null || desc.isEmpty()) return "• " + k;
                    return "• " + k + " — " + desc;
                })
                .collect(Collectors.joining("\n"));

        MutableText axisHoverText = Text.translatable(axisHoverContent)
                .setStyle(BackSlotCommandsStyles.description);
        // Value hover text
        MutableText valueHoverText = Text.translatable("chat.backslot-flike.desc_float")
                .setStyle(BackSlotCommandsStyles.description);
        // Value hover text
        MutableText valueHoverTextWithRotation = Text.empty()
                .append(valueHoverText)
                .append(
                        Text.translatable("chat.backslot-flike.rotation_n_render")
                                .setStyle(BackSlotCommandsStyles.description)
                );

        // Основная строка с hover на части "<type>"
        MutableText prefixPos = Text.literal(  "  /backslot transform ")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform"));
        MutableText typePlaceholder = Text.literal("<type>")
                .setStyle(BackSlotCommandsStyles.hover_hint(typeHoverText));
        MutableText axisPlaceholder = Text.literal("<axis>")
                .setStyle(BackSlotCommandsStyles.hover_hint(axisHoverText));
        MutableText valuePlaceholder = Text.literal("<value>")
                .setStyle(BackSlotCommandsStyles.hover_hint(valueHoverTextWithRotation));
        MutableText suffixPos = Text.translatable("chat.backslot-flike.set_position").setStyle(Style.EMPTY);


        // Основная строка с hover на части "<value>"
        MutableText prefixScales = Text.literal("  /backslot transform scales ")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform scales"));
        MutableText suffixScales = Text.translatable("chat.backslot-flike.set_scales").setStyle(Style.EMPTY);

        // Основная строка с hover на части "<jsonString>"
        MutableText jsonHoverText = Text.translatable("chat.backslot-flike.json_format", """
                \s
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
                }
                """).setStyle(BackSlotCommandsStyles.description);
        MutableText prefixJson = Text.literal("  /backslot transform json ")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform json"));
        MutableText valueJsonPlaceholder = Text.literal("<jsonString>")
                .setStyle(BackSlotCommandsStyles.hover_hint(jsonHoverText));
        MutableText suffixJson = Text.translatable("chat.backslot-flike.json_load").setStyle(Style.EMPTY);


        MutableText commandSave = Text.literal("  /backslot save ")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot save"));
        MutableText suffixSave = Text.translatable( "chat.backslot-flike.save").setStyle(Style.EMPTY);


        MutableText commandReload = Text.literal("  /backslot reload")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot reload"));
        MutableText suffixReload = Text.translatable("chat.backslot-flike.load").setStyle(Style.EMPTY);

        MutableText commandHelp = Text.literal("  /backslot help")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot help"));
        MutableText suffixHelp = Text.translatable("chat.backslot-flike.help").setStyle(Style.EMPTY);


        MutableText commandTransform = Text.literal("  /backslot transform")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform"));
        MutableText suffixTransform = Text.translatable("chat.backslot-flike.transform").setStyle(Style.EMPTY);

        MutableText exampleTransform = Text.literal("  /backslot transform render fixed")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform render fixed"));
        MutableText exampleTransform2 = Text.literal("  /backslot transform position x 0.25")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform position x 0.25"));
        MutableText exampleTransform3 = Text.literal("  /backslot transform scales 1.5")
                .setStyle(BackSlotCommandsStyles.paste_command("/backslot transform scales 1.5"));


        src.sendFeedback(Text.translatable("chat.backslot-flike.commands"));
        src.sendFeedback(Text.literal("").append(commandSave).append(suffixSave));
        src.sendFeedback(Text.literal("").append(commandReload).append(suffixReload));
        src.sendFeedback(Text.literal("").append(commandHelp).append(suffixHelp));
        src.sendFeedback(Text.literal("").append(commandTransform).append(suffixTransform));
        src.sendFeedback(Text.literal("")
                .append(prefixPos)
                .append(typePlaceholder)
                .append(Text.literal(" ") )
                .append(axisPlaceholder)
                .append(Text.literal(" ") )
                .append(valuePlaceholder)
                .append(suffixPos)
        );
        src.sendFeedback(Text.literal("").append(prefix).append(modePlaceholder).append(suffix));
        src.sendFeedback(Text.literal("").append(prefixScales).append(valuePlaceholder).append(suffixScales));
        src.sendFeedback(Text.literal("").append(prefixJson).append(valueJsonPlaceholder).append(suffixJson));
        src.sendFeedback(Text.translatable("chat.backslot-flike.examples"));
        src.sendFeedback(exampleTransform);
        src.sendFeedback(exampleTransform2);
        src.sendFeedback(exampleTransform3);
    }

    public static int showBackItemTransform(FabricClientCommandSource src) {
        AbstractClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;

        ItemStack backStack = BackSlotLogic.getBackItemStack(player);
        if (backStack == null || backStack.isEmpty()) {
            src.sendFeedback(
                    Text.translatable("chat.backslot-flike.backslot_empty")
                            .setStyle(BackSlotCommandsStyles.warning)
            );
            return 0;
        }

        BackItemTransform transform = BackItemRenderConfig.getBackItemTransform(backStack);

        Transformation t = transform.transform;
        ModelTransformationMode mode = transform.transform_mode;
        boolean enabled = transform.enabled;

        MutableText transformFor = Text.translatable("chat.backslot-flike.transform_for", backStack.getItem())
                .setStyle(BackSlotCommandsStyles.applied);
        MutableText rot         = Text.literal("rotation:     x="+t.rotation.x+"\ty="+t.rotation.y+"\tz="+t.rotation.z);
        MutableText transl      = Text.literal("translation:  x="+t.translation.x+"\ty="+t.translation.y+"\tz="+t.translation.z);
        MutableText scale       = Text.literal("rotation:     x="+t.scale.x+"\ty="+t.scale.y+"\tz="+t.scale.z);
        MutableText modeStr     = Text.literal("mode: " + (mode != null ? mode.name() : "null"))
                .formatted(Formatting.WHITE);
        MutableText enabledStr  = Text.literal("enabled: " + enabled)
                .formatted(Formatting.DARK_PURPLE);

        src.sendFeedback(transformFor);
        src.sendFeedback(rot);
        src.sendFeedback(transl);
        src.sendFeedback(scale);
        src.sendFeedback(modeStr);
        src.sendFeedback(enabledStr);
        src.sendFeedback(transl);
        src.sendFeedback(scale);
        src.sendFeedback(modeStr);
        src.sendFeedback(enabledStr);

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

        JsonObject jsonScale = new JsonObject();
        jsonScale.add("x", new JsonPrimitive(t.scale.x));
        jsonScale.add("y", new JsonPrimitive(t.scale.y));
        jsonScale.add("z", new JsonPrimitive(t.scale.z));
        root.add("scale", jsonScale);

        root.add("mode", new JsonPrimitive(mode != null ? mode.name().toLowerCase() : "null"));
        root.add("enabled", new JsonPrimitive(enabled));

        String jsonString = root.toString();

        MutableText button = Text.translatable("chat.backslot-flike.copy")
                .setStyle(BackSlotCommandsStyles.json_clipboard(jsonString));
        src.sendFeedback(button);

        return 1;
    }
}
