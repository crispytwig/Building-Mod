package com.crispytwig.bbb.neoforge.datagen.server;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.BeamBlock;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.common.registry.ModBlocks;
import com.crispytwig.bbb.common.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(output, lookup, BuildingButBetter.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (block instanceof StairBlock) {
                tag(BlockTags.STAIRS).add(block);
            } else if (block instanceof SlabBlock) {
                tag(BlockTags.SLABS).add(block);
            } else if (block instanceof BeamBlock) {
                tag(ModTags.CHAIN_CONNECTIBLE).add(block);
            }
        }

        ModBlocks.allWood().forEach(set -> tag(BlockTags.PLANKS).add(set.boards().get()));
        ModBlocks.COLORED_WOOD.forEach(set -> tag(BlockTags.PLANKS).add(set.plankBlock().get()));

        ModBlocks.woodenBlocks().forEach(holder -> tag(BlockTags.MINEABLE_WITH_AXE).add(holder.get()));
        ModBlocks.allWood().forEach(set -> set.windows().forEach(holder -> tag(BlockTags.MINEABLE_WITH_AXE).add(holder.get())));
        ModBlocks.lanterns().forEach(holder -> tag(BlockTags.MINEABLE_WITH_AXE).add(holder.get()));

        addPickaxeSet(ModBlocks.STONE_TILES);
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STONE_PILLAR.get(), ModBlocks.STONE_PILLAR_STAIRS.get(), ModBlocks.STONE_PILLAR_SLAB.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PRISMARINE_TILES.get(), ModBlocks.PRISMARINE_TILE_STAIRS.get(), ModBlocks.PRISMARINE_TILE_SLAB.get());

        tag(ModTags.PRISMARINE).add(
                Blocks.PRISMARINE, Blocks.PRISMARINE_STAIRS, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_WALL,
                Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE_BRICK_STAIRS, Blocks.PRISMARINE_BRICK_SLAB,
                Blocks.DARK_PRISMARINE, Blocks.DARK_PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE_SLAB,
                ModBlocks.PRISMARINE_TILES.get(), ModBlocks.PRISMARINE_TILE_STAIRS.get(), ModBlocks.PRISMARINE_TILE_SLAB.get());

        tag(ModTags.FACADE_MATERIALS);

        ModBlocks.TERRACOTTA.forEach(colored -> colored.sets().forEach(this::addPickaxeSet));
        ModBlocks.PLASTER.forEach(colored -> colored.sets().forEach(this::addPickaxeSet));
    }

    private void addPickaxeSet(ModBlocks.BlockSet set) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(set.block().get(), set.stairs().get(), set.slab().get());
    }
}
