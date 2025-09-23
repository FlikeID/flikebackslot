package my.flike.client.commands;

import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class BackSlotCommandsStyles {
    public static Style warning = Style.EMPTY.withColor(Formatting.YELLOW);
    public static Style applied = Style.EMPTY.withColor(Formatting.GREEN);
    public static Style description = Style.EMPTY.withColor(Formatting.GRAY);
    public static Style send_command(String command, String trans_key){
        return Style.EMPTY.withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(trans_key)));
    }
    public static Style paste_command(String command) {
        return Style.EMPTY.withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.backslot-flike.paste_command")));
    }
    public static Style hover_hint(MutableText hoverText){
        return Style.EMPTY.withColor(Formatting.GREEN)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }
    public static Style web_clickable(String url, String trans_key){
        return Style.EMPTY.withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(trans_key)));
    }
    public static Style json_clipboard(String url){
        return Style.EMPTY.withColor(Formatting.GOLD)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.backslot-flike.json_clipboard")));
    }
}
