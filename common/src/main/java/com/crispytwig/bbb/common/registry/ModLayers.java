package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.LayerBlock;
import com.crispytwig.bbb.common.block.WeatheringLayerBlock;
import com.crispytwig.bbb.common.data.LayerData;
import com.crispytwig.bbb.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
public final class ModLayers {
    private static final String SLAB_SUFFIX = "_slab";
    private static final String WAXED_PREFIX = "waxed_";

    private static final Map<ResourceLocation, Block> LAYERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> PARENTS = new LinkedHashMap<>();
    private static final List<Runnable> CALLBACKS = new ArrayList<>();

    private static boolean registered;

    private ModLayers() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        Map<ResourceLocation, ResourceLocation> found = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            ResourceLocation slabId = entry.getKey().location();
            Block slab = entry.getValue();
            if (slab instanceof SlabBlock
                    && slabId.getPath().endsWith(SLAB_SUFFIX)
                    && slab.asItem() != Items.AIR
                    && slab.defaultBlockState().getValues().size() == 2) {
                found.put(BuildingButBetter.location(layerName(slabId)), slabId);
            }
        }

        found.forEach((layerId, slabId) -> {
            Block slab = BuiltInRegistries.BLOCK.get(slabId);
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(slab);
            LayerBlock layer = Registry.register(BuiltInRegistries.BLOCK, layerId,
                    slab instanceof WeatheringCopperSlabBlock weathering
                            ? new WeatheringLayerBlock(weathering.getAge(), properties)
                            : new LayerBlock(properties));
            Registry.register(BuiltInRegistries.ITEM, layerId, new BlockItem(layer, new Item.Properties()));
            LAYERS.put(layerId, layer);
            PARENTS.put(layerId, slabId);
        });

        LayerData.generate(LAYERS, PARENTS);
        Services.PLATFORM.registerCopper(oxidation(), waxing());

        CALLBACKS.forEach(Runnable::run);
        CALLBACKS.clear();
    }

    public static void whenRegistered(Runnable callback) {
        if (registered) {
            callback.run();
        } else {
            CALLBACKS.add(callback);
        }
    }

    public static Map<ResourceLocation, Block> layers() {
        return Collections.unmodifiableMap(LAYERS);
    }

    public static Map<ResourceLocation, ResourceLocation> parents() {
        return Collections.unmodifiableMap(PARENTS);
    }

    private static Map<ResourceLocation, ResourceLocation> oxidation() {
        Map<ResourceLocation, ResourceLocation> pairs = new LinkedHashMap<>();
        LAYERS.forEach((layerId, layer) -> {
            if (layer instanceof WeatheringLayerBlock) {
                ResourceLocation previous = previousStage(layerId);
                if (previous != null && LAYERS.containsKey(previous)) {
                    pairs.put(previous, layerId);
                }
            }
        });
        return pairs;
    }

    private static Map<ResourceLocation, ResourceLocation> waxing() {
        Map<ResourceLocation, ResourceLocation> pairs = new LinkedHashMap<>();
        LAYERS.forEach((layerId, layer) -> {
            if (layerId.getPath().contains(WAXED_PREFIX)) {
                ResourceLocation unwaxed = BuildingButBetter.location(layerId.getPath().replace(WAXED_PREFIX, ""));
                if (LAYERS.containsKey(unwaxed)) {
                    pairs.put(unwaxed, layerId);
                }
            }
        });
        return pairs;
    }

    private static ResourceLocation previousStage(ResourceLocation layerId) {
        String path = layerId.getPath();
        if (path.contains("oxidized")) {
            return BuildingButBetter.location(path.replace("oxidized", "weathered"));
        }
        if (path.contains("weathered")) {
            return BuildingButBetter.location(path.replace("weathered", "exposed"));
        }
        if (path.contains("exposed_")) {
            return BuildingButBetter.location(path.replace("exposed_", ""));
        }
        return null;
    }

    private static String layerName(ResourceLocation slabId) {
        String path = slabId.getPath();
        String stem = path.substring(0, path.length() - SLAB_SUFFIX.length()) + "_layer";
        return slabId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? stem : slabId.getNamespace() + "/" + stem;
    }
}
