package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
        Artisanal.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));
        Artisanal.registerFlammability((block, encouragement, flammability) ->
                FlammableBlockRegistry.getDefaultInstance().add(block, encouragement, flammability));
        Artisanal.registerFuels((item, burnTime) -> FuelRegistry.INSTANCE.add(item, burnTime));
    }
}
