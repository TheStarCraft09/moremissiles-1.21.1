package dev.lumareth.moremissiles.item;


import dev.lumareth.moremissiles.WariumBetterMissiles;
import net.mcreator.crustychunks.init.CrustyChunksModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> WARIUM_MORE_MISSILES = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WariumBetterMissiles.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MORE_MISSILES_TAB = WARIUM_MORE_MISSILES.register("more_missiles_creative_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("item_groups." + WariumBetterMissiles.MOD_ID + ".more_missiles"))
            .icon(() -> CrustyChunksModItems.EMPTY_MISSILE_HARDPOINT.get().getDefaultInstance())
            .displayItems((params, output) -> {
            output.accept(ModItems.EMPTY_WINGTIP_MISSILE_HARDPOINT.get());
            })
            .build()
    );
}
