package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.ArtisanalClient;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ArtisanalNeoForgeClient {
    private ArtisanalNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(ArtisanalNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(ArtisanalNeoForgeClient::registerRenderers);
        modEventBus.addListener(ArtisanalNeoForgeClient::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ArtisanalClient.registerItemProperties(ItemProperties::register));
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ArtisanalClient.registerLayerDefinitions(event::registerLayerDefinition);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ArtisanalClient.registerRenderers(event::registerEntityRenderer);
    }
}
