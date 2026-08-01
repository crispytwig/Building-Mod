package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.ArtisanalClient;
import com.crispytwig.artisanal.client.AllayFlightClient;
import com.crispytwig.artisanal.network.AllayFlightPayload;
import com.crispytwig.artisanal.network.PanelPayload;
import com.crispytwig.artisanal.client.PanelClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class ArtisanalFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArtisanalClient.registerLayerDefinitions((location, definition) ->
                EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        ArtisanalClient.registerRenderers(EntityRendererRegistry::register);
        ArtisanalClient.registerItemProperties(FabricModelPredicateProviderRegistry::register);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerRenderer playerRenderer) {
                ArtisanalClient.registerPlayerLayers(playerRenderer, context.getModelSet(), registrationHelper::register);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(AllayFlightClient::tick);
        ClientPlayNetworking.registerGlobalReceiver(AllayFlightPayload.TYPE,
                (payload, context) -> AllayFlightClient.handleSync(payload));
        ClientPlayNetworking.registerGlobalReceiver(PanelPayload.TYPE,
                (payload, context) -> PanelClient.handle(payload));
    }
}
