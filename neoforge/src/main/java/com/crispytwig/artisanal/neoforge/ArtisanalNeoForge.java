package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.client.AllayFlightClient;
import com.crispytwig.artisanal.item.AllayFlightHandler;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.network.AllayFlightPayload;
import com.crispytwig.artisanal.network.PanelPayload;
import com.crispytwig.artisanal.neoforge.platform.NeoForgeRegistrationProvider;
import com.crispytwig.artisanal.client.PanelClient;
import com.crispytwig.artisanal.item.PanelItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
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
        modEventBus.addListener(ArtisanalNeoForge::commonSetup);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onBlockBreak);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> syncPanels(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncPanels(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> syncPanels(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onServerTick);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onFurnaceFuelBurnTime);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ArtisanalNeoForgeClient.init(modEventBus);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> Artisanal.registerFlammability(((FireBlock) Blocks.FIRE)::setFlammable));
    }

    private static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        Artisanal.registerFuels((item, burnTime) -> {
            if (event.getItemStack().is(item.asItem())) {
                event.setBurnTime(burnTime);
            }
        });
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        Artisanal.createAttributes((type, builder) -> event.put(type, builder.build()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(AllayFlightPayload.TYPE, AllayFlightPayload.STREAM_CODEC,
                (payload, context) -> AllayFlightClient.handleSync(payload));
        registrar.playToClient(PanelPayload.TYPE, PanelPayload.STREAM_CODEC,
                (payload, context) -> PanelClient.handle(payload));
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        AllayFlightHandler.tick(event.getServer());
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = PanelItem.tryRemove(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PanelItem.onBlockBroken(level, event.getPos(), !event.getPlayer().getAbilities().instabuild);
        }
    }

    private static void syncPanels(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PanelItem.sync(serverPlayer);
        }
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        InteractionResult result = ArchitectsScepterItem.tryCapture(event.getEntity(), event.getTarget(), event.getHand());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }
}
