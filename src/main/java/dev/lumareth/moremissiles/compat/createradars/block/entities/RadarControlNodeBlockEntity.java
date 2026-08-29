package dev.lumareth.moremissiles.compat.createradars.block.entities;

import com.happysg.radar.block.radar.behavior.IRadar;
import com.happysg.radar.block.radar.track.RadarTrack;
import com.happysg.radar.block.radar.track.TrackCategory;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;

public class RadarControlNodeBlockEntity extends SmartBlockEntity {
    protected BlockPos radarPos;
    boolean hasLock = true;
    double lockedTargetIDX = 0;
    double previousIDX;
    String lockedTargetId = "";
    boolean wasSelected = false;




    public RadarControlNodeBlockEntity(BlockPos pos, BlockState state) {
        super(RadarsCompatModBlockEntities.RADAR_CONTROL_NODE.get(), pos, state);
    }

    @Override
    public void tick() {
        CompoundTag data = this.getPersistentData();
        data.putDouble("Channel", 0D);
        if (!data.contains("RadarX") || !data.contains("RadarY") || !data.contains("RadarZ")) {
            return;
        }
        double radarX = data.getDouble("RadarX");
        double radarY = data.getDouble("RadarY");
        double radarZ = data.getDouble("RadarZ");
        double channel = data.getDouble("Channel");

        radarPos = BlockPos.containing(radarX, radarY, radarZ);
        assert level != null;
        if (!level.isClientSide()){
            if (radarPos != null && level.getBlockEntity(radarPos) instanceof IRadar radar) {
                    List<RadarTrack> tracks = radar.getTracks().stream().filter(t -> t.trackCategory() == TrackCategory.SABLE).toList();
                    WariumBetterMissiles.LOGGER.debug("Radar tracks: {}", tracks);
                    if (tracks.isEmpty()){
                        data.putDouble("TargetX" + channel, 0D);
                        data.putDouble("TargetY" + channel, 0D);
                        data.putDouble("TargetZ" + channel, 0D);
                        data.putDouble("TMX" + channel, 0D);
                        data.putDouble("TMY" + channel, 0D);
                        data.putDouble("TMZ" + channel, 0D);
                    } else if (!hasLock){
                        return;
                    } else if (hasLock) {
                        if (lockedTargetId.isEmpty() || previousIDX != lockedTargetIDX) {
                            for (int i = 0; i < tracks.size(); i++) {
                                if (i == lockedTargetIDX) {
                                    RadarTrack track = tracks.get(i);
                                    lockedTargetId = track.getId();
                                    data.putDouble("SelectedTarget", lockedTargetIDX);
                                    writeToNBT(data, channel, track);
                                }
                            }
                        } else {
                            for (int i = 0; i < tracks.size(); i++) {
                                if (tracks.get(i).getId().equals(lockedTargetId)) {
                                    RadarTrack track = tracks.get(i);
                                    writeToNBT(data, channel, track);
                                }
                            }
                        }
                    }

            }
        }
    }

    private void changeTargetIDX(int change, List<RadarTrack> tracks){
        lockedTargetIDX = (lockedTargetIDX + change);
        if (lockedTargetIDX >= tracks.size()) {
            lockedTargetIDX = 0;
        } else if (lockedTargetIDX < 0) {
            lockedTargetIDX = tracks.size() -1;
        }
    }

    private void writeToNBT(CompoundTag data, double channel, RadarTrack track) {
        data.putDouble("TargetX" + channel, track.getPosition().x);
        data.putDouble("TargetY" + channel, track.getPosition().y);
        data.putDouble("TargetZ" + channel, track.getPosition().z);
        data.putDouble("TMX" + channel, track.getVelocity().x);
        data.putDouble("TMY" + channel, track.getVelocity().y);
        data.putDouble("TMZ" + channel, track.getVelocity().z);
        data.putDouble("Trigger" + channel, 0D);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {

    }
}
