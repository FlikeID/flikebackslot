package my.flike.client;

import io.netty.buffer.Unpooled;
import my.flike.Backslot;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class BackSlotClientLogic {

    public static final Identifier SWAP_PACKET = new Identifier(Backslot.MOD_ID, "flike_backslot_swap");

    public static void register() {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (KeyBindingHandler.backslotKey.wasPressed()) {
                    swapItems(client);
                }
            });
    }

    public static void swapItems(MinecraftClient client) {
        if (client == null || client.player == null) return;
        client.execute(() -> {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            ClientPlayNetworking.send(SWAP_PACKET, buf);
            Backslot.LOG(Text.translatable("log.backslot-flike.client_send_swap"));
        });
    }

}
