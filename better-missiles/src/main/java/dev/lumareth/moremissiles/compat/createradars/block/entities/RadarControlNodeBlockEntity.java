package dev.lumareth.moremissiles.compat.createradars.block.entities;

import com.happysg.radar.block.arad.aradnetworks.RadarContactRegistry;
import com.happysg.radar.block.radar.behavior.IRadar;
import com.happysg.radar.block.radar.track.RadarTrack;
import com.happysg.radar.block.radar.track.TrackCategory;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import dev.lumareth.moremissiles.compat.createradars.block.RadarsCompatModBlockEntities;
import dev.lumareth.moremissiles.compat.createradars.menu.RadarControlNodeMenu;
import dev.lumareth.moremissiles.compat.createradars.utils.RcnLinkReceiver;
import dev.lumareth.moremissiles.network.RadarLockPayload;
import dev.lumareth.moremissiles.utils.MissileWarningRegistry;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bridges a Create Radar to WariumCE-style hardpoints. All values in the persistent data use WariumCE's
 * format: doubles, with keys built from doubles ("TargetX2.0", "Trigger1.0").
 */
public class RadarControlNodeBlockEntity extends SmartBlockEntity implements MenuProvider {
    public static final int MAX_CHANNEL = 15;
    /**
     * Default frequency pairs per link, in row order: next target, previous target, fire, channel up, channel down.
     */
    private static final Item[][] DEFAULT_FREQUENCIES = {
            {Items.REDSTONE_BLOCK, Items.GOLD_INGOT},
            {Items.REDSTONE_BLOCK, Items.DIAMOND},
            {Items.REDSTONE_BLOCK, Items.IRON_INGOT},
            {Items.REDSTONE_BLOCK, Items.COBBLESTONE},
            {Items.REDSTONE_BLOCK, Items.STONE},
    };
    public static final int LINK_COUNT = DEFAULT_FREQUENCIES.length;
    private static final int TRIGGER_PULSE_TICKS = 2;
    private static final double NO_TARGET = -1D;
    private static final double FEEDBACK_RANGE = 32D;
    private static final int RWR_LOCK_TTL = 20;
    private static final int RWR_REFRESH_TICKS = 10;

    public static final BehaviourType<RcnLinkReceiver> CHANNEL_UP = new BehaviourType<>();
    public static final BehaviourType<RcnLinkReceiver> CHANNEL_DOWN = new BehaviourType<>();
    public static final BehaviourType<RcnLinkReceiver> TRIGGER = new BehaviourType<>();
    public static final BehaviourType<RcnLinkReceiver> TARGET_UP = new BehaviourType<>();
    public static final BehaviourType<RcnLinkReceiver> TARGET_DOWN = new BehaviourType<>();

    private RcnLinkReceiver channelUpLink;
    private RcnLinkReceiver channelDownLink;
    private RcnLinkReceiver triggerLink;
    private RcnLinkReceiver targetUpLink;
    private RcnLinkReceiver targetDownLink;

    private boolean wasChannelUpPowered = false;
    private boolean wasChannelDownPowered = false;
    private boolean wasTriggerPowered = false;
    private boolean wasTargetUpPowered = false;
    private boolean wasTargetDownPowered = false;

    private String lockedTargetId = "";
    @Nullable
    private UUID rwrLockedShip = null;
    private int triggerTicks = 0;
    private double triggerChannel = 0D;

