package com.crispytwig.artisanal;

import com.crispytwig.artisanal.client.model.WrightModel;
import com.crispytwig.artisanal.client.renderer.WrightRenderer;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Supplier;

public final class ArtisanalClient {
    private ArtisanalClient() {
    }

    @FunctionalInterface
    public interface LayerRegistrar {
        void register(ModelLayerLocation location, Supplier<LayerDefinition> definition);
    }

    @FunctionalInterface
    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public static void registerLayerDefinitions(LayerRegistrar registrar) {
        registrar.register(WrightModel.LAYER_LOCATION, WrightModel::createBodyLayer);
    }

    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(ModEntityTypes.WRIGHT.get(), WrightRenderer::new);
    }
}
