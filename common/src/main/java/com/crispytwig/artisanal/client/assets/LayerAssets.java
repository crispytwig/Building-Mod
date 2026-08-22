package com.crispytwig.artisanal.client.assets;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.LayerBlock;
import com.crispytwig.artisanal.data.ArtisanalPack;
import com.crispytwig.artisanal.registry.ModLayers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
public final class LayerAssets {
    private static final List<String> FACES = List.of("top", "side", "bottom");
    private static final int MAX_TEXTURE_DEPTH = 4;

    private static final Map<ResourceLocation, JsonObject> CACHE = new HashMap<>();

    private static final Map<Direction, int[]> ROTATIONS = Map.of(
            Direction.UP, new int[]{0, 0},
            Direction.DOWN, new int[]{180, 0},
            Direction.NORTH, new int[]{90, 0},
            Direction.SOUTH, new int[]{90, 180},
            Direction.WEST, new int[]{90, 270},
            Direction.EAST, new int[]{90, 90});

    private LayerAssets() {
    }

    public static void generate(ResourceManager manager) {
        ArtisanalPack.INSTANCE.clear(PackType.CLIENT_RESOURCES);
        CACHE.clear();

        JsonObject lang = new JsonObject();
        ModLayers.parents().forEach((layerId, slabId) -> {
            lang.addProperty("block." + Artisanal.MOD_ID + "." + layerId.getPath().replace('/', '.'), title(layerId));
            generate(manager, layerId, slabId);
        });
        write("lang/en_us", lang);
        CACHE.clear();
    }

    private static void generate(ResourceManager manager, ResourceLocation layerId, ResourceLocation slabId) {
        if (manager.getResource(layerId.withPrefix("blockstates/").withSuffix(".json")).isPresent()) {
            return;
        }

        JsonObject textures = textures(manager, slabId);
        if (textures == null) {
            return;
        }

        String model = Artisanal.MOD_ID + ":block/" + layerId.getPath();
        for (int layers = 1; layers <= LayerBlock.MAX_LAYERS; layers++) {
            write("models/block/" + layerId.getPath() + "_" + layers, model(layers < LayerBlock.MAX_LAYERS
                    ? Artisanal.MOD_ID + ":block/template/layer_" + layers
                    : "minecraft:block/cube_bottom_top", textures));
        }
        write("models/item/" + layerId.getPath(), model(model + "_1", null));

        JsonObject variants = new JsonObject();
        ROTATIONS.forEach((facing, rotation) -> {
            for (int layers = 1; layers <= LayerBlock.MAX_LAYERS; layers++) {
                JsonObject variant = new JsonObject();
                variant.addProperty("model", model + "_" + layers);
                variant.addProperty("uvlock", true);
                if (layers < LayerBlock.MAX_LAYERS) {
                    if (rotation[0] != 0) {
                        variant.addProperty("x", rotation[0]);
                    }
                    if (rotation[1] != 0) {
                        variant.addProperty("y", rotation[1]);
                    }
                }
                variants.add("facing=" + facing.getSerializedName() + ",layers=" + layers, variant);
            }
        });

        JsonObject blockState = new JsonObject();
        blockState.add("variants", variants);
        write("blockstates/" + layerId.getPath(), blockState);
    }

    private static JsonObject model(String parent, @Nullable JsonObject textures) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", parent);
        if (textures != null) {
            model.add("textures", textures);
        }
        return model;
    }

    private static void write(String path, JsonObject json) {
        ArtisanalPack.INSTANCE.add(PackType.CLIENT_RESOURCES, Artisanal.location(path + ".json"), json);
    }

    private static @Nullable JsonObject textures(ResourceManager manager, ResourceLocation slabId) {
        ResourceLocation modelId = slabModel(manager, slabId);
        if (modelId == null) {
            return null;
        }

        JsonObject inherited = new JsonObject();
        for (ResourceLocation current = modelId; current != null; ) {
            JsonObject model = read(manager, current.withPrefix("models/").withSuffix(".json"));
            if (model == null) {
                break;
            }
            JsonObject textures = model.getAsJsonObject("textures");
            if (textures != null) {
                textures.entrySet().stream()
                        .filter(texture -> !inherited.has(texture.getKey()))
                        .forEach(texture -> inherited.add(texture.getKey(), texture.getValue()));
            }
            JsonElement parent = model.get("parent");
            current = parent == null ? null : ResourceLocation.parse(parent.getAsString());
        }

        JsonObject faces = new JsonObject();
        for (String face : FACES) {
            JsonElement texture = resolve(inherited, face);
            if (texture != null) {
                faces.add(face, texture);
            }
        }
        if (!faces.has("top")) {
            JsonElement fallback = resolve(inherited, "all");
            if (fallback == null) {
                fallback = faces.has("side") ? faces.get("side") : faces.get("bottom");
            }
            if (fallback == null) {
                return null;
            }
            faces.add("top", fallback);
        }
        if (!faces.has("side")) {
            faces.add("side", faces.get("top"));
        }
        if (!faces.has("bottom")) {
            faces.add("bottom", faces.get("top"));
        }
        faces.add("particle", faces.get("side"));
        return faces;
    }

    private static @Nullable JsonElement resolve(JsonObject textures, String key) {
        JsonElement value = textures.get(key);
        for (int depth = 0; value != null && value.getAsString().startsWith("#") && depth < MAX_TEXTURE_DEPTH; depth++) {
            value = textures.get(value.getAsString().substring(1));
        }
        return value == null || value.getAsString().startsWith("#") ? null : value;
    }

    private static @Nullable ResourceLocation slabModel(ResourceManager manager, ResourceLocation slabId) {
        JsonObject blockState = read(manager, slabId.withPrefix("blockstates/").withSuffix(".json"));
        if (blockState == null) {
            return null;
        }
        JsonObject variants = blockState.getAsJsonObject("variants");
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        JsonElement variant = variants.has("type=bottom") ? variants.get("type=bottom") : variants.entrySet().iterator().next().getValue();
        if (variant.isJsonArray()) {
            variant = variant.getAsJsonArray().get(0);
        }
        JsonElement model = variant.getAsJsonObject().get("model");
        return model == null ? null : ResourceLocation.parse(model.getAsString());
    }

    private static @Nullable JsonObject read(ResourceManager manager, ResourceLocation path) {
        if (CACHE.containsKey(path)) {
            return CACHE.get(path);
        }
        JsonObject json = null;
        Optional<Resource> resource = manager.getResource(path);
        if (resource.isPresent()) {
            try (BufferedReader reader = resource.get().openAsReader()) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception ignored) {
            }
        }
        CACHE.put(path, json);
        return json;
    }

    private static String title(ResourceLocation layerId) {
        String path = layerId.getPath();
        return Artisanal.titleCase(path.substring(path.indexOf('/') + 1));
    }
}
