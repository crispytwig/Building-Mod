package com.crispytwig.artisanal.data;

import com.crispytwig.artisanal.block.LayerBlock;
import com.crispytwig.artisanal.registry.ModTags;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
public final class LayerData {
    private static final List<SoundType> WOODEN = List.of(SoundType.WOOD, SoundType.BAMBOO_WOOD, SoundType.CHERRY_WOOD, SoundType.NETHER_WOOD);
    private static final List<SoundType> DIGGABLE = List.of(SoundType.GRAVEL, SoundType.GRASS, SoundType.SAND, SoundType.SNOW);

    private LayerData() {
    }

    public static void generate(Map<ResourceLocation, Block> layers, Map<ResourceLocation, ResourceLocation> parents) {
        ArtisanalPack.INSTANCE.clear(PackType.SERVER_DATA);

        Map<ResourceLocation, JsonArray> tags = new LinkedHashMap<>();
        JsonArray all = new JsonArray();
        JsonArray wooden = new JsonArray();

        layers.forEach((id, layer) -> {
            ResourceLocation slabId = parents.get(id);
            SoundType sound = layer.defaultBlockState().getSoundType();
            write(id.getNamespace(), "loot_table/blocks/" + id.getPath(), lootTable(id));
            write(id.getNamespace(), "recipe/" + id.getPath(), recipe(id, slabId));
            write(id.getNamespace(), "advancement/recipes/building_blocks/" + id.getPath(), recipeAdvancement(id, slabId));
            tags.computeIfAbsent(mineable(sound), ignored -> new JsonArray()).add(entry(id));
            all.add(entry(id));
            if (WOODEN.contains(sound)) {
                wooden.add(entry(id));
            }
        });

        tags.forEach((tag, values) -> write(tag.getNamespace(), "tags/block/" + tag.getPath(), tagFile(values)));
        blockAndItemTag(ModTags.LAYERS.location(), all);
        blockAndItemTag(ModTags.WOODEN_LAYERS.location(), wooden);
    }

    private static JsonObject recipe(ResourceLocation layerId, ResourceLocation slabId) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", slabId.toString());

        JsonObject key = new JsonObject();
        key.add("#", ingredient);

        JsonArray pattern = new JsonArray();
        pattern.add("###");

        JsonObject result = new JsonObject();
        result.addProperty("count", 6);
        result.addProperty("id", layerId.toString());

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "building");
        recipe.addProperty("group", "layer");
        recipe.add("key", key);
        recipe.add("pattern", pattern);
        recipe.add("result", result);
        return recipe;
    }

    private static JsonObject recipeAdvancement(ResourceLocation layerId, ResourceLocation slabId) {
        JsonObject slab = new JsonObject();
        slab.addProperty("items", slabId.toString());
        JsonArray items = new JsonArray();
        items.add(slab);

        JsonObject hasSlabConditions = new JsonObject();
        hasSlabConditions.add("items", items);
        JsonObject hasSlab = new JsonObject();
        hasSlab.add("conditions", hasSlabConditions);
        hasSlab.addProperty("trigger", "minecraft:inventory_changed");

        JsonObject hasRecipeConditions = new JsonObject();
        hasRecipeConditions.addProperty("recipe", layerId.toString());
        JsonObject hasRecipe = new JsonObject();
        hasRecipe.add("conditions", hasRecipeConditions);
        hasRecipe.addProperty("trigger", "minecraft:recipe_unlocked");

        String slabCriterion = "has_" + slabId.getPath();
        JsonObject criteria = new JsonObject();
        criteria.add(slabCriterion, hasSlab);
        criteria.add("has_the_recipe", hasRecipe);

        JsonArray anyOf = new JsonArray();
        anyOf.add("has_the_recipe");
        anyOf.add(slabCriterion);
        JsonArray requirements = new JsonArray();
        requirements.add(anyOf);

        JsonArray recipes = new JsonArray();
        recipes.add(layerId.toString());
        JsonObject rewards = new JsonObject();
        rewards.add("recipes", recipes);

        JsonObject advancement = new JsonObject();
        advancement.addProperty("parent", "minecraft:recipes/root");
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements);
        advancement.add("rewards", rewards);
        return advancement;
    }

    private static void blockAndItemTag(ResourceLocation tag, JsonArray values) {
        write(tag.getNamespace(), "tags/block/" + tag.getPath(), tagFile(values));
        write(tag.getNamespace(), "tags/item/" + tag.getPath(), tagFile(values));
    }

    private static ResourceLocation mineable(SoundType sound) {
        if (WOODEN.contains(sound)) {
            return ResourceLocation.withDefaultNamespace("mineable/axe");
        }
        return ResourceLocation.withDefaultNamespace(DIGGABLE.contains(sound) ? "mineable/shovel" : "mineable/pickaxe");
    }

    private static void write(String namespace, String path, JsonObject json) {
        ArtisanalPack.INSTANCE.add(PackType.SERVER_DATA, ResourceLocation.fromNamespaceAndPath(namespace, path + ".json"), json);
    }

    private static JsonObject entry(ResourceLocation id) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id.toString());
        entry.addProperty("required", false);
        return entry;
    }

    private static JsonObject tagFile(JsonArray values) {
        JsonObject tag = new JsonObject();
        tag.addProperty("replace", false);
        tag.add("values", values);
        return tag;
    }

    private static JsonObject lootTable(ResourceLocation id) {
        JsonArray children = new JsonArray();
        for (int layers = LayerBlock.MAX_LAYERS; layers > 1; layers--) {
            children.add(drop(id, layers));
        }
        children.add(drop(id, 1));

        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        alternatives.add("children", children);

        JsonArray entries = new JsonArray();
        entries.add(alternatives);

        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survives);

        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.addProperty("bonus_rolls", 0);
        pool.add("entries", entries);
        pool.add("conditions", conditions);

        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject lootTable = new JsonObject();
        lootTable.addProperty("type", "minecraft:block");
        lootTable.add("pools", pools);
        lootTable.addProperty("random_sequence", id.getNamespace() + ":blocks/" + id.getPath());
        return lootTable;
    }

    private static JsonObject drop(ResourceLocation id, int layers) {
        JsonObject drop = new JsonObject();
        drop.addProperty("type", "minecraft:item");
        drop.addProperty("name", id.toString());

        if (layers > 1) {
            JsonObject properties = new JsonObject();
            properties.addProperty("layers", String.valueOf(layers));

            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:block_state_property");
            condition.addProperty("block", id.toString());
            condition.add("properties", properties);

            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            drop.add("conditions", conditions);

            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "minecraft:set_count");
            setCount.addProperty("count", layers);

            JsonArray functions = new JsonArray();
            functions.add(setCount);
            drop.add("functions", functions);
        }

        return drop;
    }
}
