package dev.lumareth.moremissiles.utils;

import dev.lumareth.moremissiles.entity.HunterSpearMissileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Guided missiles in flight, used to warn the targeted aircraft. Two kinds:
 * <ul>
 *     <li>Semi-active (Radar Spear): tied to the control node guiding it. Inbound on an aircraft while the missile
 *     exists and the node is locked onto that aircraft. The missile reads its target through the hardpoint from the
 *     node, so a node switching targets redirects the warning too.</li>
 *     <li>Active (Hunter Spear): carries its own target. Inbound while its seeker is homing on that aircraft.</li>
 * </ul>
 */
public final class MissileWarningRegistry {
    /**
     * Key in a control node's persistent data holding the UUID of the locked Sable sub-level
     */
    public static final String LOCKED_SHIP_KEY = "LockedShip";
    // Longer than any missile's lifetime; anything older is forgotten even if the entity somehow survives
    private static final long MAX_FLIGHT_TICKS = 400;

    private static final Map<Level, List<TrackedMissile>> MISSILES = new WeakHashMap<>();

    /**
     * @param controlPos the guiding control node, or null for missiles with their own seeker
     */
    private record TrackedMissile(UUID missile, @Nullable BlockPos controlPos, long launchedAt) {}

    private MissileWarningRegistry() {}

    public static void register(ServerLevel level, Entity missile, BlockPos controlPos) {
        add(level, new TrackedMissile(missile.getUUID(), controlPos.immutable(), level.getGameTime()));
    }

    public static void registerSelfGuided(ServerLevel level, HunterSpearMissileEntity missile) {
        add(level, new TrackedMissile(missile.getUUID(), null, level.getGameTime()));
    }

    private static void add(ServerLevel level, TrackedMissile tracked) {
        MISSILES.computeIfAbsent(level, l -> new ArrayList<>()).add(tracked);
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
            if (tracked.controlPos() == null) {
                if (level.getEntity(tracked.missile()) instanceof HunterSpearMissileEntity hunter && ship.equals(hunter.getGuidedTarget())) {
                    return true;
                }
                continue;
            }
            BlockEntity control = level.getBlockEntity(tracked.controlPos());
            if (control != null && shipId.equals(control.getPersistentData().getString(LOCKED_SHIP_KEY))) {
                return true;
            }
        }
        return false;
    }
}
