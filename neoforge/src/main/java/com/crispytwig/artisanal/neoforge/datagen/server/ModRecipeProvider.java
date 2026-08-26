package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.recipe.FacadeRecipe;
import com.crispytwig.artisanal.recipe.PolishingRecipe;
import com.crispytwig.artisanal.registry.ModBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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
        facadeRecipe(output);

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

        ModBlocks.WOOD.forEach(set -> woodRecipes(output, set));

        ModBlocks.TERRACOTTA.forEach(colored -> terracottaRecipes(output, colored));

        for (ModBlocks.ColoredSet colored : ModBlocks.PLASTER) {
            colored.sets().forEach(set -> plasterRecipes(output, set, colored.color()));
        }
    }

    private void woodRecipes(RecipeOutput output, ModBlocks.WoodSet set) {
        Block boards = set.boards().get();
        Block polished = set.polished().get();
        Block trim = set.trim().get();
        Block pillar = set.pillar().get();

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, boards, 3)
                .group("boards")
                .pattern("P")
                .pattern("P")
                .pattern("P")
                .define('P', set.planks())
                .unlockedBy("has_planks", has(set.planks()))
                .save(output);

        stairBuilder(set.boardStairs().get(), Ingredient.of(boards))
                .group("board_stairs")
                .unlockedBy(getHasName(boards), has(boards))
                .save(output);

        slab(output, RecipeCategory.BUILDING_BLOCKS, set.boardSlab().get(), boards);

        polishing(output, polished, boards);

        slab(output, RecipeCategory.BUILDING_BLOCKS, set.polishedSlab().get(), polished);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, trim)
                .group("trim")
                .pattern("S")
                .pattern("S")
                .define('S', set.boardSlab().get())
                .unlockedBy(getHasName(set.boardSlab().get()), has(set.boardSlab().get()))
                .save(output);

        stairBuilder(set.trimStairs().get(), Ingredient.of(trim))
                .group("trim_stairs")
                .unlockedBy(getHasName(trim), has(trim))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pillar, 8)
                .group("pillar")
                .pattern("L")
                .pattern("L")
                .define('L', set.logs())
                .unlockedBy("has_logs", has(set.logs()))
                .save(output);

        stairBuilder(set.pillarStairs().get(), Ingredient.of(pillar))
                .group("pillar_stairs")
                .unlockedBy(getHasName(pillar), has(pillar))
                .save(output);

        slab(output, RecipeCategory.BUILDING_BLOCKS, set.pillarSlab().get(), pillar);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.beam().get(), 8)
                .group("beam")
                .pattern("P")
                .pattern("P")
                .define('P', pillar)
                .unlockedBy(getHasName(pillar), has(pillar))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, set.frame().get(), 4)
                .group("frame")
                .pattern("BSB")
                .pattern("S S")
                .pattern("BSB")
                .define('B', boards)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(boards), has(boards))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.timberFrame().get(), 4)
                .group("timber_frame")
                .pattern("B B")
                .pattern(" B ")
                .pattern("B B")
                .define('B', boards)
                .unlockedBy(getHasName(boards), has(boards))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, set.shutter().get(), 2)
                .group("shutter")
                .pattern("BB")
                .pattern("SS")
                .pattern("BB")
                .define('B', boards)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(boards), has(boards))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, set.table().get(), 2)
                .group("table")
                .pattern("###")
                .pattern("S S")
                .define('#', set.slab())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(set.slab()), has(set.slab()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, set.chair().get())
                .group("chair")
                .pattern("P ")
                .pattern("PP")
                .pattern("SS")
                .define('P', set.planks())
                .define('S', Items.STICK)
                .unlockedBy("has_planks", has(set.planks()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, set.window().get(), 4)
                .group("window")
                .pattern("PGP")
                .define('P', set.planks())
                .define('G', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, set.windowPane().get(), 16)
                .group("window_pane")
                .pattern("WWW")
                .pattern("WWW")
                .define('W', set.window().get())
                .unlockedBy("has_window", has(set.window().get()))
                .save(output);
    }

    private void facadeRecipe(RecipeOutput output) {
        ResourceLocation id = Artisanal.location("facade");
        output.accept(id, new FacadeRecipe(CraftingBookCategory.BUILDING), Advancement.Builder.recipeAdvancement()
                .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                .addCriterion(getHasName(Items.PHANTOM_MEMBRANE), has(Items.PHANTOM_MEMBRANE))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(id.withPrefix("recipes/misc/")));
    }

    private void polishing(RecipeOutput output, Block result, Block ingredient) {
        ResourceLocation id = Artisanal.location(getItemName(result));
        PolishingRecipe recipe = new PolishingRecipe("polished_wood", CraftingBookCategory.BUILDING,
                new ItemStack(result),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ingredient), Ingredient.of(Tags.Items.SANDS)));
        output.accept(id, recipe, Advancement.Builder.recipeAdvancement()
                .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                .addCriterion(getHasName(ingredient), has(ingredient))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR)
                .build(id.withPrefix("recipes/building_blocks/")));
    }

    private void terracottaRecipes(RecipeOutput output, ModBlocks.ColoredSet colored) {
        Block terracotta = ModBlocks.vanillaTerracotta(colored.color());
        ModBlocks.BlockSet cobbled = colored.sets().get(0);
        ModBlocks.BlockSet tiles = colored.sets().get(1);
        ModBlocks.BlockSet bricks = colored.sets().get(2);
        ModBlocks.BlockSet shingles = colored.sets().get(3);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(cobbled.block().get()), RecipeCategory.BUILDING_BLOCKS, terracotta, 0.1F, 200)
                .group(group(terracotta))
                .unlockedBy(getHasName(cobbled.block().get()), has(cobbled.block().get()))
                .save(output, Artisanal.location(getItemName(terracotta) + "_from_smelting_" + getItemName(cobbled.block().get())));

        shapeRecipes(output, cobbled);
        stonecutting(output, cobbled.block().get(), cobbled.stairs().get(), 1);
        stonecutting(output, cobbled.block().get(), cobbled.slab().get(), 2);

        terracottaShape(output, tiles, terracotta);
        terracottaShape(output, bricks, tiles.block().get());
        terracottaShape(output, shingles, bricks.block().get());
    }

    private void terracottaShape(RecipeOutput output, ModBlocks.BlockSet set, ItemLike source) {
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
