package dev.lumareth.moremissiles.compat.createradars.utils;


import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.IntConsumer;

public class RedstoneReceiverBehaviour extends BlockEntityBehaviour {
    private final BehaviourType<RedstoneReceiverBehaviour> type;
    private final LinkBehaviour delegate;
    private ItemStack frequencyFirst = ItemStack.EMPTY;
    private ItemStack frequencyLast = ItemStack.EMPTY;

    public RedstoneReceiverBehaviour(SmartBlockEntity be,
                                     Pair<ValueBoxTransform, ValueBoxTransform> slots,
                                     IntConsumer signalCallback,
                                     BehaviourType<RedstoneReceiverBehaviour> type) {
        super(be);
        this.type = type;
        this.delegate = LinkBehaviour.receiver(be, slots, signalCallback);
    }

    public void setFrequency(boolean first, ItemStack stack) {
        if (first) {
            this.frequencyFirst = stack;
        } else {
            this.frequencyLast = stack;
        }
        this.delegate.setFrequency(first, stack);
    }

    public ItemStack getFrequencyStack(boolean first) {
        return first ? this.frequencyFirst : this.frequencyLast;
    }

    @Override
    public BehaviourType<?> getType() {
        return this.type;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.delegate.initialize();
    }

    @Override
    public void unload() {
        super.unload();
        this.delegate.unload();
    }

    @Override
    public void destroy() {
        super.destroy();
        this.delegate.destroy();
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(nbt, registries, clientPacket);
        this.delegate.read(nbt, registries, clientPacket);
    }

    @Override
    public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(nbt, registries, clientPacket);
        this.delegate.write(nbt, registries, clientPacket);
    }

    @Override
    public void onBlockChanged(BlockState oldState) {
        super.onBlockChanged(oldState);
        this.delegate.onBlockChanged(oldState);
    }

    @Override
    public void onNeighborChanged(BlockPos neighborPos) {
        super.onNeighborChanged(neighborPos);
        this.delegate.onNeighborChanged(neighborPos);
    }

    public boolean testHit(boolean first, Vec3 hit) {
        return this.delegate.testHit(first, hit);
    }

}