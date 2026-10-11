package dev.lumareth.moremissiles.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Radar-guided missiles in flight, each tied to the control node guiding it. A missile counts as inbound on an
 * aircraft while it still exists and its control node is locked onto that aircraft. The missile reads its target
 * through the hardpoint from the node, so a node switching targets redirects the warning too.
 */
public final class MissileWarningRegistry {
    /**
     * Key in a control node's persistent data holding the UUID of the locked Sable sub-level
     */
    public static final String LOCKED_SHIP_KEY = "LockedShip";
    // Radar Spears fly at most 130 ticks; anything older is forgotten even if the entity somehow survives
    private static final long MAX_FLIGHT_TICKS = 200;

    private static final Map<Level, List<TrackedMissile>> MISSILES = new WeakHashMap<>();

    private record TrackedMissile(UUID missile, BlockPos controlPos, long launchedAt) {}

    private MissileWarningRegistry() {}

    public static void register(ServerLevel level, Entity missile, BlockPos controlPos) {
        MISSILES.computeIfAbsent(level, l -> new ArrayList<>())
                .add(new TrackedMissile(missile.getUUID(), controlPos.immutable(), level.getGameTime()));
    }

    public static boolean isInbound(ServerLevel level, UUID ship) {
        List<TrackedMissile> missiles = MISSILES.get(level);
        if (missiles == null) {
            return false;
        }
        long now = level.getGameTime();
        missiles.removeIf(tracked -> {
            Entity entity = level.getEntity(tracked.missile());
            return entity == null || entity.isRemoved() || now - tracked.launchedAt() > MAX_FLIGHT_TICKS;
        });
        String shipId = ship.toString();
        for (TrackedMissile tracked : missiles) {
            BlockEntity control = level.getBlockEntity(tracked.controlPos());
            if (control != null && shipId.equals(control.getPersistentData().getString(LOCKED_SHIP_KEY))) {
                return true;
            }
        }
        return false;
    }
}
