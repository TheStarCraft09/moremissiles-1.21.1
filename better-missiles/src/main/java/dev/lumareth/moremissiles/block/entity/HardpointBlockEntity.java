package dev.lumareth.moremissiles.block.entity;

import dev.lumareth.moremissiles.block.ModBlockEntities;
import dev.lumareth.moremissiles.block.records.ControllerTargetData;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import dev.lumareth.moremissiles.block.states.ModBlockStates;
import dev.lumareth.moremissiles.block.block.WingtipHardpointBlock;
import dev.lumareth.moremissiles.utils.MissileWarningRegistry;
import dev.lumareth.moremissiles.utils.procedures.HardpointProcedures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static dev.lumareth.moremissiles.block.states.ModBlockStates.MISSILE_TYPE;

public class HardpointBlockEntity extends BlockEntity {
    private BlockPos ControlPos;
    private Vec3 TargetPos;
    private Vec3 TargetVelocity;
    private boolean Trigger = false;
    private boolean wasControlTriggered = false;
    private double currentTarget = 0D;
    private double channel = 0D;
    private boolean was_powered = false;

    protected HardpointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public HardpointBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntities.WING_HARDPOINT.get(), pos, blockState);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ControlPos")) {
            this.ControlPos = NbtUtils.readBlockPos(tag, "ControlPos").orElse(null);
        }
        if (tag.contains("Channel")) {
            this.channel = tag.getDouble("Channel");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ControlPos != null) {
            tag.put("ControlPos", NbtUtils.writeBlockPos(this.ControlPos));
        }
        if (this.channel != 0D) {
            tag.putDouble("Channel", this.channel);
        }
    }

    public void tick() {
        CompoundTag data = this.getPersistentData();
        assert this.level != null;
        boolean powered = this.level.hasNeighborSignal(this.worldPosition);
        if (powered && !was_powered) {
            if (this.getBlockState().getValue(MISSILE_TYPE) != Missiletype.EMPTY) {
                fireMissile();
            } else {
                return;
            }
        }
        was_powered = powered;
        if (!data.contains("ControlX") || !data.contains("ControlY") || !data.contains("ControlZ")) {
            return;
        }
        double controlX = data.getDouble("ControlX");
        double controlY = data.getDouble("ControlY");
        double controlZ = data.getDouble("ControlZ");
        this.channel = data.getDouble("Channel");
        BlockState blockState = this.getBlockState();
        ControllerTargetData targetData;
        ControlPos = BlockPos.containing(controlX, controlY, controlZ);
        BlockEntity controlBlockEntity = level.getBlockEntity(ControlPos);
        if (controlBlockEntity != null) {
            currentTarget = controlBlockEntity.getPersistentData().getDouble("SelectedTarget");
        } else {
            currentTarget = 0D;
        }
        targetData = HardpointProcedures.getTargetDataFromControlNode(controlBlockEntity, level, currentTarget);
        Trigger = HardpointProcedures.getTriggerFromControlNode(controlBlockEntity, level, channel);
        TargetPos = targetData.position();
        TargetVelocity = targetData.velocity();
        data.putDouble("TargetX", TargetPos.x);
        data.putDouble("TargetY", TargetPos.y);
        data.putDouble("TargetZ", TargetPos.z);
        data.putDouble("MX", TargetVelocity.x);
        data.putDouble("MY", TargetVelocity.y);
        data.putDouble("MZ", TargetVelocity.z);
        setChanged();

        // One missile per trigger press: the control node holds the trigger for several ticks
        boolean risingEdge = Trigger && !wasControlTriggered;
        wasControlTriggered = Trigger;
        if (risingEdge) {
            if (this.getBlockState().getValue(MISSILE_TYPE) != Missiletype.EMPTY) {
                Entity radarGuidedMissile = fireMissile();
                // Lets a Radar Warning Receiver on the target know a missile is inbound
                if (radarGuidedMissile != null && level instanceof ServerLevel serverLevel) {
                    MissileWarningRegistry.register(serverLevel, radarGuidedMissile, ControlPos);
                }
            } else {
                return;
            }
        }

    }

    /**
     * @return the linked control node's block entity, or null if not linked or not loaded
     */
    @Nullable
    protected BlockEntity getControlBlockEntity() {
        return level == null || ControlPos == null ? null : level.getBlockEntity(ControlPos);
    }

    /**
     * @return the target position last received from the control node
     */
    protected Vec3 getTargetPos() {
        return TargetPos == null ? Vec3.ZERO : TargetPos;
    }

    /**
     * @return the fired missile if it is radar guided, otherwise null
     */
    @Nullable
    public Entity fireMissile() {
        BlockState blockState = this.getBlockState();
        Vec3 launchDirection = getLaunchDirection();
        Entity radarGuidedMissile = null;
        switch (blockState.getValue(MISSILE_TYPE)) {
            case IR -> {
                HardpointType hardpointType = blockState.getValue(ModBlockStates.HARDPOINT_TYPE);
                HardpointProcedures.fireSeekerSpear(level, this.worldPosition, hardpointType, launchDirection);
            }
            case SARH -> {
                HardpointType hardpointType = blockState.getValue(ModBlockStates.HARDPOINT_TYPE);
                radarGuidedMissile = HardpointProcedures.fireRadarSpear(level, this.worldPosition, hardpointType, launchDirection);
            }
            case SACLOCS -> {
                HardpointType hardpointType = blockState.getValue(ModBlockStates.HARDPOINT_TYPE);
                HardpointProcedures.fireStrikeSpear(level, this.worldPosition, hardpointType, launchDirection);
            }
        }
        level.setBlock(this.worldPosition, blockState.setValue(MISSILE_TYPE, Missiletype.EMPTY), Block.UPDATE_CLIENTS);
        return radarGuidedMissile;
    }

    public Vec3 getLaunchDirection() {
        Vec3 launchDirection = Vec3.ZERO;
        Direction direction =
                this.getBlockState().getValue(WingtipHardpointBlock.FACING);

        return switch (direction) {
            case NORTH -> new Vec3(0D, 0D, -1D);
            case SOUTH -> new Vec3(0D, 0D, 1D);
            case EAST -> new Vec3(1D, 0D, 0D);
            case WEST -> new Vec3(-1D, 0D, 0D);
            default -> Vec3.ZERO;
        };
    }
}
