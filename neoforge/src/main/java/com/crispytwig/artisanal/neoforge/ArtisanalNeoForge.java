package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.client.AllayFlightClient;
import com.crispytwig.artisanal.flight.AllayFlightHandler;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.network.AllayFlightPayload;
import com.crispytwig.artisanal.neoforge.platform.NeoForgeRegistrationProvider;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Artisanal.MOD_ID)
public class ArtisanalNeoForge {
    public ArtisanalNeoForge(IEventBus modEventBus) {
        NeoForgeRegistrationProvider.EVENT_BUS = modEventBus;

        Artisanal.bootstrap();

        modEventBus.addListener(ArtisanalNeoForge::createAttributes);
        modEventBus.addListener(ArtisanalNeoForge::registerPayloads);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onServerTick);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ArtisanalNeoForgeClient.init(modEventBus);
        }
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        Artisanal.createAttributes((type, builder) -> event.put(type, builder.build()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AllayFlightPayload.TYPE, AllayFlightPayload.STREAM_CODEC,
                (payload, context) -> AllayFlightClient.handleSync(payload));
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        AllayFlightHandler.tick(event.getServer());
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ArchitectsScepterItem.tryCapture(event.getEntity(), event.getTarget(), event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
