package dev.lumareth.moremissiles.block;

import dev.lumareth.moremissiles.WariumBetterMissiles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> LINKABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(WariumBetterMissiles.MOD_ID, "linkable"));
}
