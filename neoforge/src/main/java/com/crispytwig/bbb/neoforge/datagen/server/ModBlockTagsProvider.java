package com.crispytwig.bbb.neoforge.datagen.server;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(output, lookup, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
    }
}
