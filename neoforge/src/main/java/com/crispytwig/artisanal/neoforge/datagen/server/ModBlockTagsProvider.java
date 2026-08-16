package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(output, lookup, Artisanal.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (block instanceof StairBlock) {
                tag(BlockTags.STAIRS).add(block);
            } else if (block instanceof SlabBlock) {
                tag(BlockTags.SLABS).add(block);
            }
        }

        ModBlocks.woodenBlocks().forEach(holder -> tag(BlockTags.MINEABLE_WITH_AXE).add(holder.get()));
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.OAK_WINDOW.get(), ModBlocks.OAK_WINDOW_PANE.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PRISMARINE_TILES.get(), ModBlocks.PRISMARINE_TILE_STAIRS.get(), ModBlocks.PRISMARINE_TILE_SLAB.get());

        ModBlocks.TERRACOTTA.forEach(colored -> colored.sets().forEach(this::addPickaxeSet));
        ModBlocks.PLASTER.forEach(colored -> colored.sets().forEach(this::addPickaxeSet));
    }

    private void addPickaxeSet(ModBlocks.BlockSet set) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(set.block().get(), set.stairs().get(), set.slab().get());
    }
}
