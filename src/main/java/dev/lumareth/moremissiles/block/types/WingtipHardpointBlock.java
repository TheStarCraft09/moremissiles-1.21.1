package dev.lumareth.moremissiles.block.types;

import com.mojang.serialization.MapCodec;
import dev.lumareth.moremissiles.block.entity.WingtipHardpointBlockEntity;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import dev.lumareth.moremissiles.block.states.ModBlockStates;
import dev.lumareth.moremissiles.utils.procedures.WingtipHardpointProcedures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WingtipHardpointBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final VoxelShape BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    public static final VoxelShape TOP = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public static final EnumProperty<HardpointType> TYPE = ModBlockStates.HARDPOINT_TYPE;
    public static final EnumProperty<Missiletype> MISSILE_TYPE = ModBlockStates.MISSILE_TYPE;


    public WingtipHardpointBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, HardpointType.BOTTOM).setValue(MISSILE_TYPE, Missiletype.EMPTY));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, MISSILE_TYPE);
    }

    @NotNull
    public VoxelShape getShape(BlockState blockState,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
        HardpointType hardpointType = blockState.getValue(TYPE);
        return switch (hardpointType) {
            case BOTTOM -> BOTTOM;
            case TOP -> TOP;
        };
    }

    @Override
    public boolean canConnectRedstone(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            Missiletype current = blockState.getValue(MISSILE_TYPE);
            Missiletype updated = WingtipHardpointProcedures.reload(current, player, interactionHand);
            if (updated != current) {
                level.setBlock(blockPos, blockState.setValue(MISSILE_TYPE, updated), 3);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState1 = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(TYPE, HardpointType.BOTTOM).setValue(MISSILE_TYPE, Missiletype.EMPTY);
        Direction direction = context.getClickedFace();
        return direction != Direction.DOWN && (direction == Direction.UP || !(context.getClickLocation().y - (double)blockPos.getY() > 0.5D)) ? blockState1: blockState1.setValue(TYPE,  HardpointType.TOP).setValue(MISSILE_TYPE, Missiletype.EMPTY);
    }

    @Override
    @Nullable
    public <T extends BlockEntity>BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, st, blockEntity) -> {
            if (!level.isClientSide()) {
                if (blockEntity instanceof WingtipHardpointBlockEntity be){
                    be.tick();
                }
            } else {
                return;
            }
        };
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (blockState.getBlock() != newState.getBlock()) {
            ItemStack itemStack = WingtipHardpointProcedures.returnDroppedItem(blockState.getValue(MISSILE_TYPE));
            if (!itemStack.isEmpty()) {
                Block.popResource(level, blockPos, itemStack);
            }
            super.onRemove(blockState, level, blockPos, newState, isMoving);
        }
    }



    @Override
    public @org.jetbrains.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WingtipHardpointBlockEntity(pos, state);
    }
}
