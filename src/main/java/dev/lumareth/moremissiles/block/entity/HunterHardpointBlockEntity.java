package dev.lumareth.moremissiles.block.entity;

import dev.lumareth.moremissiles.block.ModBlockEntities;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.ModBlockStates;
import dev.lumareth.moremissiles.utils.procedures.HardpointProcedures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HunterHardpointBlockEntity extends HardpointBlockEntity{

    public HunterHardpointBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HUNTER_HARDPOINT.get(), pos, blockState);
    }

    @Override
    public void fireMissile() {
        BlockState blockState = this.getBlockState();
        Vec3 launchDirection = getLaunchDirection();
        HardpointType hardpointType = blockState.getValue(ModBlockStates.HARDPOINT_TYPE);
        HardpointProcedures.fireHunterSpear(level, this.worldPosition, hardpointType, launchDirection);
    }
}
