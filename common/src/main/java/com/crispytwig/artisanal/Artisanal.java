package com.crispytwig.artisanal;

import com.crispytwig.artisanal.registry.ModBlockEntities;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModCreativeTabs;
import com.crispytwig.artisanal.registry.ModDataComponents;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import com.crispytwig.artisanal.registry.ModRecipeSerializers;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

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

    public static void registerFlammability(FlammabilityRegistrar registrar) {
        registrar.register(ModBlocks.OAK_TRIM.get(), 5, 20);
        registrar.register(ModBlocks.OAK_TRIM_STAIRS.get(), 5, 20);
        registrar.register(ModBlocks.OAK_TRIM_SLAB.get(), 5, 20);
    }

    @FunctionalInterface
    public interface FuelRegistrar {
        void register(ItemLike item, int burnTime);
    }

    public static void registerFuels(FuelRegistrar registrar) {
        registrar.register(ModBlocks.OAK_TRIM.get(), 300);
        registrar.register(ModBlocks.OAK_TRIM_STAIRS.get(), 300);
        registrar.register(ModBlocks.OAK_TRIM_SLAB.get(), 150);
    }
}
