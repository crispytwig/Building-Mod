package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> PRISMARINE = TagKey.create(Registries.BLOCK, Artisanal.location("prismarine"));
    public static final TagKey<Block> FACADE_MATERIALS = TagKey.create(Registries.BLOCK, Artisanal.location("facade_materials"));
    public static final TagKey<Block> CHAIN_CONNECTIBLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("connectiblechains", "chain_connectible"));

    private ModTags() {
    }
}
