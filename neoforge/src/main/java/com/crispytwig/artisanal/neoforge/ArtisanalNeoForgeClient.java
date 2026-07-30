package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.ArtisanalClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ArtisanalNeoForgeClient {
    private ArtisanalNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ArtisanalNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(ArtisanalNeoForgeClient::registerRenderers);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ArtisanalClient.registerLayerDefinitions(event::registerLayerDefinition);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ArtisanalClient.registerRenderers(event::registerEntityRenderer);
    }
}
