package dev.lumareth.moremissiles.compat.createradars.block;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.block.RadarControlNodeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RadarsCompatModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<Block, Block> RADAR_CONTROL_NODE = BLOCKS.register("radar_control_node", () -> new RadarControlNodeBlock(Block.Properties.of().strength(1.0f).noOcclusion()));
}
