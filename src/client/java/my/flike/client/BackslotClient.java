package my.flike.client;

import my.flike.Backslot;
import net.fabricmc.api.ClientModInitializer;



public class BackslotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Backslot.LOG("Backslot client init. Registering client-side handlers.");
        KeyBindingHandler.register();
        BackslotLogic.register();
        BackslotClientCommands.register();
    }
}
