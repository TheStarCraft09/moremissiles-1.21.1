package dev.lumareth.moremissiles.block.block;

import dev.lumareth.moremissiles.block.entity.HardpointBlockEntity;
import dev.lumareth.moremissiles.block.entity.HunterHardpointBlockEntity;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import dev.lumareth.moremissiles.utils.procedures.HardpointProcedures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExternalHunterMissileHardpoint extends HardpointBlock implements EntityBlock {
    public static final VoxelShape HITBOX = Block.box(0.0D, -1.0D, 0.0D, 16.0D, 16.0D, 16.0D);



    public ExternalHunterMissileHardpoint(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TYPE, HardpointType.EXTERNAL).setValue(MISSILE_TYPE, Missiletype.EMPTY));
    }
    @NotNull
    @Override
    public VoxelShape getShape(BlockState blockState, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return HITBOX;
    }


    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            Missiletype current = blockState.getValue(MISSILE_TYPE);
            Missiletype updated = HardpointProcedures.reloadHunter(current, player, interactionHand);
            if (updated != current) {
                level.setBlock(blockPos, blockState.setValue(MISSILE_TYPE, updated), 3);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HunterHardpointBlockEntity(blockPos, blockState);
    }

}
