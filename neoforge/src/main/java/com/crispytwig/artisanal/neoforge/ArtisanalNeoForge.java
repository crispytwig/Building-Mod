package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.client.FacadeClient;
import com.crispytwig.artisanal.item.FacadeItem;
import com.crispytwig.artisanal.network.FacadePayload;
import com.crispytwig.artisanal.neoforge.config.NeoForgeArtisanalConfig;
import com.crispytwig.artisanal.neoforge.platform.NeoForgeRegistrationProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Artisanal.MOD_ID)
public class ArtisanalNeoForge {
    public ArtisanalNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeRegistrationProvider.EVENT_BUS = modEventBus;

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeArtisanalConfig.SPEC, NeoForgeArtisanalConfig.FILE_NAME);

        Artisanal.bootstrap();

        modEventBus.addListener(ArtisanalNeoForge::createAttributes);
        modEventBus.addListener(ArtisanalNeoForge::registerPayloads);
        modEventBus.addListener(ArtisanalNeoForge::commonSetup);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onBlockBreak);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(ArtisanalNeoForge::onFurnaceFuelBurnTime);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ArtisanalNeoForgeClient.init(modEventBus, modContainer);
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
        registrar.playToClient(FacadePayload.TYPE, FacadePayload.STREAM_CODEC,
                (payload, context) -> FacadeClient.handle(payload));
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = FacadeItem.tryRemove(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            FacadeItem.onBlockBroken(level, event.getPos(), !event.getPlayer().getAbilities().instabuild);
        }
    }

    private static void syncFacades(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            FacadeItem.sync(serverPlayer);
        }
    }
}
