package dev.lumareth.moremissiles.compat.createradars.utils;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.lumareth.moremissiles.WariumBetterMissiles;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A receive-only Create redstone link endpoint. Unlike Create's LinkBehaviour, every instance has its own
 * BehaviourType, so several of them can live on one block entity. Frequencies are saved in a shared "Frequencies"
 * compound under this receiver's name, and also synced to the client for the configuration screen.
 */
public class RcnLinkReceiver extends BlockEntityBehaviour implements IRedstoneLinkable {
    private static final String FREQUENCIES_KEY = "Frequencies";

    private final BehaviourType<RcnLinkReceiver> type;
    private final String name;
    private Frequency frequencyFirst;
    private Frequency frequencyLast;
    private int receivedStrength = 0;

    public RcnLinkReceiver(SmartBlockEntity be, BehaviourType<RcnLinkReceiver> type, String name,
                           ItemStack first, ItemStack last) {
        super(be);
        this.type = type;
        this.name = name;
        this.frequencyFirst = Frequency.of(singleItem(first));
        this.frequencyLast = Frequency.of(singleItem(last));
    }

    public boolean isPowered() {
        return this.receivedStrength > 0;
    }

    public int getReceivedStrength() {
        return this.receivedStrength;
    }

    public ItemStack getFirstFrequencyItem() {
        return this.frequencyFirst.getStack();
    }

    public ItemStack getLastFrequencyItem() {
        return this.frequencyLast.getStack();
    }

    /**
     * Moves this receiver to a new frequency pair, the same way Create's LinkBehaviour does.
     *
     * @return whether the frequency actually changed
     */
    public boolean setFrequency(ItemStack first, ItemStack last) {
        first = singleItem(first);
        last = singleItem(last);
        if (ItemStack.isSameItemSameComponents(first, getFirstFrequencyItem())
                && ItemStack.isSameItemSameComponents(last, getLastFrequencyItem())) {
            return false;
        }
        Level level = this.getWorld();
        boolean inNetwork = level != null && !level.isClientSide;
        if (inNetwork) {
            getHandler().removeFromNetwork(level, this);
        }
        this.frequencyFirst = Frequency.of(first);
        this.frequencyLast = Frequency.of(last);
        // Create doesn't report the new network's power to the joining link itself; wait for its next update
        this.receivedStrength = 0;
        if (inNetwork) {
            getHandler().addToNetwork(level, this);
            WariumBetterMissiles.LOGGER.debug("RCN link {} at {} moved to network {} + {}", this.name, this.getPos(),
                    first.getItem(), last.getItem());
        }
        return true;
    }

    private static ItemStack singleItem(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(1);
        }
        return copy;
    }

    @Override
    public BehaviourType<?> getType() {
        return this.type;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!this.getWorld().isClientSide) {
            getHandler().addToNetwork(this.getWorld(), this);
            WariumBetterMissiles.LOGGER.debug("RCN link {} at {} joined network {} + {}", this.name, this.getPos(),
                    this.frequencyFirst.getStack().getItem(), this.frequencyLast.getStack().getItem());
        }
    }

    @Override
    public void unload() {
        super.unload();
        if (!this.getWorld().isClientSide) {
            getHandler().removeFromNetwork(this.getWorld(), this);
        }
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        CompoundTag link = new CompoundTag();
        link.put("First", getFirstFrequencyItem().saveOptional(registries));
        link.put("Last", getLastFrequencyItem().saveOptional(registries));
        CompoundTag frequencies = nbt.getCompound(FREQUENCIES_KEY);
        frequencies.put(this.name, link);
        nbt.put(FREQUENCIES_KEY, frequencies);
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        CompoundTag frequencies = nbt.getCompound(FREQUENCIES_KEY);
        if (!frequencies.contains(this.name)) {
            return;
        }
        // Only called before initialize() on the server, so there is no network membership to update here
        CompoundTag link = frequencies.getCompound(this.name);
        this.frequencyFirst = Frequency.of(ItemStack.parseOptional(registries, link.getCompound("First")));
        this.frequencyLast = Frequency.of(ItemStack.parseOptional(registries, link.getCompound("Last")));
    }

    @Override
    public int getTransmittedStrength() {
        return 0;
    }

    @Override
    public void setReceivedStrength(int power) {
        if (power != this.receivedStrength) {
            WariumBetterMissiles.LOGGER.debug("RCN link {} at {} received power {}", this.name, this.getPos(), power);
        }
        this.receivedStrength = power;
    }

    @Override
    public boolean isListening() {
        return true;
    }

    @Override
    public boolean isAlive() {
        Level level = this.getWorld();
        BlockPos pos = this.getPos();
        if (this.blockEntity.isChunkUnloaded() || this.blockEntity.isRemoved()) {
            return false;
        }
        return level.isLoaded(pos) && level.getBlockEntity(pos) == this.blockEntity;
    }

    @Override
    public Couple<Frequency> getNetworkKey() {
        return Couple.create(this.frequencyFirst, this.frequencyLast);
    }

    @Override
    public BlockPos getLocation() {
        return this.getPos();
    }

    private static RedstoneLinkNetworkHandler getHandler() {
        return Create.REDSTONE_LINK_NETWORK_HANDLER;
    }
}
