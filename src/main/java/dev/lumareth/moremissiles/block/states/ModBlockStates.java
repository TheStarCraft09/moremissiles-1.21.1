package dev.lumareth.moremissiles.block.states;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ModBlockStates {
    public static final EnumProperty<HardpointType> HARDPOINT_TYPE = EnumProperty.create("hardpoint_type", HardpointType.class);
    public static final EnumProperty<Missiletype> MISSILE_TYPE = EnumProperty.create("missile_type", Missiletype.class);
}
