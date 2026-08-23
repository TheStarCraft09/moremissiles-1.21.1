package dev.lumareth.moremissiles;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = WariumBetterMissiles.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = WariumBetterMissiles.MOD_ID, value = Dist.CLIENT)
public class WariumBetterMissilesClient {
    public WariumBetterMissilesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        WariumBetterMissiles.LOGGER.info("HELLO FROM CLIENT SETUP");
        WariumBetterMissiles.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
