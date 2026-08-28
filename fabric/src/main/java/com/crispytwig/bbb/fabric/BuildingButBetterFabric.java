package com.crispytwig.bbb.fabric;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.TimberFrameBlock;
import com.crispytwig.bbb.common.item.FacadeItem;
import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.network.FacadePayload;
import com.crispytwig.bbb.common.network.PaintSelectionPayload;
import com.crispytwig.bbb.common.registry.ModLayers;
import com.crispytwig.bbb.common.paint.PaintJobs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
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
        PayloadTypeRegistry.playC2S().register(PaintSelectionPayload.TYPE, PaintSelectionPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PaintSelectionPayload.TYPE,
                (payload, context) -> PaintJobs.start(context.player(), payload.from(), payload.to()));
        ServerTickEvents.END_SERVER_TICK.register(server -> PaintJobs.tick());
        ServerWorldEvents.UNLOAD.register((server, world) -> PaintJobs.clear(world));

        UseBlockCallback.EVENT.register(PaintBrushItem::tryPaint);
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
