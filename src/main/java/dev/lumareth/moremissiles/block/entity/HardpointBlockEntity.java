package dev.lumareth.moremissiles.block.entity;

import dev.lumareth.moremissiles.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HardpointBlockEntity extends BlockEntity {
    public HardpointBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WING_HARDPOINT.get(), pos, blockState);
    }

    public void tick() {
    }
}
