package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import com.crispytwig.bbb.common.recipe.FacadeRecipe;
import com.crispytwig.bbb.common.recipe.PolishingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FacadeRecipe>> FACADE =
            RECIPE_SERIALIZERS.register("facade", () -> new SimpleCraftingRecipeSerializer<>(FacadeRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, PolishingRecipe.Serializer> POLISHING =
            RECIPE_SERIALIZERS.register("polishing", PolishingRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void init() {
    }
}
