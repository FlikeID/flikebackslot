package my.flike;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Backslot implements ModInitializer {
    public static final String MOD_ID = "backslot-flike";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Backslot mod initializing");
        BackslotCompat.registerTrinketPredicate();
        BackslotLogic.register();
    }

    public static void LOG(String text, PlayerEntity player){
        LOG(text);
        player.sendMessage(Text.of(String.valueOf(text)), false);
    }

    public static void LOG(String text){
        LOGGER.info(text);
    }
}
