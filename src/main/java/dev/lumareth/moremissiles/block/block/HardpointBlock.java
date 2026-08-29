package dev.lumareth.moremissiles.block.block;

import com.mojang.serialization.MapCodec;
import dev.lumareth.moremissiles.block.entity.HardpointBlockEntity;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import dev.lumareth.moremissiles.block.states.ModBlockStates;
import dev.lumareth.moremissiles.utils.procedures.HardpointProcedures;
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
import org.jetbrains.annotations.Nullable;

public class HardpointBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final VoxelShape HITBOX = Block.box(0.0D, -1.0D, 0.0D, 16.0D, 11.0D, 16.0D);
    public static final EnumProperty<HardpointType> TYPE = ModBlockStates.HARDPOINT_TYPE;
    public static final EnumProperty<Missiletype> MISSILE_TYPE = ModBlockStates.MISSILE_TYPE;

    public HardpointBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TYPE, HardpointType.WING).setValue(MISSILE_TYPE, Missiletype.EMPTY));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HardpointBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TYPE, MISSILE_TYPE);
    }

    @Override
    @NotNull
    public VoxelShape getShape(BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return HITBOX;
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
            Missiletype updated = HardpointProcedures.reload(current, player, interactionHand);
            if (updated != current) {
                level.setBlock(blockPos, blockState.setValue(MISSILE_TYPE, updated), 3);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (lvl, pos, st, blockEntity) -> {
            if (!level.isClientSide()) {
                if (blockEntity instanceof HardpointBlockEntity be){
                    be.tick();
                }
            }
        };
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (blockState.getBlock() != newState.getBlock()) {
            ItemStack itemStack = HardpointProcedures.returnDroppedItem(blockState.getValue(MISSILE_TYPE));
            if (!itemStack.isEmpty()) {
                Block.popResource(level, blockPos, itemStack);
            }
            super.onRemove(blockState, level, blockPos, newState, isMoving);
        }
    }


}
