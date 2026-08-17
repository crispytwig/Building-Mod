package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.ArtisanalClient;
import com.crispytwig.artisanal.client.FacadeClient;
import com.crispytwig.artisanal.network.FacadePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ArtisanalFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArtisanalClient.registerLayerDefinitions((location, definition) ->
                EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        ArtisanalClient.registerRenderers(EntityRendererRegistry::register);
        ArtisanalClient.registerItemProperties(FabricModelPredicateProviderRegistry::register);
        ArtisanalClient.registerRenderTypes((type, block) -> BlockRenderLayerMap.INSTANCE.putBlock(block, type));
        ArtisanalClient.registerBlockEntityRenderers(BlockEntityRenderers::register);
        ModelLoadingPlugin.register(context -> ArtisanalClient.registerExtraModels(context::addModels));

        ClientPlayNetworking.registerGlobalReceiver(FacadePayload.TYPE, (payload, context) -> FacadeClient.handle(payload));
    }
}
