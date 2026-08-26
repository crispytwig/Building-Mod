package com.crispytwig.bbb.data;

import com.crispytwig.bbb.BuildingButBetter;
import com.google.gson.JsonElement;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
public final class BuildingButBetterPack extends AbstractPackResources {
    public static final BuildingButBetterPack INSTANCE = new BuildingButBetterPack();

    private static final Component DESCRIPTION = Component.literal("Building But Better Runtime Resources");

    private final Map<PackType, Map<String, Map<ResourceLocation, byte[]>>> resources = Map.of(
            PackType.CLIENT_RESOURCES, new ConcurrentHashMap<>(),
            PackType.SERVER_DATA, new ConcurrentHashMap<>());

    private BuildingButBetterPack() {
        super(new PackLocationInfo(BuildingButBetter.MOD_ID + "_runtime", DESCRIPTION, PackSource.BUILT_IN, Optional.empty()));
    }

    public void add(PackType type, ResourceLocation path, JsonElement json) {
        resources.get(type)
                .computeIfAbsent(path.getNamespace(), ignored -> new ConcurrentHashMap<>())
                .put(path, json.toString().getBytes(StandardCharsets.UTF_8));
    }

    public void clear(PackType type) {
        resources.get(type).clear();
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        Map<ResourceLocation, byte[]> namespace = resources.get(packType).get(location.getNamespace());
        byte[] bytes = namespace == null ? null : namespace.get(location);
        return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {
        Map<ResourceLocation, byte[]> entries = resources.get(packType).get(namespace);
        if (entries == null) {
            return;
        }
        entries.forEach((location, bytes) -> {
            if (location.getPath().startsWith(path)) {
                output.accept(location, () -> new ByteArrayInputStream(bytes));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Set.copyOf(resources.get(type).keySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer != PackMetadataSection.TYPE) {
            return null;
        }
        return (T) new PackMetadataSection(DESCRIPTION, SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES), Optional.empty());
    }

    @Override
    public void close() {
    }
}
