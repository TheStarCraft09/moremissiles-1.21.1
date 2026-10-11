package dev.lumareth.moremissiles.utils.procedures;

import com.wariumce.init.CrustyChunksModItems;
import dev.lumareth.moremissiles.block.records.ControllerTargetData;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import com.wariumce.entity.RadarSpearMissileProjectileEntity;
import com.wariumce.entity.SeekerSpearMissileProjectileEntity;
import com.wariumce.entity.StrikeSpearProjectileEntity;
import com.wariumce.init.CrustyChunksModEntities;
import dev.lumareth.moremissiles.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HardpointProcedures {
    public static Missiletype reload(Missiletype currentMissileType, LivingEntity entity, InteractionHand interactionHand) {
        if (interactionHand != InteractionHand.MAIN_HAND) {
            return currentMissileType;
        }

    if (currentMissileType == Missiletype.EMPTY) {
        ItemStack itemStack = entity.getItemInHand(interactionHand);
        if (itemStack.is(CrustyChunksModItems.SEEKER_SPEAR_ROCKET.get())){
            entity.setItemInHand(interactionHand, ItemStack.EMPTY);
            return Missiletype.IR;
        } else if (itemStack.is(CrustyChunksModItems.RADAR_SPEAR_MISSILE.get())) {
            entity.setItemInHand(interactionHand, ItemStack.EMPTY);
            return Missiletype.SARH;
        } else if (itemStack.is(CrustyChunksModItems.STRIKE_SPEAR_MISSILE.get())) {
            entity.setItemInHand(interactionHand, ItemStack.EMPTY);
            return Missiletype.SACLOCS;
        }
    } else {
        if (entity.getItemInHand(interactionHand).isEmpty()) {
            if (interactionHand == InteractionHand.MAIN_HAND) {
                if (currentMissileType == Missiletype.IR) {
                    entity.setItemInHand(interactionHand, new ItemStack(CrustyChunksModItems.SEEKER_SPEAR_ROCKET.get()));
                } else if (currentMissileType == Missiletype.SACLOCS) {
                    entity.setItemInHand(interactionHand, new ItemStack(CrustyChunksModItems.STRIKE_SPEAR_MISSILE.get()));
                } else if (currentMissileType == Missiletype.SARH) {
                    entity.setItemInHand(interactionHand, new ItemStack(CrustyChunksModItems.RADAR_SPEAR_MISSILE.get()));
                }
                return Missiletype.EMPTY;
            }
        } else {
            return currentMissileType;
        }

    }

    //default return if for some reason the above breaks
    return Missiletype.EMPTY;
    }

    public static Missiletype reloadHunter(Missiletype currentMissileType, LivingEntity entity, InteractionHand interactionHand) {
        if (interactionHand != InteractionHand.MAIN_HAND) {
            return currentMissileType;}
        if (currentMissileType == Missiletype.EMPTY && entity.getItemInHand(interactionHand).is(ModItems.HUNTER_MISSILE.get())) {
            entity.setItemInHand(interactionHand, ItemStack.EMPTY);
            return Missiletype.HUNTER;
        } else if(currentMissileType == Missiletype.HUNTER && entity.getItemInHand(interactionHand).isEmpty()) {
            entity.setItemInHand(interactionHand, new ItemStack(ModItems.HUNTER_MISSILE.get()));
            return Missiletype.EMPTY;
        } else {
            return currentMissileType;
        }

    }

    public static void fireHunterSpear(Level level, BlockPos worldPosition, HardpointType hardpointType, Vec3 launchDirection) {
        // Implement the logic to fire the Hunter Spear
    }

    @Deprecated
    public static void fireMissile(Level level, BlockPos hardpointPos, HardpointType state, Vec3 facing, Missiletype missiletype) {
        if (missiletype == Missiletype.IR) {
            fireSeekerSpear(level, hardpointPos, state, facing);
        } else if (missiletype == Missiletype.SARH) {
            fireRadarSpear(level, hardpointPos, state, facing);
        } else if (missiletype == Missiletype.SACLOCS) {
            fireStrikeSpear(level, hardpointPos, state, facing);
        }
    }

    public static ItemStack returnDroppedItem(Missiletype missiletype) {
        if (missiletype == Missiletype.IR) {
            return new ItemStack(CrustyChunksModItems.SEEKER_SPEAR_ROCKET.get());
        } else if (missiletype == Missiletype.SARH) {
            return new ItemStack(CrustyChunksModItems.RADAR_SPEAR_MISSILE.get());
        } else if (missiletype == Missiletype.SACLOCS) {
            return new ItemStack(CrustyChunksModItems.STRIKE_SPEAR_MISSILE.get());
        } else if (missiletype == Missiletype.HUNTER) {
            return new ItemStack(ModItems.HUNTER_MISSILE.get());
        }
         else {

            return ItemStack.EMPTY;
        }
    }

    public static ControllerTargetData getTargetDataFromControlNode(BlockEntity controleNodeBlockEntity, Level level, double target) {
        if (!level.isClientSide) {
            if (controleNodeBlockEntity != null) {
                Vec3 targetPos = Vec3.ZERO;
                Vec3 targetVelocity = Vec3.ZERO;
                BlockEntity bE = controleNodeBlockEntity;
                double TargetX = bE.getPersistentData().getDouble("TargetX" + target);
                double TargetY = bE.getPersistentData().getDouble("TargetY" + target);
                double TargetZ = bE.getPersistentData().getDouble("TargetZ" + target);
                targetPos = new Vec3(TargetX, TargetY, TargetZ);
                double VelocityX = bE.getPersistentData().getDouble("TMX" + target);
                double VelocityY = bE.getPersistentData().getDouble("TMY" + target);
                double VelocityZ = bE.getPersistentData().getDouble("TMZ" + target);
                targetVelocity = new Vec3(VelocityX, VelocityY, VelocityZ);
                return new ControllerTargetData(targetPos, targetVelocity);
            } else {
                return new ControllerTargetData(Vec3.ZERO, Vec3.ZERO);
            }
        } else {
            return new ControllerTargetData(Vec3.ZERO, Vec3.ZERO);
        }
    }



    /**
     * Reads "Trigger" + channel (e.g. "Trigger1.0") like WariumCE does. The control block owns the trigger and
     * resets it itself, so this only reads; callers fire on the rising edge.
     */
    public static boolean getTriggerFromControlNode(
            BlockEntity controlNode,
            Level level,
            double channel
    ) {
        if (level.isClientSide() || controlNode == null) {
            return false;
        }

        return controlNode.getPersistentData().getDouble("Trigger" + channel) > 0.0D;
    }



    public static double returnLaunchOffset(HardpointType hardpointType) {
        if (hardpointType == HardpointType.BOTTOM) {
            return -0.5;
        } else if (hardpointType == HardpointType.TOP) {
            return -1;
        } else if (hardpointType == HardpointType.WING) {
            return -0.75;
        } else if (hardpointType == HardpointType.INTERNAL) {
            return -1.5;
        } else {
            return 0;
        }
    }

    public static void fireSeekerSpear(Level level, BlockPos hardpointPos, HardpointType type, Vec3 facing) {
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            SeekerSpearMissileProjectileEntity missile = new SeekerSpearMissileProjectileEntity(CrustyChunksModEntities.SEEKER_SPEAR_MISSILE_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setSilent(true);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(type), hardpointPos.getZ() + 0.5D);

            double LaunchX = facing.x;
            double LaunchY = facing.y;
            double LaunchZ = facing.z;

            missile.shoot(LaunchX, LaunchY, LaunchZ, 4.0F, 2.0F);
            serverLevel.addFreshEntity(missile);
        }
    }

    /**
     * @return the spawned missile, or null on the client
     */
    @Nullable
    public static Entity fireRadarSpear(Level level, BlockPos hardpointPos, HardpointType type, Vec3 facing){
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            RadarSpearMissileProjectileEntity missile = new RadarSpearMissileProjectileEntity(CrustyChunksModEntities.RADAR_SPEAR_MISSILE_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setSilent(true);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(type), hardpointPos.getZ() + 0.5D);

            double LaunchX = facing.x;
            double LaunchY = facing.y;
            double LaunchZ = facing.z;

            missile.getPersistentData().putDouble("LX", hardpointPos.getX());
            missile.getPersistentData().putDouble("LY", hardpointPos.getY());
            missile.getPersistentData().putDouble("LZ", hardpointPos.getZ());

            missile.shoot(LaunchX, LaunchY, LaunchZ, 4.0F, 0.0F);
            serverLevel.addFreshEntity(missile);
            return missile;
        }
        return null;
    }
    public static void fireStrikeSpear(Level level, BlockPos hardpointPos, HardpointType type, Vec3 facing){
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            StrikeSpearProjectileEntity missile = new StrikeSpearProjectileEntity(CrustyChunksModEntities.STRIKE_SPEAR_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setSilent(true);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(type), hardpointPos.getZ() + 0.5D);

            double LaunchX = facing.x;
            double LaunchY = facing.y;
            double LaunchZ = facing.z;

            missile.getPersistentData().putDouble("LX", hardpointPos.getX());
            missile.getPersistentData().putDouble("LY", hardpointPos.getY());
            missile.getPersistentData().putDouble("LZ", hardpointPos.getZ());

            missile.shoot(LaunchX, LaunchY, LaunchZ, 3.0F, 0.0F);
            serverLevel.addFreshEntity(missile);

        }
    }

}
