package my.flike.client;

import my.flike.Backslot;
import my.flike.client.commands.BackSlotClientCommandsBuilder;
import my.flike.client.config.BackItemRenderConfig;
import net.fabricmc.api.ClientModInitializer;


public class BackSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Backslot.LOG("Backslot client init. Registering client-side handlers.");
        BackItemRenderConfig.loadBackItemTransformsFromDisk();
        KeyBindingHandler.register();
        BackSlotClientLogic.register();
        BackSlotClientCommandsBuilder.registerCommand();
    }
}
