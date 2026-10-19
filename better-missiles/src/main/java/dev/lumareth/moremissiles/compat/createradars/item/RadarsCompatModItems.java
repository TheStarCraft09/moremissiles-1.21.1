package dev.lumareth.moremissiles.compat.createradars.item;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RadarsCompatModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<Item, Item> RADAR_CONTROL_NODE = ITEMS.register("radar_control_node", () -> new BlockItem(RadarsCompatModBlocks.RADAR_CONTROL_NODE.get(), new Item.Properties()));
}
