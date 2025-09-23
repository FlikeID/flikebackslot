package my.flike;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

import static my.flike.BackslotCompat.isBackCompatible;

public class BackSlotLogic {
    private static final TagKey<Item> BACK_SLOT_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("trinkets", "chest/back"));

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(Backslot.MOD_ID,"flike_backslot_swap"),
                (server, player, networkHandler, buf, sender) -> {
                    // Выполняем на серверном потоке
                    server.execute(() -> handleSwapToBack(player));
                }
        );
    }

    @Nullable
    public static TrinketInventory getBackSlot(PlayerEntity player) {
        // Получаем TrinketComponent через Trinkets API
        Optional<TrinketComponent> compOpt = TrinketsApi.getTrinketComponent(player);
        if (compOpt.isEmpty()) return null;
        TrinketComponent comp = compOpt.get();
        // Извлекаем chest/back
        Map<String, TrinketInventory> chestGroup = comp.getInventory().get("chest");
        if (chestGroup == null) return null;
        return chestGroup.get("back");
    }

    public static ItemStack getBackItemStack(TrinketInventory backInv) {
        if (backInv == null) return ItemStack.EMPTY;
        ItemStack backStack = backInv.getStack(0);
        return (backStack == null) ? ItemStack.EMPTY : backStack;
    }

    public static ItemStack getBackItemStack(PlayerEntity player) {
        TrinketInventory backInv = getBackSlot(player);
        return getBackItemStack(backInv);
    }

    private static void handleSwapToBack(ServerPlayerEntity player) {
        // Берём слот спины
        TrinketInventory backInv = getBackSlot(player);
        if (backInv == null) return;
        Hand hand = Hand.MAIN_HAND;

        // Копируем стак в руке
        ItemStack held = player.getStackInHand(hand);
        // Копируем стак за спиной
        ItemStack back = BackSlotLogic.getBackItemStack(backInv);

        //Если в активном слоте нет предмета, то пропустим проверку
        if (!held.isEmpty()) {
            Item item = held.getItem();
            //проверяем через код
            boolean allowedByCode = isBackCompatible(item);
            //проверяем через тег
            boolean allowedByTag = held.isIn(BACK_SLOT_TAG);

            // нельзя по коду и нельзя по тегу
            if (!allowedByCode && !allowedByTag) {
                // тогда ищем свободный слот в инвентаре
                int freeSlot = player.getInventory().getEmptySlot();
                if (freeSlot == -1) {
                    // нет места — можно уведомить игрока (чат/звук) или просто выйти
                    player.sendMessage(Text.translatable("msg.backslot-flike.hand_occupied"), true);
                    return;
                }

                // Кладём копию в пустой слот. Работаем с копией из-за ссылочной структуры объектов
                player.getInventory().setStack(freeSlot, held.copy());
                // Предмет в руке заменяем на пустой стак
                held = ItemStack.EMPTY;
            }
        }

        // Предмет из-за спины кладём в руку
        player.setStackInHand(hand, back.copy());
        // Предмет из руки кладём за спину
        backInv.setStack(0, held.copy());

        // Обновить инвентарь игрока
        player.playerScreenHandler.syncState();
    }
}
