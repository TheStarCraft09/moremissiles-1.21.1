package dev.lumareth.moremissiles.compat.createradars.menu;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RadarsCompatModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, WariumBetterMissiles.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<RadarControlNodeMenu>> RADAR_CONTROL_NODE = MENU_TYPES.register(
            "radar_control_node",
            () -> IMenuTypeExtension.create((id, inv, buf) -> new RadarControlNodeMenu(RadarsCompatModMenuTypes.RADAR_CONTROL_NODE.get(), id, inv, buf))
    );
}