    public RadarControlNodeBlockEntity(BlockPos pos, BlockState state) {
        super(RadarsCompatModBlockEntities.RADAR_CONTROL_NODE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        targetUpLink = createLink(TARGET_UP, "TargetUp", 0);
        targetDownLink = createLink(TARGET_DOWN, "TargetDown", 1);
        triggerLink = createLink(TRIGGER, "Trigger", 2);
        channelUpLink = createLink(CHANNEL_UP, "ChannelUp", 3);
        channelDownLink = createLink(CHANNEL_DOWN, "ChannelDown", 4);

        behaviours.addAll(linksInRowOrder());
    }

    private RcnLinkReceiver createLink(BehaviourType<RcnLinkReceiver> type, String name, int row) {
        return new RcnLinkReceiver(this, type, name, new ItemStack(DEFAULT_FREQUENCIES[row][0]), new ItemStack(DEFAULT_FREQUENCIES[row][1]));
    }

    /**
     * The links in the order the configuration screen shows them, matching {@link #DEFAULT_FREQUENCIES}.
     */
    private List<RcnLinkReceiver> linksInRowOrder() {
        return List.of(targetUpLink, targetDownLink, triggerLink, channelUpLink, channelDownLink);
    }

    /**
     * @return two items (first, last frequency) per link, in row order
     */
    public List<ItemStack> getFrequencyItems() {
        List<ItemStack> items = new ArrayList<>();
        for (RcnLinkReceiver link : linksInRowOrder()) {
            items.add(link.getFirstFrequencyItem().copy());
            items.add(link.getLastFrequencyItem().copy());
        }
        return items;
    }

    public static List<ItemStack> getDefaultFrequencyItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Item[] pair : DEFAULT_FREQUENCIES) {
            items.add(new ItemStack(pair[0]));
            items.add(new ItemStack(pair[1]));
        }
        return items;
    }

    /**
     * @param items two items (first, last frequency) per link, in row order
     */
    public void setFrequencyItems(List<ItemStack> items) {
        List<RcnLinkReceiver> links = linksInRowOrder();
        boolean changed = false;
        for (int i = 0; i < links.size(); i++) {
            changed |= links.get(i).setFrequency(items.get(i * 2), items.get(i * 2 + 1));
        }
        if (changed) {
            notifyUpdate();
        }
    }

    @Override
    public boolean canPlayerUse(Player player) {
        // Create's check compares raw positions, which fails when the node sits on a Sable sub-level
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, Vec3.atCenterOf(worldPosition), player.position()) <= 64D;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.more_missiles.radar_control_node");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return RadarControlNodeMenu.create(containerId, playerInventory, this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }
        CompoundTag data = this.getPersistentData();
        boolean changed = tickTriggerPulse(data);

        boolean channelUp = risingEdge(channelUpLink, wasChannelUpPowered);
        wasChannelUpPowered = channelUpLink.isPowered();
        boolean channelDown = risingEdge(channelDownLink, wasChannelDownPowered);
        wasChannelDownPowered = channelDownLink.isPowered();
        boolean trigger = risingEdge(triggerLink, wasTriggerPowered);
        wasTriggerPowered = triggerLink.isPowered();
        boolean targetUp = risingEdge(targetUpLink, wasTargetUpPowered);
        wasTargetUpPowered = targetUpLink.isPowered();
        boolean targetDown = risingEdge(targetDownLink, wasTargetDownPowered);
        wasTargetDownPowered = targetDownLink.isPowered();

        if (channelUp) {
            changed |= changeChannel(data, 1);
        }
        if (channelDown) {
            changed |= changeChannel(data, -1);
        }
        if (channelUp || channelDown) {
            notifyNearby(Component.translatable("message.more_missiles.rcn.channel", (int) data.getDouble("Channel")));
        }

        List<RadarTrack> tracks = getRadarTracks(data);
        boolean hadLock = !lockedTargetId.isEmpty();
        int targetIdx = findLockedTrack(tracks);
        if (hadLock && targetIdx < 0) {
            notifyNearby(Component.translatable("message.more_missiles.rcn.target_lost"));
        }
        if (targetUp) {
            targetIdx = cycleTarget(tracks, targetIdx, 1);
        }
        if (targetDown) {
            targetIdx = cycleTarget(tracks, targetIdx, -1);
        }
        if (targetUp || targetDown) {
            notifyNearby(targetIdx < 0
                    ? noTargetReason(data)
                    : Component.translatable("message.more_missiles.rcn.target_locked", targetIdx + 1, tracks.size()));
        }
        changed |= writeTarget(data, tracks, targetIdx);
        updateRwrLock(targetIdx < 0 ? null : tracks.get(targetIdx));
        if (targetIdx >= 0) {
            RadarLockPayload marker = new RadarLockPayload(worldPosition, tracks.get(targetIdx).getPosition());
            for (ServerPlayer player : nearbyPlayers()) {
                PacketDistributor.sendToPlayer(player, marker);
            }
        }

        if (trigger) {
            startTriggerPulse(data);
            notifyNearby(Component.translatable("message.more_missiles.rcn.fired", (int) triggerChannel));
            changed = true;
        }

        if (changed) {
            setChanged();
        }
    }

    private static boolean risingEdge(RcnLinkReceiver link, boolean wasPowered) {
        return link.isPowered() && !wasPowered;
    }

    @Nullable
    private static BlockPos getRadarPos(CompoundTag data) {
        if (!data.contains("RadarX") || !data.contains("RadarY") || !data.contains("RadarZ")) {
            return null;
        }
        return BlockPos.containing(data.getDouble("RadarX"), data.getDouble("RadarY"), data.getDouble("RadarZ"));
    }

    @Nullable
    private IRadar getRadar(CompoundTag data) {
        BlockPos radarPos = getRadarPos(data);
        if (radarPos != null && level.getBlockEntity(radarPos) instanceof IRadar radar) {
            return radar;
        }
        return null;
    }

    private List<RadarTrack> getRadarTracks(CompoundTag data) {
        IRadar radar = getRadar(data);
        if (radar == null) {
            return List.of();
        }
        return radar.getTracks().stream().filter(t -> t.trackCategory() == TrackCategory.SABLE).toList();
    }

    private Component noTargetReason(CompoundTag data) {
        BlockPos radarPos = getRadarPos(data);
        if (radarPos == null) {
            return Component.translatable("message.more_missiles.rcn.no_radar");
        }
        if (getRadar(data) == null) {
            return Component.translatable("message.more_missiles.rcn.radar_missing", radarPos.getX(), radarPos.getY(), radarPos.getZ());
        }
        return Component.translatable("message.more_missiles.rcn.no_targets");
    }

    /**
     * Players close enough to get feedback from this node. Distance accounts for Sable sub-levels.
     */
    private List<ServerPlayer> nearbyPlayers() {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        List<ServerPlayer> players = new ArrayList<>();
        for (Player player : level.players()) {
            if (player instanceof ServerPlayer serverPlayer
                    && SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, center, player.position()) <= FEEDBACK_RANGE * FEEDBACK_RANGE) {
                players.add(serverPlayer);
            }
        }
        return players;
    }

    private void notifyNearby(Component message) {
        for (ServerPlayer player : nearbyPlayers()) {
            player.displayClientMessage(message, true);
        }
    }

    public void showStatus(Player player) {
        CompoundTag data = this.getPersistentData();
        List<RadarTrack> tracks = getRadarTracks(data);
        BlockPos radarPos = getRadarPos(data);
        int targetIdx = findLockedTrack(tracks);

        player.sendSystemMessage(Component.translatable("message.more_missiles.rcn.status.header"));
        player.sendSystemMessage(getRadar(data) != null
                ? Component.translatable("message.more_missiles.rcn.status.radar", radarPos.getX(), radarPos.getY(), radarPos.getZ(), tracks.size())
                : noTargetReason(data));
        player.sendSystemMessage(targetIdx < 0
                ? Component.translatable("message.more_missiles.rcn.status.no_lock")
                : Component.translatable("message.more_missiles.rcn.status.lock", targetIdx + 1, tracks.size()));
        player.sendSystemMessage(Component.translatable("message.more_missiles.rcn.channel", (int) data.getDouble("Channel")));
        player.sendSystemMessage(Component.translatable("message.more_missiles.rcn.status.inputs",
                channelUpLink.getReceivedStrength(), channelDownLink.getReceivedStrength(), triggerLink.getReceivedStrength(),
                targetUpLink.getReceivedStrength(), targetDownLink.getReceivedStrength()));
    }

    /**
     * @return the index of the locked track, or -1 (and the lock is dropped) if it is no longer tracked
     */
    private int findLockedTrack(List<RadarTrack> tracks) {
        if (lockedTargetId.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId().equals(lockedTargetId)) {
                return i;
            }
        }
        WariumBetterMissiles.LOGGER.debug("RCN at {} lost target {}", worldPosition, lockedTargetId);
        lockedTargetId = "";
        return -1;
    }

    private int cycleTarget(List<RadarTrack> tracks, int currentIdx, int direction) {
        if (tracks.isEmpty()) {
            return currentIdx;
        }
        int nextIdx;
        if (currentIdx < 0) {
            nextIdx = direction > 0 ? 0 : tracks.size() - 1;
        } else {
            nextIdx = Math.floorMod(currentIdx + direction, tracks.size());
        }
        lockedTargetId = tracks.get(nextIdx).getId();
        WariumBetterMissiles.LOGGER.debug("RCN at {} locked target {} ({})", worldPosition, nextIdx, lockedTargetId);
        return nextIdx;
    }

    /**
     * Writes the locked track under "TargetX" + SelectedTarget etc., the way WariumCE hardpoints read it.
     * Keys of a previously selected index are removed so hardpoints never read a stale target.
     */
    private boolean writeTarget(CompoundTag data, List<RadarTrack> tracks, int targetIdx) {
        boolean changed = false;
        double selected = targetIdx < 0 ? NO_TARGET : targetIdx;
        double previous = data.contains("SelectedTarget") ? data.getDouble("SelectedTarget") : NO_TARGET;

        if (!data.contains("SelectedTarget") || previous != selected) {
            if (previous != NO_TARGET) {
                removeTargetKeys(data, previous);
            }
            data.putDouble("SelectedTarget", selected);
            changed = true;
        }

        if (targetIdx >= 0) {
            RadarTrack track = tracks.get(targetIdx);
            data.putDouble("TargetX" + selected, track.getPosition().x);
            data.putDouble("TargetY" + selected, track.getPosition().y);
            data.putDouble("TargetZ" + selected, track.getPosition().z);
            data.putDouble("TMX" + selected, track.getVelocity().x);
            data.putDouble("TMY" + selected, track.getVelocity().y);
            data.putDouble("TMZ" + selected, track.getVelocity().z);
            changed = true;
        }
        return changed;
    }

    /**
     * Reports the lock to Create Radars, so a Radar Warning Receiver on the locked aircraft sounds its lock warning.
     * Create Radars never decays these locks by itself, so a dropped lock has to be removed explicitly.
     */
    private void updateRwrLock(@Nullable RadarTrack track) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID ship = track == null ? null : parseShipId(track.getId());
        if (rwrLockedShip != null && !rwrLockedShip.equals(ship)) {
            RadarContactRegistry.unLock(serverLevel, rwrLockedShip);
        }
        // Refresh periodically in case Create Radars starts decaying locks
        if (ship != null && (!ship.equals(rwrLockedShip) || serverLevel.getGameTime() % RWR_REFRESH_TICKS == 0)) {
            RadarContactRegistry.markLocked(serverLevel, ship, RWR_LOCK_TTL);
        }
        if (ship == null) {
            getPersistentData().remove(MissileWarningRegistry.LOCKED_SHIP_KEY);
        } else if (!ship.equals(rwrLockedShip)) {
            getPersistentData().putString(MissileWarningRegistry.LOCKED_SHIP_KEY, ship.toString());
        }
        rwrLockedShip = ship;
    }

    @Nullable
    private static UUID parseShipId(String trackId) {
        // Sable tracks use the sub-level's UUID as their id
        try {
            return UUID.fromString(trackId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        // Broken or unloaded: don't leave the target's RWR warning on forever
        updateRwrLock(null);
    }

    private static void removeTargetKeys(CompoundTag data, double index) {
        data.remove("TargetX" + index);
        data.remove("TargetY" + index);
        data.remove("TargetZ" + index);
        data.remove("TMX" + index);
        data.remove("TMY" + index);
        data.remove("TMZ" + index);
    }

    private boolean changeChannel(CompoundTag data, int direction) {
        // A running pulse must not stay stuck on the channel we are leaving
        stopTriggerPulse(data);
        double channel = Math.floorMod((int) data.getDouble("Channel") + direction, MAX_CHANNEL + 1);
        data.putDouble("Channel", channel);
        WariumBetterMissiles.LOGGER.debug("RCN at {} switched to channel {}", worldPosition, channel);
        return true;
    }

    /**
     * WariumCE hardpoints never reset the control block's trigger, so the RCN sends a short pulse and clears it
     * itself. The pulse lasts two full ticks, so every hardpoint sees it regardless of block entity tick order.
     */
    private void startTriggerPulse(CompoundTag data) {
        stopTriggerPulse(data);
        triggerChannel = data.getDouble("Channel");
        triggerTicks = TRIGGER_PULSE_TICKS;
        data.putDouble("Trigger" + triggerChannel, 1D);
        WariumBetterMissiles.LOGGER.debug("RCN at {} triggered channel {}", worldPosition, triggerChannel);
    }

    private boolean tickTriggerPulse(CompoundTag data) {
        if (triggerTicks <= 0) {
            return false;
        }
        triggerTicks--;
        if (triggerTicks == 0) {
            data.putDouble("Trigger" + triggerChannel, 0D);
        }
        return true;
    }

    private void stopTriggerPulse(CompoundTag data) {
        if (triggerTicks > 0) {
            data.putDouble("Trigger" + triggerChannel, 0D);
            triggerTicks = 0;
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            return;
        }
        tag.putString("LockedTargetId", lockedTargetId);
        tag.putInt("TriggerTicks", triggerTicks);
        tag.putDouble("TriggerChannel", triggerChannel);
        // Saved so a link that is still powered after a reload doesn't count as a new press
        tag.putBoolean("WasChannelUpPowered", wasChannelUpPowered);
        tag.putBoolean("WasChannelDownPowered", wasChannelDownPowered);
        tag.putBoolean("WasTriggerPowered", wasTriggerPowered);
        tag.putBoolean("WasTargetUpPowered", wasTargetUpPowered);
        tag.putBoolean("WasTargetDownPowered", wasTargetDownPowered);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            return;
        }
        lockedTargetId = tag.getString("LockedTargetId");
        triggerTicks = tag.getInt("TriggerTicks");
        triggerChannel = tag.getDouble("TriggerChannel");
        wasChannelUpPowered = tag.getBoolean("WasChannelUpPowered");
        wasChannelDownPowered = tag.getBoolean("WasChannelDownPowered");
        wasTriggerPowered = tag.getBoolean("WasTriggerPowered");
        wasTargetUpPowered = tag.getBoolean("WasTargetUpPowered");
        wasTargetDownPowered = tag.getBoolean("WasTargetDownPowered");
    }
}
