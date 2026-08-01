package com.crispytwig.artisanal.recipe;

import com.crispytwig.artisanal.item.PanelItem;
import com.crispytwig.artisanal.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PanelRecipe extends CustomRecipe {
    public PanelRecipe(CraftingBookCategory category) {
        super(category);
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
            if (material != null || !(stack.getItem() instanceof BlockItem item) || !PanelItem.isMaterial(item.getBlock())) {
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
        return material == null ? ItemStack.EMPTY : PanelItem.of(material, 8);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PANEL.get();
    }
}
