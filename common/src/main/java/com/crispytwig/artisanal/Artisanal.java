package com.crispytwig.artisanal;

import com.crispytwig.artisanal.registry.ModBlockEntities;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModCreativeTabs;
import com.crispytwig.artisanal.registry.ModDataComponents;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import com.crispytwig.artisanal.registry.ModLayers;
import com.crispytwig.artisanal.platform.Services;
import com.crispytwig.artisanal.registry.ModRecipeSerializers;
import com.crispytwig.artisanal.registry.ModTags;
import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Artisanal {
    public static final String MOD_ID = "artisanal";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Artisanal() {
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
        ModRecipeSerializers.init();
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
        ModBlocks.woodenBlocks().forEach(holder -> registrar.register(holder.get(), 5, 20));
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
        ModBlocks.woodenBlocks().forEach(holder -> registrar.register(holder.get(), holder.get() instanceof SlabBlock ? 150 : 300));
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
