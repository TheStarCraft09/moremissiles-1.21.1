package dev.lumareth.moremissiles.client;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.item.ModItems;
import dev.lumareth.moremissiles.utils.ModDataComponents;
import dev.lumareth.moremissiles.utils.itemstates.ToolkitMode;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(modid = WariumBetterMissiles.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModItemProperties {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.RADAR_TOOLKIT.get(),
                    ResourceLocation.fromNamespaceAndPath(WariumBetterMissiles.MOD_ID, "mode"),
                    (stack, level, entity, seed) -> {
                        ToolkitMode mode = stack.getOrDefault(ModDataComponents.MODE.get(), ToolkitMode.LINKING);
                        return mode == ToolkitMode.CONFIGURING ? 1.0F : 0.0F;
                    }
            );
        });
    }
}