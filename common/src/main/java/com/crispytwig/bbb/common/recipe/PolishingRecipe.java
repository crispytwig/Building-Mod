package com.crispytwig.bbb.common.recipe;

import com.crispytwig.bbb.common.registry.ModRecipeSerializers;
import com.crispytwig.bbb.common.registry.ModTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PolishingRecipe extends ShapelessRecipe {
    private final ItemStack output;

    public PolishingRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.output = result;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < remaining.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(ModTags.ABRASIVES)) {
                remaining.set(slot, stack.copyWithCount(1));
            } else if (stack.getItem().hasCraftingRemainingItem()) {
                remaining.set(slot, new ItemStack(stack.getItem().getCraftingRemainingItem()));
            }
        }
        return remaining;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.POLISHING.get();
    }

    public static class Serializer implements RecipeSerializer<PolishingRecipe> {
        private static final MapCodec<PolishingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients")
                        .xmap(ingredients -> NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)), List::copyOf)
                        .forGetter(ShapelessRecipe::getIngredients)
        ).apply(instance, PolishingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, PolishingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, ShapelessRecipe::getGroup,
                        CraftingBookCategory.STREAM_CODEC, ShapelessRecipe::category,
                        ItemStack.STREAM_CODEC, recipe -> recipe.output,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)), ShapelessRecipe::getIngredients,
                        PolishingRecipe::new);

        @Override
        public @NotNull MapCodec<PolishingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, PolishingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
