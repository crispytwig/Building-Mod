package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
        Artisanal.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                ArchitectsScepterItem.tryCapture(player, entity, hand));
    }
}
