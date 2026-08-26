package com.crispytwig.artisanal.recipe;

import com.crispytwig.artisanal.item.FacadeItem;
import com.crispytwig.artisanal.registry.ModItems;
import com.crispytwig.artisanal.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FacadeRecipe implements CraftingRecipe {
    private static Ingredient materials;

    private final CraftingBookCategory category;

    public FacadeRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Nullable
    private static Block material(CraftingInput input) {
        Block material = null;
        boolean membrane = false;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.PHANTOM_MEMBRANE)) {
                if (membrane) {
                    return null;
                }
                membrane = true;
                continue;
            }
            if (material != null || !(stack.getItem() instanceof BlockItem item) || !FacadeItem.isMaterial(item.getBlock())) {
                return null;
            }
            material = item.getBlock();
        }
        return membrane ? material : null;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return material(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Block material = material(input);
        return material == null ? ItemStack.EMPTY : FacadeItem.of(material, 8);
    }

    private static Ingredient materials() {
        if (materials == null) {
            materials = Ingredient.of(BuiltInRegistries.BLOCK.stream()
                    .filter(FacadeItem::isMaterial)
                    .map(ItemStack::new)
                    .filter(stack -> !stack.isEmpty()));
        }
        return materials;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.PHANTOM_MEMBRANE), materials());
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return new ItemStack(ModItems.FACADE.get(), 8);
    }

    @Override
    public @NotNull CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FACADE.get();
    }
}
