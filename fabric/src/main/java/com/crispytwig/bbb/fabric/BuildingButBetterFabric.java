package com.crispytwig.bbb.fabric;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.block.TimberFrameBlock;
import com.crispytwig.bbb.item.FacadeItem;
import com.crispytwig.bbb.network.FacadePayload;
import com.crispytwig.bbb.registry.ModLayers;
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

public class BuildingButBetterFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BuildingButBetter.bootstrap();
        BuildingButBetter.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));
        BuildingButBetter.FlammabilityRegistrar flammability = (block, encouragement, burn) ->
                FlammableBlockRegistry.getDefaultInstance().add(block, burn, encouragement);
        BuildingButBetter.registerFlammability(flammability);
        ModLayers.whenRegistered(() -> BuildingButBetter.registerLayerFlammability(flammability));
        BuildingButBetter.registerFuels((item, burnTime) -> FuelRegistry.INSTANCE.add(item, burnTime));
        BuildingButBetter.registerFuelTags(FuelRegistry.INSTANCE::add);

        PayloadTypeRegistry.playS2C().register(FacadePayload.TYPE, FacadePayload.STREAM_CODEC);

        UseBlockCallback.EVENT.register(FacadeItem::tryRemove);
        UseBlockCallback.EVENT.register(TimberFrameBlock::tryCycleCross);
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
