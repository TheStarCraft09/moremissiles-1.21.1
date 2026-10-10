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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A receive-only Create redstone link endpoint. Unlike Create's LinkBehaviour, every instance has its own
 * BehaviourType, so several of them can live on one block entity. Frequencies are fixed by the owner and not saved.
 */
public class RcnLinkReceiver extends BlockEntityBehaviour implements IRedstoneLinkable {
    private final BehaviourType<RcnLinkReceiver> type;
    private final String name;
    private final Frequency frequencyFirst;
    private final Frequency frequencyLast;
    private int receivedStrength = 0;

    public RcnLinkReceiver(SmartBlockEntity be, BehaviourType<RcnLinkReceiver> type, String name,
                           ItemStack first, ItemStack last) {
        super(be);
        this.type = type;
        this.name = name;
        this.frequencyFirst = Frequency.of(first);
        this.frequencyLast = Frequency.of(last);
    }

    public boolean isPowered() {
        return this.receivedStrength > 0;
    }

    public int getReceivedStrength() {
        return this.receivedStrength;
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
