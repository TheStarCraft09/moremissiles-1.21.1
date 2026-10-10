package dev.lumareth.moremissiles.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side store of the locked targets reported by nearby Radar Control Nodes, one per node.
 * A marker expires when its node stops reporting (lock lost or player out of range).
 */
public final class RadarLockMarkers {
    private static final long EXPIRY_TICKS = 10;
    private static final Map<BlockPos, Marker> MARKERS = new HashMap<>();

    private record Marker(Vec3 target, long receivedAt) {}

    private RadarLockMarkers() {}

    public static void update(BlockPos node, Vec3 target, long gameTime) {
        MARKERS.put(node, new Marker(target, gameTime));
    }

    public static List<Vec3> getActive(long gameTime) {
        MARKERS.values().removeIf(marker -> gameTime - marker.receivedAt() > EXPIRY_TICKS || gameTime < marker.receivedAt());
        return MARKERS.values().stream().map(Marker::target).toList();
    }
}
