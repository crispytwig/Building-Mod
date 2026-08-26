package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import com.crispytwig.artisanal.recipe.FacadeRecipe;
import com.crispytwig.artisanal.recipe.PolishingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Artisanal.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FacadeRecipe>> FACADE =
            RECIPE_SERIALIZERS.register("facade", () -> new SimpleCraftingRecipeSerializer<>(FacadeRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, PolishingRecipe.Serializer> POLISHING =
            RECIPE_SERIALIZERS.register("polishing", PolishingRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void init() {
    }
}
