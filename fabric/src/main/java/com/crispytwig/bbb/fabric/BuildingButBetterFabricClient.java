package com.crispytwig.bbb.fabric;

import com.crispytwig.bbb.BuildingButBetterClient;
import com.crispytwig.bbb.client.FacadeClient;
import com.crispytwig.bbb.network.FacadePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class BuildingButBetterFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BuildingButBetterClient.registerLayerDefinitions((location, definition) ->
                EntityModelLayerRegistry.registerModelLayer(location, definition::get));
        BuildingButBetterClient.registerRenderers(EntityRendererRegistry::register);
        BuildingButBetterClient.registerItemProperties(FabricModelPredicateProviderRegistry::register);
        BuildingButBetterClient.registerRenderTypes((type, block) -> BlockRenderLayerMap.INSTANCE.putBlock(block, type));
        BuildingButBetterClient.registerBlockEntityRenderers(BlockEntityRenderers::register);
        BuildingButBetterClient.registerScreens(new BuildingButBetterClient.ScreenRegistrar() {
            @Override
            public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, BuildingButBetterClient.ScreenFactory<M, S> factory) {
                MenuScreens.register(type, factory::create);
            }
        });
        ModelLoadingPlugin.register(context -> BuildingButBetterClient.registerExtraModels(context::addModels));

        ClientPlayNetworking.registerGlobalReceiver(FacadePayload.TYPE, (payload, context) -> FacadeClient.handle(payload));
    }
}
