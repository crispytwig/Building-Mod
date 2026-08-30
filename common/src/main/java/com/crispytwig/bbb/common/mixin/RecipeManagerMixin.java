package com.crispytwig.bbb.common.mixin;

import com.crispytwig.bbb.common.config.BlockConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"))
    private void bbb$removeRecipes(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (BlockConfig.disabledGroups().isEmpty()) {
            return;
        }
        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> keptByName = ImmutableMap.builder();
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> keptByType = ImmutableMultimap.builder();

        int removed = 0;
        for (Map.Entry<ResourceLocation, RecipeHolder<?>> entry : this.byName.entrySet()) {
            RecipeHolder<?> holder = entry.getValue();
            if (bbb$disabled(holder.value())) {
                removed++;
                continue;
            }
            keptByName.put(entry.getKey(), holder);
            keptByType.put(holder.value().getType(), holder);
        }

        if (removed == 0) {
            return;
        }
        this.byName = keptByName.build();
        this.byType = keptByType.build();
    }

    private boolean bbb$disabled(Recipe<?> recipe) {
        ItemStack result;
        try {
            result = recipe.getResultItem(this.registries);
        } catch (Exception ignored) {
            return false;
        }
        return !result.isEmpty() && !BlockConfig.isEnabled(result.getItem());
    }
}
