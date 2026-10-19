package dev.lumareth.moremissiles.network;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent every tick by a Radar Control Node with a lock, to players near it. The client shows a marker at the target.
 */
public record RadarLockPayload(BlockPos node, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<RadarLockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WariumBetterMissiles.MOD_ID, "radar_lock"));

    public static final StreamCodec<ByteBuf, RadarLockPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RadarLockPayload::node,
            ByteBufCodecs.DOUBLE, RadarLockPayload::x,
            ByteBufCodecs.DOUBLE, RadarLockPayload::y,
            ByteBufCodecs.DOUBLE, RadarLockPayload::z,
            RadarLockPayload::new
    );

    public RadarLockPayload(BlockPos node, Vec3 target) {
        this(node, target.x, target.y, target.z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadarLockPayload payload, IPayloadContext context) {
        RadarLockMarkers.update(payload.node(), new Vec3(payload.x(), payload.y(), payload.z()),
                context.player().level().getGameTime());
    }
}
