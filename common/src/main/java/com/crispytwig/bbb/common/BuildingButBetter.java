package com.crispytwig.bbb.common;

import com.crispytwig.bbb.common.registry.ModBlockEntities;
import com.crispytwig.bbb.common.registry.ModBlocks;
import com.crispytwig.bbb.common.registry.ModCreativeTabs;
import com.crispytwig.bbb.common.registry.ModDataComponents;
import com.crispytwig.bbb.common.registry.ModEntityTypes;
import com.crispytwig.bbb.common.registry.ModItems;
import com.crispytwig.bbb.common.registry.ModLayers;
import com.crispytwig.bbb.common.registry.ModMenuTypes;
import com.crispytwig.bbb.platform.Services;
import com.crispytwig.bbb.common.registry.ModRecipeSerializers;
import com.crispytwig.bbb.common.registry.ModSounds;
import com.crispytwig.bbb.common.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class BuildingButBetter {
    public static final String MOD_ID = "bbb";

    private BuildingButBetter() {
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void bootstrap() {
        ModBlocks.init();
        ModEntityTypes.init();
        ModDataComponents.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenuTypes.init();
        ModRecipeSerializers.init();
        ModSounds.init();
        ModCreativeTabs.init();
    }

    @FunctionalInterface
    public interface AttributeRegistrar {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    public static void createAttributes(AttributeRegistrar registrar) {
    }

    @FunctionalInterface
    public interface FlammabilityRegistrar {
        void register(Block block, int encouragement, int flammability);
    }

    public record Flammability(int encouragement, int flammability) {
    }

    public static void registerFlammability(FlammabilityRegistrar registrar) {
        ModBlocks.burnableBlocks().forEach(holder -> registrar.register(holder.get(), 5, 20));
    }

    public static void registerLayerFlammability(FlammabilityRegistrar registrar) {
        ModLayers.parents().forEach((layerId, slabId) -> {
            Flammability parent = Services.PLATFORM.flammability(BuiltInRegistries.BLOCK.get(slabId));
            if (parent != null) {
                registrar.register(ModLayers.layers().get(layerId), parent.encouragement(), parent.flammability());
            }
        });
    }

    @FunctionalInterface
    public interface FuelRegistrar {
        void register(ItemLike item, int burnTime);
    }

    public static void registerFuels(FuelRegistrar registrar) {
        ModBlocks.burnableBlocks().forEach(holder -> registrar.register(holder.get(), holder.get() instanceof SlabBlock ? 150 : 300));
    }

    @FunctionalInterface
    public interface FuelTagRegistrar {
        void register(TagKey<Item> tag, int burnTime);
    }

    public static void registerFuelTags(FuelTagRegistrar registrar) {
        registrar.register(ModTags.WOODEN_LAYERS, 75);
    }

    public static String titleCase(String path) {
        return Arrays.stream(path.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
