package my.flike.client;

import my.flike.Backslot;
import my.flike.client.commands.BackSlotClientCommandsBuilder;
import my.flike.client.config.BackItemRenderConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;


public class BackSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Backslot.LOG(Text.translatable("log.backslot-flike.client_init_start"));
        BackItemRenderConfig.loadBackItemTransformsFromDisk();
        KeyBindingHandler.register();
        BackSlotClientLogic.register();
        BackSlotClientCommandsBuilder.registerCommand();
        Backslot.LOG(Text.translatable("log.backslot-flike.client_init_end"));
    }
}
