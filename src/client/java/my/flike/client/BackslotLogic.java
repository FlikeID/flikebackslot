package my.flike.client;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import io.netty.buffer.Unpooled;
import my.flike.Backslot;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

public class BackslotLogic {

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
            Backslot.LOG("Sent swap packet to server");
        });
    }

    public static ItemStack getBackItemStack(AbstractClientPlayerEntity player) {
        ItemStack backStack = ItemStack.EMPTY;
        // Получаем TrinketComponent через Trinkets API
        Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(player);
        if (compOpt.isEmpty()) return backStack;
        TrinketComponent comp = compOpt.get();
        // Извлекаем стаки из chest/back
        Map<String, TrinketInventory> chestGroup = comp.getInventory().get("chest");
        if (chestGroup == null) return backStack;
        TrinketInventory backInv = chestGroup.get("back"); // тип API: get(String)
        if (backInv == null) return backStack;
        backStack = backInv.getStack(0);
        return backStack;
    }

}
