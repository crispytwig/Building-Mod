package com.crispytwig.bbb;

import com.crispytwig.bbb.registry.ModBlockEntities;
import com.crispytwig.bbb.registry.ModBlocks;
import com.crispytwig.bbb.registry.ModCreativeTabs;
import com.crispytwig.bbb.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class BuildingButBetter {
    public static final String MOD_ID = "bbb";
    public static final Logger LOGGER = LogUtils.getLogger();

    private BuildingButBetter() {
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void bootstrap() {
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModCreativeTabs.init();
    }
}
