package com.crispytwig.bbb.common.recipe;

import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.paint.BlockRecolor;
import com.crispytwig.bbb.common.registry.ModItems;
import com.crispytwig.bbb.common.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintingRecipe implements CraftingRecipe {
    private final CraftingBookCategory category;

    public PaintingRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    private record Painting(ItemStack brush, ItemStack result, int painted) {
    }

    @Nullable
    private static Painting painting(CraftingInput input) {
        ItemStack brush = ItemStack.EMPTY;
        Block source = null;
        int painted = 0;

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof PaintBrushItem) {
                if (!brush.isEmpty()) {
                    return null;
                }
                brush = stack;
                continue;
            }
            if (!(stack.getItem() instanceof BlockItem item) || (source != null && item.getBlock() != source)) {
                return null;
            }
            source = item.getBlock();
            painted++;
        }

        if (brush.isEmpty() || source == null) {
            return null;
        }

        DyeColor color = PaintBrushItem.getColor(brush);
        if (color == null || brush.getMaxDamage() - brush.getDamageValue() < painted) {
            return null;
        }

        Block result = BlockRecolor.paint(source.defaultBlockState(), color)
                .map(BlockState::getBlock)
                .orElse(null);
        if (result == null || result == source) {
            return null;
        }

        ItemStack stack = new ItemStack(result, painted);
        return stack.isEmpty() ? null : new Painting(brush, stack, painted);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return painting(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Painting painting = painting(input);
        return painting == null ? ItemStack.EMPTY : painting.result();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        Painting painting = painting(input);
        if (painting == null) {
            return remaining;
        }

        for (int slot = 0; slot < remaining.size(); slot++) {
            if (!(input.getItem(slot).getItem() instanceof PaintBrushItem)) {
                continue;
            }
            ItemStack brush = input.getItem(slot).copy();
            brush.setDamageValue(brush.getDamageValue() + painting.painted());
            remaining.set(slot, brush.getDamageValue() >= brush.getMaxDamage() ? ItemStack.EMPTY : brush);
            break;
        }
        return remaining;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.PAINT_BRUSH.get()));
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
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
        return ModRecipeSerializers.PAINTING.get();
    }
}
