package com.crispytwig.bbb.neoforge.datagen.server;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper helper) {
        super(output, lookup, blockTags, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
    }
}
