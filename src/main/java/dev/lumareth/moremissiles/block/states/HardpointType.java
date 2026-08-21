package dev.lumareth.moremissiles.block.states;

import net.minecraft.util.StringRepresentable;

public enum HardpointType implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    HardpointType(String type) {
        this.name = type;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
