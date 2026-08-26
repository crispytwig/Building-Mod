package com.crispytwig.bbb.neoforge.datagen.server;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.registry.ModBlocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModCompatRecipeProvider implements DataProvider {
    private static final String CLAYWORKS = "clayworks";

    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider advancements;

    public ModCompatRecipeProvider(PackOutput output) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.advancements = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (ModBlocks.ColoredSet colored : ModBlocks.TERRACOTTA) {
            Block terracotta = ModBlocks.vanillaTerracotta(colored.color());
            Block cobbled = colored.sets().get(0).block().get();
            futures.add(baking(output, cobbled, terracotta, 0.1F, 100));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> baking(CachedOutput output, ItemLike ingredient, ItemLike result, float experience, int cookingTime) {
        ResourceLocation id = BuildingButBetter.location(name(result) + "_from_baking_" + name(ingredient));

        JsonObject recipe = new JsonObject();
        recipe.add("neoforge:conditions", neoForgeConditions());
        recipe.add("fabric:load_conditions", fabricConditions());
        recipe.addProperty("type", CLAYWORKS + ":baking");
        recipe.addProperty("category", "blocks");
        recipe.addProperty("cookingtime", cookingTime);
        recipe.addProperty("experience", experience);
        recipe.addProperty("group", ModRecipeProvider.group(result));

        JsonObject ingredientJson = new JsonObject();
        ingredientJson.addProperty("item", key(ingredient).toString());
        recipe.add("ingredient", ingredientJson);

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("count", 1);
        resultJson.addProperty("id", key(result).toString());
        recipe.add("result", resultJson);

        return CompletableFuture.allOf(
                DataProvider.saveStable(output, recipe, this.recipes.json(id)),
                DataProvider.saveStable(output, advancement(id, ingredient), this.advancements.json(id.withPrefix("recipes/building_blocks/")))
        );
    }

    private JsonObject advancement(ResourceLocation recipeId, ItemLike ingredient) {
        String hasIngredient = "has_" + name(ingredient);

        JsonObject items = new JsonObject();
        items.addProperty("items", key(ingredient).toString());
        JsonArray itemsArray = new JsonArray();
        itemsArray.add(items);
        JsonObject inventoryConditions = new JsonObject();
        inventoryConditions.add("items", itemsArray);
        JsonObject inventoryChanged = new JsonObject();
        inventoryChanged.add("conditions", inventoryConditions);
        inventoryChanged.addProperty("trigger", "minecraft:inventory_changed");

        JsonObject recipeConditions = new JsonObject();
        recipeConditions.addProperty("recipe", recipeId.toString());
        JsonObject recipeUnlocked = new JsonObject();
        recipeUnlocked.add("conditions", recipeConditions);
        recipeUnlocked.addProperty("trigger", "minecraft:recipe_unlocked");

        JsonObject criteria = new JsonObject();
        criteria.add(hasIngredient, inventoryChanged);
        criteria.add("has_the_recipe", recipeUnlocked);

        JsonArray requirement = new JsonArray();
        requirement.add("has_the_recipe");
        requirement.add(hasIngredient);
        JsonArray requirements = new JsonArray();
        requirements.add(requirement);

        JsonArray recipeRewards = new JsonArray();
        recipeRewards.add(recipeId.toString());
        JsonObject rewardedRecipes = new JsonObject();
        rewardedRecipes.add("recipes", recipeRewards);

        JsonObject advancement = new JsonObject();
        advancement.add("neoforge:conditions", neoForgeConditions());
        advancement.add("fabric:load_conditions", fabricConditions());
        advancement.addProperty("parent", "minecraft:recipes/root");
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements);
        advancement.add("rewards", rewardedRecipes);
        return advancement;
    }

    private static JsonArray neoForgeConditions() {
        JsonObject modLoaded = new JsonObject();
        modLoaded.addProperty("type", "neoforge:mod_loaded");
        modLoaded.addProperty("modid", CLAYWORKS);
        JsonArray conditions = new JsonArray();
        conditions.add(modLoaded);
        return conditions;
    }

    private static JsonArray fabricConditions() {
        JsonArray values = new JsonArray();
        values.add(CLAYWORKS);
        JsonObject allModsLoaded = new JsonObject();
        allModsLoaded.addProperty("condition", "fabric:all_mods_loaded");
        allModsLoaded.add("values", values);
        JsonArray conditions = new JsonArray();
        conditions.add(allModsLoaded);
        return conditions;
    }

    private static ResourceLocation key(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }

    private static String name(ItemLike item) {
        return key(item).getPath();
    }

    @Override
    public String getName() {
        return "Mod Compat Recipes";
    }
}
