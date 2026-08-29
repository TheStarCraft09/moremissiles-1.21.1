package dev.lumareth.moremissiles.compat.createradars.block;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RadarsCompatModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> RADAR_CONTROL_NODE =
            BLOCK_ENTITIES.register("radar_control_node",
                    () -> BlockEntityType.Builder.of(
                            null,
                            RadarsCompatModBlocks.RADAR_CONTROL_NODE.get()
                    ).build(null));
}
