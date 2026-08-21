package dev.lumareth.moremissiles.block;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.block.entity.WingtipHardpointBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WingtipHardpointBlockEntity>> WINGTIP_HARDPOINT =
            BLOCK_ENTITIES.register("wingtip_hardpoint",
                    () -> BlockEntityType.Builder.of(
                            WingtipHardpointBlockEntity::new,
                            ModBlocks.EMPTY_WINGTIP_MISSILE_HARDPOINT.get()
                    ).build(null));
}