package com.crispytwig.bbb.neoforge;

import com.crispytwig.bbb.BuildingButBetterClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class BuildingButBetterNeoForgeClient {
    private BuildingButBetterNeoForgeClient() {
    }

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(BuildingButBetterNeoForgeClient::registerLayerDefinitions);
        modEventBus.addListener(BuildingButBetterNeoForgeClient::registerRenderers);
        modEventBus.addListener(BuildingButBetterNeoForgeClient::registerAdditionalModels);
        modEventBus.addListener(BuildingButBetterNeoForgeClient::registerScreens);
        modEventBus.addListener(BuildingButBetterNeoForgeClient::clientSetup);
    }

    private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        BuildingButBetterClient.registerExtraModels(location -> event.register(new ModelResourceLocation(location, "standalone")));
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        BuildingButBetterClient.registerScreens(new BuildingButBetterClient.ScreenRegistrar() {
            @Override
            public <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, BuildingButBetterClient.ScreenFactory<M, S> factory) {
                event.register(type, factory::create);
            }
        });
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BuildingButBetterClient.registerItemProperties(ItemProperties::register));
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        BuildingButBetterClient.registerLayerDefinitions(event::registerLayerDefinition);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BuildingButBetterClient.registerRenderers(event::registerEntityRenderer);
        BuildingButBetterClient.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
    }
}
