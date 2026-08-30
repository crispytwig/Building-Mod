package com.crispytwig.bbb.neoforge;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.TimberFrameBlock;
import com.crispytwig.bbb.client.BlockConfigClient;
import com.crispytwig.bbb.client.FacadeClient;
import com.crispytwig.bbb.common.item.FacadeItem;
import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.network.BlockConfigPayload;
import com.crispytwig.bbb.common.network.FacadePayload;
import com.crispytwig.bbb.common.network.PaintSelectionPayload;
import com.crispytwig.bbb.neoforge.config.NeoForgeBuildingButBetterConfig;
import com.crispytwig.bbb.neoforge.platform.NeoForgeRegistrationProvider;
import com.crispytwig.bbb.common.registry.ModLayers;
import com.crispytwig.bbb.common.paint.PaintJobs;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.concurrent.atomic.AtomicLong;

@Mod(BuildingButBetter.MOD_ID)
public class BuildingButBetterNeoForge {
    private static final AtomicLong CONFIG_CHANGED_AT = new AtomicLong(-1L);

    public BuildingButBetterNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeRegistrationProvider.EVENT_BUS = modEventBus;

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeBuildingButBetterConfig.SPEC, NeoForgeBuildingButBetterConfig.FILE_NAME);

        BuildingButBetter.bootstrap();

        modEventBus.addListener(BuildingButBetterNeoForge::createAttributes);
        modEventBus.addListener(BuildingButBetterNeoForge::registerPayloads);
        modEventBus.addListener(BuildingButBetterNeoForge::commonSetup);
        modEventBus.addListener(BuildingButBetterNeoForge::registerLayers);
        NeoForge.EVENT_BUS.addListener(BuildingButBetterNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(BuildingButBetterNeoForge::onBlockBreak);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> syncFacades(event.getEntity()));

        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            BlockConfigPayload payload = BlockConfigPayload.current();
            if (event.getPlayer() != null) {
                payload.send(event.getPlayer());
            } else {
                event.getPlayerList().getPlayers().forEach(payload::send);
            }
        });

        modEventBus.addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == NeoForgeBuildingButBetterConfig.SPEC) {
                NeoForgeBuildingButBetterConfig.clearCache();
                CONFIG_CHANGED_AT.set(System.currentTimeMillis());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(BuildingButBetterNeoForge::onFurnaceFuelBurnTime);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            PaintJobs.tick();
            syncIfChanged();
        });
        NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof ServerLevel level) {
                PaintJobs.clear(level);
            }
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BuildingButBetterNeoForgeClient.init(modEventBus, modContainer);
        }
    }

    private static void registerLayers(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.POTION)) {
            ModLayers.register();
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BuildingButBetter.registerFlammability(((FireBlock) Blocks.FIRE)::setFlammable);
            BuildingButBetter.registerLayerFlammability(((FireBlock) Blocks.FIRE)::setFlammable);
        });
    }

    private static void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        BuildingButBetter.registerFuelTags((tag, burnTime) -> {
            if (event.getItemStack().is(tag)) {
                event.setBurnTime(burnTime);
            }
        });
        BuildingButBetter.registerFuels((item, burnTime) -> {
            if (event.getItemStack().is(item.asItem())) {
                event.setBurnTime(burnTime);
            }
        });
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        BuildingButBetter.createAttributes((type, builder) -> event.put(type, builder.build()));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(FacadePayload.TYPE, FacadePayload.STREAM_CODEC,
                (payload, context) -> FacadeClient.handle(payload));
        registrar.playToClient(BlockConfigPayload.TYPE, BlockConfigPayload.STREAM_CODEC,
                (payload, context) -> BlockConfigClient.handle(payload));
        registrar.playToServer(PaintSelectionPayload.TYPE, PaintSelectionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        PaintJobs.start(player, payload.from(), payload.to());
                    }
                }));
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = PaintBrushItem.tryPaint(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result == InteractionResult.PASS) {
            result = FacadeItem.tryRemove(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        }
        if (result == InteractionResult.PASS) {
            result = TimberFrameBlock.tryCycleCross(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        }
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

    private static void syncIfChanged() {
        long changedAt = CONFIG_CHANGED_AT.get();
        if (changedAt < 0 || System.currentTimeMillis() - changedAt < 200L) {
            return;
        }

        if (!CONFIG_CHANGED_AT.compareAndSet(changedAt, -1L)) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            BlockConfigPayload payload = BlockConfigPayload.current();
            server.getPlayerList().getPlayers().forEach(payload::send);
        }
    }

    private static void syncFacades(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            FacadeItem.sync(serverPlayer);
        }
    }
}
