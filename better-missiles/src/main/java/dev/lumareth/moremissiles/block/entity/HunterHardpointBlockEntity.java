package dev.lumareth.moremissiles.block.entity;

import dev.lumareth.moremissiles.block.ModBlockEntities;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import dev.lumareth.moremissiles.block.states.ModBlockStates;
import dev.lumareth.moremissiles.utils.MissileWarningRegistry;
import dev.lumareth.moremissiles.utils.procedures.HardpointProcedures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import static dev.lumareth.moremissiles.block.states.ModBlockStates.MISSILE_TYPE;

public class HunterHardpointBlockEntity extends HardpointBlockEntity{

    public HunterHardpointBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HUNTER_HARDPOINT.get(), pos, blockState);
    }

    /**
     * Hands the control node's current lock to the missile, which guides itself from then on.
     *
     * @return always null: the Hunter registers itself for Radar Warning Receivers
     */
    @Override
    public Entity fireMissile() {
        BlockState blockState = this.getBlockState();
        if (blockState.getValue(MISSILE_TYPE) != Missiletype.HUNTER) {
            return null;
        }
        UUID target = null;
        Vec3 targetPos = Vec3.ZERO;
        BlockEntity control = getControlBlockEntity();
        if (control != null) {
            String lockedShip = control.getPersistentData().getString(MissileWarningRegistry.LOCKED_SHIP_KEY);
            if (!lockedShip.isEmpty()) {
                target = UUID.fromString(lockedShip);
                targetPos = getTargetPos();
            }
        }
        Vec3 launchDirection = getLaunchDirection();
        HardpointType hardpointType = blockState.getValue(ModBlockStates.HARDPOINT_TYPE);
        HardpointProcedures.fireHunterSpear(level, this.worldPosition, hardpointType, launchDirection, target, targetPos);
        level.setBlock(this.worldPosition, blockState.setValue(MISSILE_TYPE, Missiletype.EMPTY), Block.UPDATE_CLIENTS);
        return null;
    }
}
