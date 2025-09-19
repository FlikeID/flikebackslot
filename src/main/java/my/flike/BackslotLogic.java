package my.flike;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

import static my.flike.BackslotCompat.isBackCompatible;

public class BackslotLogic {
    private static final TagKey<Item> BACK_SLOT_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("trinkets", "chest/back"));

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(Backslot.MOD_ID,"flike_backslot_swap"),
                (server, player, networkHandler, buf, sender) -> {
                    // Выполняем на серверном потоке
                    server.execute(() -> handleSwapToBack(player));
                }
        );
    }
    private static void handleSwapToBack(ServerPlayerEntity player) {
        // 1) берем выбранный слот игрока (hotbar index)
        int selected = player.getInventory().selectedSlot;
        ItemStack stack = player.getInventory().getStack(selected);

        // 2) проверим, допустим ли предмет для слота chest/back
        Optional<TrinketComponent> maybeComp = TrinketsApi.getTrinketComponent(player);
        if (maybeComp.isEmpty()) return;
        TrinketComponent comp = maybeComp.get();

        // получить инвентарь chest->back
        Map<String, TrinketInventory> chestGroup = comp.getInventory().get("chest");
        if (chestGroup == null) return;
        TrinketInventory backInv = chestGroup.get("back"); // тип API: get(String)
        if (backInv == null) return;

        //Если в активном слоте нет предмета, то пропустим проверку
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            boolean allowedByCode = isBackCompatible(item);
            boolean allowedByTag = stack.isIn(BACK_SLOT_TAG);


            if (!allowedByCode && !allowedByTag) {
                // не разрешён — ничего не делаем
                return;
            }
        }
        // у нас слот один: индекс 0
        int slotIndex = 0;
        ItemStack target = backInv.getStack(slotIndex);

        // меняем предметы местами
        player.getInventory().setStack(selected, target.copy());
        backInv.setStack(slotIndex, stack);


        // Обновить игрока: markDirty/обновить инвентарь
        player.playerScreenHandler.syncState();
    }
}
