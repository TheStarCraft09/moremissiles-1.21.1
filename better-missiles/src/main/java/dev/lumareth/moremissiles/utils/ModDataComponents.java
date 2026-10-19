package dev.lumareth.moremissiles.utils;

import dev.lumareth.moremissiles.utils.itemstates.ToolkitMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "moremissiles");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> LINKER_POS = register("linker_pos", builder -> builder.persistent(BlockPos.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ToolkitMode>> MODE = register("toolkit_mode", builder -> builder.persistent(ToolkitMode.CODEC));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENTS.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }
}
