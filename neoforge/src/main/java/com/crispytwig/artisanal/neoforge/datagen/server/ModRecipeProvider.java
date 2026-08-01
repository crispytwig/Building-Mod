package com.crispytwig.artisanal.neoforge.datagen.server;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.recipe.PanelRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        SpecialRecipeBuilder.special(PanelRecipe::new).save(output, Artisanal.location("panel"));
    }
}
