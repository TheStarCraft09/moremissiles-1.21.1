package dev.lumareth.moremissiles.compat.createradars.client;

import dev.lumareth.moremissiles.compat.createradars.menu.RadarsCompatModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-only setup for the Create Radars compat. Only called on the client and only when the compat is active.
 */
public class RadarsCompatClient {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RadarsCompatClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RadarsCompatModMenuTypes.RADAR_CONTROL_NODE.get(), RadarControlNodeScreen::new);
    }
}
