package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PRISMARINE_TILES.get(), 4)
                .define('#', Blocks.PRISMARINE_BRICKS)
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_prismarine_bricks", has(Blocks.PRISMARINE_BRICKS))
                .save(output);

        stairBuilder(ModBlocks.PRISMARINE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.PRISMARINE_TILES.get()))
                .unlockedBy("has_prismarine_tiles", has(ModBlocks.PRISMARINE_TILES.get()))
                .save(output);

        slab(output, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PRISMARINE_TILE_SLAB.get(), ModBlocks.PRISMARINE_TILES.get());
    }
}
