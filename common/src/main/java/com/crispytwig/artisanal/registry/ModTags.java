package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> PRISMARINE = TagKey.create(Registries.BLOCK, Artisanal.location("prismarine"));
    public static final TagKey<Block> FACADE_MATERIALS = TagKey.create(Registries.BLOCK, Artisanal.location("facade_materials"));
    public static final TagKey<Block> CHAIN_CONNECTIBLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("connectiblechains", "chain_connectible"));

    public static final TagKey<Item> ABRASIVES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "sands"));

    public static final TagKey<Block> LAYERS = TagKey.create(Registries.BLOCK, Artisanal.location("layers"));
    public static final TagKey<Item> WOODEN_LAYERS = TagKey.create(Registries.ITEM, Artisanal.location("wooden_layers"));

    private ModTags() {
    }
}
