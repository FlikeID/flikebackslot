package my.flike;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Backslot implements ModInitializer {
    public static final String MOD_ID = "backslot-flike";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Backslot mod initializing");
        BackslotCompat.registerTrinketPredicate();
        BackSlotLogic.register();
    }

    public static String getModVersion() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(ModContainer::getMetadata)
                .map(m -> m.getVersion().getFriendlyString())
                .orElse("unknown");
    }
    public static String getModAuthors() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(ModContainer::getMetadata)
                .map(metadata -> {
                    // ModMetadata#getAuthors возвращает Collection<ModMetadata.Person>
                    List<String> names = metadata.getAuthors().stream()
                            .map(person -> {
                                String name = person.getName();
                                return (name == null || name.isEmpty()) ? null : name;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // fallback: попробуем Contributors (если нет authors)
                    if (names.isEmpty()) {
                        names = metadata.getContributors().stream()
                                .map(person -> {
                                    String name = person.getName();
                                    return (name == null || name.isEmpty()) ? null : name;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    }

                    return names.isEmpty() ? "unknown" : String.join(", ", names);
                })
                .orElse("unknown");
    }

    public static String getModDescription() {
        String str = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(ModContainer::getMetadata)
                .map(metadata -> {
                    try {
                        String desc = metadata.getDescription();
                        if (desc == null || desc.trim().isEmpty()) return "No description";
                        return desc.trim();
                    } catch (Exception e) {
                        // defensive: if description is not present or conversion fails
                        return "No description";
                    }
                })
                .orElse("No description");
        return str.split("\\r?\\n", 2)[0];
    }

    public static String getModContact(String key) {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(ModContainer::getMetadata)
                .map(metadata -> {
                    try {
                        // ModMetadata#getContact возвращает Map-like структуру; пробуем получить несколько возможных ключей
                        return metadata.getContact().get(key).orElse("No sources");
                    } catch (Exception ignored) {}
                    return "No sources";
                })
                .orElse("No sources");
    }


    @SuppressWarnings("unused")
    public static void LOG(String text, PlayerEntity player){
        LOG(text);
        player.sendMessage(Text.of(String.valueOf(text)), false);
    }

    public static void LOG(String text){
        LOGGER.info(text);
    }
}
