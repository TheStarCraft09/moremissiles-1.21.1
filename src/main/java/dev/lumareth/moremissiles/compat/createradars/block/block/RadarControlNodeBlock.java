package dev.lumareth.moremissiles.compat.createradars.block.block;

import com.mojang.serialization.MapCodec;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlockEntities;
import dev.lumareth.moremissiles.compat.createradars.block.entities.RadarControlNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;



public class RadarControlNodeBlock extends BaseEntityBlock {
    public static final MapCodec<RadarControlNodeBlock> CODEC =
            simpleCodec(RadarControlNodeBlock::new);

    public static final DirectionProperty FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    public RadarControlNodeBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                defaultBlockState().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected MapCodec<RadarControlNodeBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new RadarControlNodeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof RadarControlNodeBlockEntity radarControlNode) {
                radarControlNode.tick();
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }
}
