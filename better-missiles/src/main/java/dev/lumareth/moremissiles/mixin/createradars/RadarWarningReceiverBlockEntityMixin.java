package dev.lumareth.moremissiles.mixin.createradars;

import com.happysg.radar.block.arad.aradnetworks.RadarContactRegistry;
import com.happysg.radar.block.arad.rwr.RadarWarningReceiverBlockEntity;
import com.happysg.radar.registry.ModSounds;
import dev.lumareth.moremissiles.utils.MissileWarningRegistry;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Makes the RWR beep fast while a guided missile is inbound on its aircraft.
 */
@Mixin(value = RadarWarningReceiverBlockEntity.class, remap = false)
public abstract class RadarWarningReceiverBlockEntityMixin {
    // Ticks between beeps while a missile is inbound; Create Radars uses 31 for a plain lock
    @Unique
    private static final int more_missiles$MISSILE_BEEP_PERIOD = 5;

    @Shadow
    private int lockBeepTicks;

    @Unique
    private int more_missiles$missileBeepTicks = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void more_missiles$beepFastOnInboundMissile(CallbackInfo ci) {
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        SubLevelAccess ship = SableCompanion.INSTANCE.getContaining(serverLevel, self.getBlockPos());
        if (ship == null) {
            return;
        }
        UUID shipId = ship.getUniqueId();
        if (!MissileWarningRegistry.isInbound(serverLevel, shipId)) {
            more_missiles$missileBeepTicks = 0;
            return;
        }
        if (RadarContactRegistry.isLocked(serverLevel, shipId)) {
            // Create Radars beeps for the lock itself; just shorten the wait for the next beep
            if (lockBeepTicks > more_missiles$MISSILE_BEEP_PERIOD) {
                lockBeepTicks = more_missiles$MISSILE_BEEP_PERIOD;
            }
            return;
        }
        // An active-radar missile homing without any radar lock: Create Radars stays silent, so beep ourselves
        if (more_missiles$missileBeepTicks > 0) {
            more_missiles$missileBeepTicks--;
            return;
        }
        Vec3 worldPos = ship.logicalPose().transformPosition(Vec3.atCenterOf(self.getBlockPos()));
        serverLevel.playSound(null, worldPos.x, worldPos.y, worldPos.z, ModSounds.RWR_LOCK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        more_missiles$missileBeepTicks = more_missiles$MISSILE_BEEP_PERIOD;
    }
}
