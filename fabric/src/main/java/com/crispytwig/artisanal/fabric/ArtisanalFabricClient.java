package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.ArtisanalClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;

public class ArtisanalFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArtisanalClient.registerLayerDefinitions((location, definition) ->
                EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        ArtisanalClient.registerRenderers(EntityRendererRegistry::register);
        ArtisanalClient.registerItemProperties(FabricModelPredicateProviderRegistry::register);
    }
}
