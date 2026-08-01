package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.AllayFlightHandler;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.network.AllayFlightPayload;
import com.crispytwig.artisanal.network.PanelPayload;
import com.crispytwig.artisanal.item.PanelItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
        Artisanal.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));
        Artisanal.registerFlammability((block, encouragement, flammability) ->
                FlammableBlockRegistry.getDefaultInstance().add(block, encouragement, flammability));
        Artisanal.registerFuels((item, burnTime) -> FuelRegistry.INSTANCE.add(item, burnTime));

        PayloadTypeRegistry.playS2C().register(AllayFlightPayload.TYPE, AllayFlightPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(PanelPayload.TYPE, PanelPayload.STREAM_CODEC);

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                ArchitectsScepterItem.tryCapture(player, entity, hand));
        UseBlockCallback.EVENT.register(PanelItem::tryRemove);
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                PanelItem.onBlockBroken(serverLevel, pos, !player.getAbilities().instabuild);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PanelItem.sync(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PanelItem.sync(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> PanelItem.sync(player));

        ServerTickEvents.END_SERVER_TICK.register(AllayFlightHandler::tick);
    }
}
