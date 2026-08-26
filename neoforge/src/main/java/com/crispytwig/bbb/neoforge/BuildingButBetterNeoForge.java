package com.crispytwig.bbb.neoforge;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.block.TimberFrameBlock;
import com.crispytwig.bbb.client.FacadeClient;
import com.crispytwig.bbb.item.FacadeItem;
import com.crispytwig.bbb.network.FacadePayload;
import com.crispytwig.bbb.neoforge.config.NeoForgeBuildingButBetterConfig;
import com.crispytwig.bbb.neoforge.platform.NeoForgeRegistrationProvider;
import com.crispytwig.bbb.registry.ModLayers;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(BuildingButBetter.MOD_ID)
public class BuildingButBetterNeoForge {
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
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> syncFacades(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(BuildingButBetterNeoForge::onFurnaceFuelBurnTime);

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
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = FacadeItem.tryRemove(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
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

    private static void syncFacades(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            FacadeItem.sync(serverPlayer);
        }
    }
}
