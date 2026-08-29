package dev.lumareth.moremissiles.compat.createradars;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlockEntities;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlocks;
import dev.lumareth.moremissiles.compat.createradars.item.RadarsCompatModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class CreateRadarsCompat {
    public static void registerIfPresent(IEventBus ModEventBus) {
        if (ModList.get().isLoaded("create") && ModList.get().isLoaded("create_radar")) {
            WariumBetterMissiles.LOGGER.info("Create and Create Radar are loaded.");
            RadarsCompatModItems.ITEMS.register(ModEventBus);
            RadarsCompatModBlocks.BLOCKS.register(ModEventBus);
            RadarsCompatModBlockEntities.BLOCK_ENTITIES.register(ModEventBus);
        }
    }
}
