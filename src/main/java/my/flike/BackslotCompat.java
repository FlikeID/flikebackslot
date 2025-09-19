package my.flike;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;

public class BackslotCompat {
    public static void registerTrinketPredicate() {
        TrinketsApi.registerTrinketPredicate(new Identifier(Backslot.MOD_ID, "backslot"), (stack, slot, entity) -> {
            Item item = stack.getItem();
            return isBackCompatible(item) ? TriState.TRUE : TriState.DEFAULT;
        });
    }

    public static boolean isBackCompatible(Item item) {

        return item instanceof ShieldItem ||
               item instanceof TridentItem ||
               item instanceof RangedWeaponItem ||
               item instanceof ToolItem;
    }
}
