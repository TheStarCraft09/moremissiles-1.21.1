package dev.lumareth.moremissiles.utils.procedures;

import dev.lumareth.moremissiles.block.records.ControllerTargetData;
import dev.lumareth.moremissiles.block.states.HardpointType;
import dev.lumareth.moremissiles.block.states.Missiletype;
import net.mcreator.crustychunks.entity.RadarSpearMissileProjectileEntity;
import net.mcreator.crustychunks.entity.SeekerSpearMissileProjectileEntity;
import net.mcreator.crustychunks.entity.StrikeSpearProjectileEntity;
import net.mcreator.crustychunks.init.CrustyChunksModEntities;
import net.mcreator.crustychunks.init.CrustyChunksModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class WingtipHardpointProcedures {
    public static Missiletype reload(Missiletype currentMissileType, LivingEntity entity, InteractionHand interactionHand) {
    if (interactionHand != InteractionHand.MAIN_HAND){
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

    return Missiletype.EMPTY;
    }

    public static ItemStack returnDroppedItem(Missiletype missiletype) {
        if (missiletype == Missiletype.IR) {
            return new ItemStack(CrustyChunksModItems.SEEKER_SPEAR_ROCKET.get());
        } else if (missiletype == Missiletype.SARH) {
            return new ItemStack(CrustyChunksModItems.RADAR_SPEAR_MISSILE.get());
        } else if (missiletype == Missiletype.SACLOCS) {
            return new ItemStack(CrustyChunksModItems.STRIKE_SPEAR_MISSILE.get());
        } else {
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

    public static boolean getTriggerFromControlNode(BlockEntity controlNode, Level level, double Channel) {
        if (!level.isClientSide()) {
            String triggerKey = "Trigger" + Channel;
            double triggerValue = controlNode.getPersistentData().getDouble(triggerKey);
            if (triggerValue > 0.0D) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    public static double returnLaunchOffset(HardpointType hardpointType) {
        if (hardpointType == HardpointType.BOTTOM) {
            return -0.5;
        } else if (hardpointType == HardpointType.TOP) {
            return -1.5;
        } else {
            return 0;
        }
    }

    public static void fireSeekerSpear(Level level, BlockPos hardpointPos, HardpointType state, Vec3 facing) {
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            SeekerSpearMissileProjectileEntity missile = new SeekerSpearMissileProjectileEntity(CrustyChunksModEntities.SEEKER_SPEAR_MISSILE_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setKnockback(2);
            missile.setSilent(true);
            missile.setPierceLevel((byte) 10);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(state), hardpointPos.getZ() + 0.5D);

            double LaunchX = facing.x;
            double LaunchY = facing.y;
            double LaunchZ = facing.z;

            missile.shoot(LaunchX, LaunchY, LaunchZ, 4.0F, 2.0F);
            serverLevel.addFreshEntity(missile);
        }
    }

    public static void fireRadarSpear(Level level, BlockPos hardpointPos, HardpointType state, Vec3 facing){
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            RadarSpearMissileProjectileEntity missile = new RadarSpearMissileProjectileEntity(CrustyChunksModEntities.RADAR_SPEAR_MISSILE_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setKnockback(2);
            missile.setSilent(true);
            missile.setPierceLevel((byte) 10);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(state), hardpointPos.getZ() + 0.5D);

            double LaunchX = facing.x;
            double LaunchY = facing.y;
            double LaunchZ = facing.z;

            missile.getPersistentData().putDouble("LX", hardpointPos.getX());
            missile.getPersistentData().putDouble("LY", hardpointPos.getY());
            missile.getPersistentData().putDouble("LZ", hardpointPos.getZ());

            missile.shoot(LaunchX, LaunchY, LaunchZ, 4.0F, 0.0F);
            serverLevel.addFreshEntity(missile);

        }
    }
    public static void fireStrikeSpear(Level level, BlockPos hardpointPos, HardpointType state, Vec3 facing){
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            StrikeSpearProjectileEntity missile = new StrikeSpearProjectileEntity(CrustyChunksModEntities.STRIKE_SPEAR_PROJECTILE.get(), serverLevel);

            missile.setBaseDamage(10.0D);
            missile.setKnockback(2);
            missile.setSilent(true);
            missile.setPierceLevel((byte) 10);
            missile.setCritArrow(true);

            missile.setPos(hardpointPos.getX() + 0.5D, hardpointPos.getY() + returnLaunchOffset(state), hardpointPos.getZ() + 0.5D);

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
