package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.flight.AllayFlightHandler;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.network.AllayFlightPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
        Artisanal.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));

        PayloadTypeRegistry.playS2C().register(AllayFlightPayload.TYPE, AllayFlightPayload.STREAM_CODEC);

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                ArchitectsScepterItem.tryCapture(player, entity, hand));
        ServerTickEvents.END_SERVER_TICK.register(AllayFlightHandler::tick);
    }
}
