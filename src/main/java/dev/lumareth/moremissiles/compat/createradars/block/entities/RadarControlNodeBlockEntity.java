package dev.lumareth.moremissiles.compat.createradars.block.entities;

import com.happysg.radar.block.radar.behavior.IRadar;
import com.happysg.radar.block.radar.track.RadarTrack;
import com.happysg.radar.block.radar.track.TrackCategory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlockEntities;
import dev.lumareth.moremissiles.compat.createradars.utils.RedstoneReceiverBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;

public class RadarControlNodeBlockEntity extends SmartBlockEntity {
    protected BlockPos radarPos;
    private boolean hasLock = false;
    private int lockedTargetIDX = 0;
    private String lockedTargetId = "";
    boolean wasSelected = false;

    public static final BehaviourType<RedstoneReceiverBehaviour> CHANNEL_UP = new BehaviourType<>();
    public static final BehaviourType<RedstoneReceiverBehaviour> CHANNEL_DOWN = new BehaviourType<>();
    public static final BehaviourType<RedstoneReceiverBehaviour> TRIGGER = new BehaviourType<>();
    public static final BehaviourType<RedstoneReceiverBehaviour> TARGET_UP = new BehaviourType<>();
    public static final BehaviourType<RedstoneReceiverBehaviour> TARGET_DOWN = new BehaviourType<>();

    private RedstoneReceiverBehaviour channelUpBehaviour;
    private RedstoneReceiverBehaviour channelDownBehaviour;
    private RedstoneReceiverBehaviour triggerBehaviour;
    private RedstoneReceiverBehaviour targetUpBehaviour;
    private RedstoneReceiverBehaviour targetDownBehaviour;

    private boolean wasChannelUpPowered = false;
    private boolean wasChannelDownPowered = false;
    private boolean wasTriggerPowered = false;
    private boolean wasTargetUpPowered = false;
    private boolean wasTargetDownPowered = false;

