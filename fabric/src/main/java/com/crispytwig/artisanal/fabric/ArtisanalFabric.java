package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.FacadeItem;
import com.crispytwig.artisanal.network.FacadePayload;
import com.crispytwig.artisanal.registry.ModLayers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.server.level.ServerLevel;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
        Artisanal.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));
        Artisanal.FlammabilityRegistrar flammability = (block, encouragement, burn) ->
                FlammableBlockRegistry.getDefaultInstance().add(block, burn, encouragement);
        Artisanal.registerFlammability(flammability);
        ModLayers.whenRegistered(() -> Artisanal.registerLayerFlammability(flammability));
        Artisanal.registerFuels((item, burnTime) -> FuelRegistry.INSTANCE.add(item, burnTime));
        Artisanal.registerFuelTags(FuelRegistry.INSTANCE::add);

        PayloadTypeRegistry.playS2C().register(FacadePayload.TYPE, FacadePayload.STREAM_CODEC);

        UseBlockCallback.EVENT.register(FacadeItem::tryRemove);
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                FacadeItem.onBlockBroken(serverLevel, pos, !player.getAbilities().instabuild);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> FacadeItem.sync(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> FacadeItem.sync(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> FacadeItem.sync(player));
    }
}
