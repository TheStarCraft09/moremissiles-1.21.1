package dev.lumareth.moremissiles.block;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.block.types.WingtipHardpointBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<Block, Block> EMPTY_WINGTIP_MISSILE_HARDPOINT = BLOCKS.register("empty_wingtip_missile_hardpoint", () -> new WingtipHardpointBlock(Block.Properties.of().strength(1.0f).noOcclusion()));
}