    public RadarControlNodeBlockEntity(BlockPos pos, BlockState state) {
        super(RadarsCompatModBlockEntities.RADAR_CONTROL_NODE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        CompoundTag data = this.getPersistentData();
        if (!data.contains("Channel")) {
            data.putDouble("Channel", 0D);
        }
        int channel = (int) data.getDouble("Channel");
        if (!data.contains("RadarX") || !data.contains("RadarY") || !data.contains("RadarZ")) {
            return;
        }
        double radarX = data.getDouble("RadarX");
        double radarY = data.getDouble("RadarY");
        double radarZ = data.getDouble("RadarZ");

        WariumBetterMissiles.LOGGER.debug("Radar position: {}, {}, {}", radarX, radarY, radarZ);
        radarPos = BlockPos.containing(radarX, radarY, radarZ);
        assert level != null;
        if (!level.isClientSide()){
            if (radarPos != null && level.getBlockEntity(radarPos) instanceof IRadar radar) {
                    List<RadarTrack> tracks = radar.getTracks().stream().filter(t -> t.trackCategory() == TrackCategory.SABLE).toList();
                    WariumBetterMissiles.LOGGER.debug("Radar tracks: {}", tracks);
                if (tracks.isEmpty()) {
                    clearLock(data, channel);
                } else if (hasLock) {
                    updateLockedTarget(data, channel, tracks);
                } else {
                    clearTargetData(data, channel);
                }

            }
        }
        this.setChanged();
    }

    private void updateLockedTarget(
            CompoundTag date,
            int channel,
            List<RadarTrack> tracks
    ) {
        RadarTrack lockedTrack = null;
        int currentIDX = -1;

        for (int i = 0; i < tracks.size(); i++) {
            RadarTrack track = tracks.get(i);
            if (track.getId().equals(lockedTargetId)) {
                lockedTrack = track;
                currentIDX = i;
                break;
            }
        }
        if (lockedTrack == null) {
            clearLock(date, channel);
            return;
        }
        lockedTargetIDX = currentIDX;
        date.putDouble("SelectedTarget", lockedTargetIDX);
        writeToNBT(date, channel, lockedTrack);
    }

    private void clearLock(CompoundTag data, int channel) {
        hasLock = false;
        lockedTargetId = "";
        lockedTargetIDX = 0;

        data.putDouble("SelectedTarget", -1);
        clearTargetData(data, channel);
    }

    private void clearTargetData(CompoundTag data, int channel) {
        data.putDouble("TargetX" + channel, 0D);
        data.putDouble("TargetY" + channel, 0D);
        data.putDouble("TargetZ" + channel, 0D);
        data.putDouble("TMX" + channel, 0D);
        data.putDouble("TMY" + channel, 0D);
        data.putDouble("TMZ" + channel, 0D);
        data.putDouble("Trigger" + channel, 0D);
    }

    private void changeTargetIDX(int change, List<RadarTrack> tracks){
        if (tracks.isEmpty()) {
            lockedTargetIDX = 0;
            return;
        }

        lockedTargetIDX += change;
        if (lockedTargetIDX >= tracks.size()) {
            lockedTargetIDX = 0;
        } else if (lockedTargetIDX < 0) {
            lockedTargetIDX = tracks.size() -1;
        }
    }

    private void writeToNBT(
            CompoundTag data,
            int channel,
            RadarTrack track
    ) {
        data.putDouble("TargetX" + channel, track.getPosition().x);
        data.putDouble("TargetY" + channel, track.getPosition().y);
        data.putDouble("TargetZ" + channel, track.getPosition().z);

        data.putDouble("TMX" + channel, track.getVelocity().x);
        data.putDouble("TMY" + channel, track.getVelocity().y);
        data.putDouble("TMZ" + channel, track.getVelocity().z);

        data.putDouble("Trigger" + channel, 0D);
    }

    @Override
    public void addBehavioursDeferred(List<BlockEntityBehaviour> behaviours) {
        Pair<ValueBoxTransform, ValueBoxTransform> dummySlots = ValueBoxTransform.Dual.makeSlots(DummyDualTransform::new);

        this.channelUpBehaviour = new RedstoneReceiverBehaviour(this, dummySlots, this::onChannelUpSignal, CHANNEL_UP);
        this.channelDownBehaviour = new RedstoneReceiverBehaviour(this, dummySlots, this::onChannelDownSignal, CHANNEL_DOWN);
        this.triggerBehaviour = new RedstoneReceiverBehaviour(this, dummySlots, this::onTriggerSignal, TRIGGER);
        this.targetUpBehaviour = new RedstoneReceiverBehaviour(this, dummySlots, this::onTargetUpSignal, TARGET_UP);
        this.targetDownBehaviour = new RedstoneReceiverBehaviour(this, dummySlots, this::onTargetDownSignal, TARGET_DOWN);

        channelUpBehaviour.setFrequency(true, frequency(Items.REDSTONE));
        channelUpBehaviour.setFrequency(false, frequency(Items.COBBLESTONE));

        channelDownBehaviour.setFrequency(true, frequency(Items.REDSTONE));
        channelDownBehaviour.setFrequency(false, frequency(Items.STONE));

        triggerBehaviour.setFrequency(true, frequency(Items.REDSTONE));
        triggerBehaviour.setFrequency(false, frequency(Items.IRON_INGOT));

        targetUpBehaviour.setFrequency(true, frequency(Items.REDSTONE));
        targetUpBehaviour.setFrequency(false, frequency(Items.GOLD_INGOT));

        targetDownBehaviour.setFrequency(true, frequency(Items.REDSTONE));
        targetDownBehaviour.setFrequency(false, frequency(Items.DIAMOND));

        behaviours.add(channelUpBehaviour);
        behaviours.add(channelDownBehaviour);
        behaviours.add(triggerBehaviour);
        behaviours.add(targetUpBehaviour);
        behaviours.add(targetDownBehaviour);

        WariumBetterMissiles.LOGGER.debug("RadarControlNode Behaviors: {}", behaviours.size());
    }
    private static ItemStack frequency(Item item) {
        return new ItemStack(item);
    }

    private void onChannelUpSignal(int pwr) {
        if (pwr > 0 && !wasChannelUpPowered) {
            debugMessage("Channel up received");
            double channel = this.getPersistentData().getDouble("Channel");
            channel++;
            if(channel >15){
                channel = 0;
            }
            this.getPersistentData().putDouble("Channel", channel);
            wasChannelUpPowered = pwr > 0;
        }else if (pwr <= 0) {
            wasChannelUpPowered = false;
        }
    }
    private void onChannelDownSignal(int pwr) {
        if (pwr > 0 && !wasChannelDownPowered) {
            debugMessage("Channel Down received");
            double channel = this.getPersistentData().getDouble("Channel");
            channel--;
            if(channel < 0){
                channel = 15;
            }
            this.getPersistentData().putDouble("Channel", channel);
            wasChannelDownPowered = pwr > 0;
        }else if (pwr <= 0) {
            wasChannelDownPowered = false;
        }
    }

    private void onTriggerSignal(int pwr) {
        if (pwr > 0 && !wasTriggerPowered) {
            debugMessage("Trigger received");
            double channel = this.getPersistentData().getDouble("Channel");
            String triggerKey = "Trigger" + channel;
            this.getPersistentData().putDouble(triggerKey, 1D);
            wasTriggerPowered = pwr > 0;
        }
         else if (pwr <= 0) {
            wasTriggerPowered = false;
        }
    }

    private void onTargetUpSignal(int pwr) {
        if (pwr > 0 && !wasTargetUpPowered) {
            debugMessage("Target up received");
            if (radarPos != null && level.getBlockEntity(radarPos) instanceof IRadar radar) {
                List<RadarTrack> tracks = radar.getTracks().stream().filter(t -> t.trackCategory() == TrackCategory.SABLE).toList();
                if (!tracks.isEmpty()){
                    changeTargetIDX(1, tracks);
                    RadarTrack selectedTrack = tracks.get(lockedTargetIDX);
                    lockedTargetId = selectedTrack.getId();
                    hasLock = true;

                    CompoundTag data = this.getPersistentData();
                    int channel = (int) data.getDouble("Channel");
                    writeToNBT(data, channel, selectedTrack);
                    setChanged();
                }
            }
            wasTargetUpPowered = pwr > 0;
        } else if (pwr <= 0) {
            wasTargetUpPowered = false;
        }
        }
    private void onTargetDownSignal(int pwr) {
    if (pwr > 0 && !wasTargetDownPowered) {
        debugMessage("Target down received");
        if (radarPos != null && level.getBlockEntity(radarPos) instanceof IRadar radar) {
            List<RadarTrack> tracks = radar.getTracks().stream()
                    .filter(t -> t.trackCategory() == TrackCategory.SABLE)
                    .toList();

            if (!tracks.isEmpty()) {
                changeTargetIDX(-1, tracks);

                RadarTrack selectedTrack = tracks.get(lockedTargetIDX);
                lockedTargetId = selectedTrack.getId();
                hasLock = true;

                CompoundTag data = getPersistentData();
                int channel = (int) data.getDouble("Channel");
                data.putInt("SelectedTarget", lockedTargetIDX);
                writeToNBT(data, channel, selectedTrack);
                setChanged();
            }
        }

        wasTargetDownPowered = true;
    } else if (pwr <= 0) {
        wasTargetDownPowered = false;
    }
}
    private void debugMessage(String message) {
        if (level == null || level.isClientSide()) {
            return;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(
                    Component.literal("[RCN] " + message)
            );
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {}

    private static class DummyDualTransform extends ValueBoxTransform.Dual {

        public DummyDualTransform(boolean first) {
            super(first);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
            return null;
        }

        @Override
        public void rotate(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, PoseStack poseStack) {

        }
    }

}
