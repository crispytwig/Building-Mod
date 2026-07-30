package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.ArtisanalClient;
import com.crispytwig.artisanal.client.AllayFlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ArtisanalNeoForgeClient {
    private ArtisanalNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ArtisanalNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(ArtisanalNeoForgeClient::registerRenderers);
        modEventBus.addListener(ArtisanalNeoForgeClient::addLayers);
        modEventBus.addListener(ArtisanalNeoForgeClient::clientSetup);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForgeClient::onClientTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        AllayFlightClient.tick(Minecraft.getInstance());
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

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer playerRenderer) {
                ArtisanalClient.registerPlayerLayers(playerRenderer, event.getEntityModels(), playerRenderer::addLayer);
            }
        }
    }
}
