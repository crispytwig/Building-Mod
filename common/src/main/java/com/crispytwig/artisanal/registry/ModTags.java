package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> PRISMARINE = TagKey.create(Registries.BLOCK, Artisanal.location("prismarine"));

    private ModTags() {
    }
}
