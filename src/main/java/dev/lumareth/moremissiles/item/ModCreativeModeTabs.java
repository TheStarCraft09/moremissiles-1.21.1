package dev.lumareth.moremissiles.item;


import com.wariumce.init.CrustyChunksModItems;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.item.RadarsCompatModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> WARIUM_MORE_MISSILES = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WariumBetterMissiles.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MORE_MISSILES_TAB = WARIUM_MORE_MISSILES.register("more_missiles_creative_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("item_groups." + WariumBetterMissiles.MOD_ID + ".more_missiles"))
            .icon(() -> CrustyChunksModItems.EMPTY_MISSILE_HARDPOINT.get().getDefaultInstance())
            .displayItems((params, output) -> {
            output.accept(ModItems.EMPTY_WINGTIP_MISSILE_HARDPOINT.get());
            output.accept(ModItems.WING_HARDPOINT.get());
            output.accept(ModItems.DARK_WING_HARDPOINT.get());
            output.accept(ModItems.RADAR_TOOLKIT.get());
            output.accept(ModItems.HUNTER_MISSILE.get());
            output.accept(ModItems.EXTERNAL_HUNTER_MISSILE_HARDPOINT.get());
            if ((ModList.get().isLoaded("create") && ModList.get().isLoaded("create_radar"))) {
                output.accept(RadarsCompatModItems.RADAR_CONTROL_NODE.get());
            }
            })
            .build()
    );
}
