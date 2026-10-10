package dev.lumareth.moremissiles.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(RadarLockPayload.TYPE, RadarLockPayload.STREAM_CODEC, RadarLockPayload::handle);
    }
}
