package dev.lumareth.moremissiles.utils.itemstates;


import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ToolkitMode implements StringRepresentable {
    LINKING("linking"),
    CONFIGURING("configuring");

    ToolkitMode(String name) {
        this.name = name;
    }

    public static final Codec<ToolkitMode> CODEC = Codec.stringResolver(
            ToolkitMode::name,
            ToolkitMode::valueOf
    );

    public ToolkitMode next() {
        ToolkitMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolkitMode> STREAM_CODEC
           = ByteBufCodecs.idMapper(
                    i -> ToolkitMode.values()[i],  // int -> ToolkitMode
                    ToolkitMode::ordinal           // ToolkitMode -> int
            ).cast();

    private String name;

    @Override
    public String getSerializedName() {
        return this.name;
    }

}
