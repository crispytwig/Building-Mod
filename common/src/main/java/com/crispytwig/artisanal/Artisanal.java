package com.crispytwig.artisanal;

import com.crispytwig.artisanal.entity.Wright;
import com.crispytwig.artisanal.registry.ModBlockEntities;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModCreativeTabs;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
        ModItems.init();
        ModBlockEntities.init();
        ModCreativeTabs.init();
    }

    @FunctionalInterface
    public interface AttributeRegistrar {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    public static void createAttributes(AttributeRegistrar registrar) {
        registrar.register(ModEntityTypes.WRIGHT.get(), Wright.createAttributes());
    }
}
