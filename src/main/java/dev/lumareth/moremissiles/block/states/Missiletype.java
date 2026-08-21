package dev.lumareth.moremissiles.block.states;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum Missiletype implements StringRepresentable {
    EMPTY("empty"),
    IR("ir"),
    SARH("sarh"),
    SACLOCS("saclocs");

    private final String name;

    Missiletype(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
