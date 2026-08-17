package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
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

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OAK_PILLAR.get(), 8)
                .group("pillar")
                .pattern("L")
                .pattern("L")
                .define('L', ItemTags.OAK_LOGS)
                .unlockedBy("has_logs", has(ItemTags.OAK_LOGS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OAK_BEAM.get(), 8)
                .group("beam")
                .pattern("P")
                .pattern("P")
                .define('P', ModBlocks.OAK_PILLAR.get())
                .unlockedBy(getHasName(ModBlocks.OAK_PILLAR.get()), has(ModBlocks.OAK_PILLAR.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_TABLE.get(), 2)
                .pattern("PPP")
                .pattern("P P")
                .define('P', Items.OAK_PLANKS)
                .unlockedBy("has_planks", has(Items.OAK_PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_CHAIR.get())
                .pattern("P  ")
                .pattern("PPP")
                .pattern("P P")
                .define('P', Items.OAK_PLANKS)
                .unlockedBy("has_planks", has(Items.OAK_PLANKS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OAK_WINDOW.get(), 4)
                .group("window")
                .pattern("PGP")
                .define('P', Items.OAK_PLANKS)
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.OAK_WINDOW_PANE.get(), 16)
                .group("window_pane")
                .pattern("WWW")
                .pattern("WWW")
                .define('W', ModBlocks.OAK_WINDOW.get())
                .unlockedBy("has_window", has(ModBlocks.OAK_WINDOW.get()))
                .save(output);

        for (ModBlocks.ColoredSet colored : ModBlocks.TERRACOTTA) {
            Block source = ModBlocks.vanillaTerracotta(colored.color());
            colored.sets().forEach(set -> terracottaRecipes(output, set, source));
        }

        for (ModBlocks.ColoredSet colored : ModBlocks.PLASTER) {
            colored.sets().forEach(set -> plasterRecipes(output, set, colored.color()));
        }
    }

    private void terracottaRecipes(RecipeOutput output, ModBlocks.BlockSet set, Block source) {
        ItemLike block = set.block().get();

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block, 4)
                .group(group(block))
                .define('#', source)
                .pattern("##")
                .pattern("##")
                .unlockedBy(getHasName(source), has(source))
                .save(output);

        shapeRecipes(output, set);

        stonecutting(output, source, block, 1);
        stonecutting(output, source, set.stairs().get(), 1);
        stonecutting(output, source, set.slab().get(), 2);
        stonecutting(output, block, set.stairs().get(), 1);
        stonecutting(output, block, set.slab().get(), 2);
    }

    private void plasterRecipes(RecipeOutput output, ModBlocks.BlockSet set, DyeColor color) {
        ItemLike block = set.block().get();

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, block, 8)
                .group(group(block))
                .requires(DyeItem.byColor(color))
                .requires(Ingredient.of(Tags.Items.SANDS), 4)
                .requires(Items.CLAY_BALL, 4)
                .unlockedBy(getHasName(Items.CLAY_BALL), has(Items.CLAY_BALL))
                .save(output);

        shapeRecipes(output, set);

        stonecutting(output, block, set.stairs().get(), 1);
        stonecutting(output, block, set.slab().get(), 2);
    }

    private void shapeRecipes(RecipeOutput output, ModBlocks.BlockSet set) {
        ItemLike block = set.block().get();

        stairBuilder(set.stairs().get(), Ingredient.of(block))
                .group(group(set.stairs().get()))
                .unlockedBy(getHasName(block), has(block))
                .save(output);

        slabBuilder(RecipeCategory.BUILDING_BLOCKS, set.slab().get(), Ingredient.of(block))
                .group(group(set.slab().get()))
                .unlockedBy(getHasName(block), has(block))
                .save(output);
    }

    private void stonecutting(RecipeOutput output, ItemLike input, ItemLike result, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
                .group(group(result))
                .unlockedBy(getHasName(input), has(input))
                .save(output, Artisanal.location(getConversionRecipeName(result, input) + "_stonecutting"));
    }

    private static String group(ItemLike item) {
        String name = getItemName(item);
        for (DyeColor color : DyeColor.values()) {
            if (name.startsWith(color.getName() + "_")) {
                return name.substring(color.getName().length() + 1);
            }
        }
        return name;
    }
}
