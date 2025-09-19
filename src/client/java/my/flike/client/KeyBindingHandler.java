package my.flike.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindingHandler {
    public static KeyBinding backslotKey;

    public static void register() {
        backslotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.backslot.activate", // ID
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,         // Клавиша R
                "category.backslot"      // Категория в настройках
        ));
    }
}
