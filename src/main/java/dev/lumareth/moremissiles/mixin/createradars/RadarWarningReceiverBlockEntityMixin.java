package dev.lumareth.moremissiles.mixin.createradars;

import com.happysg.radar.block.arad.aradnetworks.RadarContactRegistry;
import com.happysg.radar.block.arad.rwr.RadarWarningReceiverBlockEntity;
import dev.lumareth.moremissiles.utils.MissileWarningRegistry;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the RWR's lock beep fast while a radar-guided missile is inbound on its aircraft.
 */
@Mixin(value = RadarWarningReceiverBlockEntity.class, remap = false)
public abstract class RadarWarningReceiverBlockEntityMixin {
    // Ticks between lock beeps while a missile is inbound; Create Radars uses 31 for a plain lock
    private static final int more_missiles$MISSILE_BEEP_PERIOD = 5;

    @Shadow
    private int lockBeepTicks;

    @Inject(method = "tick", at = @At("TAIL"))
    private void more_missiles$beepFastOnInboundMissile(CallbackInfo ci) {
        // Only matters right after a lock beep, when the RWR waits the long period for the next one
        if (lockBeepTicks <= more_missiles$MISSILE_BEEP_PERIOD) {
            return;
        }
        BlockEntity self = (BlockEntity) (Object) this;
        if (!(self.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        SubLevelAccess ship = SableCompanion.INSTANCE.getContaining(serverLevel, self.getBlockPos());
        if (ship == null || !RadarContactRegistry.isLocked(serverLevel, ship.getUniqueId())) {
            return;
        }
        if (MissileWarningRegistry.isInbound(serverLevel, ship.getUniqueId())) {
            lockBeepTicks = more_missiles$MISSILE_BEEP_PERIOD;
        }
    }
}
