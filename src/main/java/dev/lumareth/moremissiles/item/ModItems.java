package dev.lumareth.moremissiles.item;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<Item, Item> EMPTY_WINGTIP_MISSILE_HARDPOINT = ITEMS.register("empty_wingtip_missile_hardpoint", () -> new BlockItem(ModBlocks.EMPTY_WINGTIP_MISSILE_HARDPOINT.get(), new Item.Properties()));
}
