package com.crispytwig.artisanal.neoforge;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.neoforge.config.NeoForgeArtisanalConfig;
import com.crispytwig.artisanal.neoforge.platform.NeoForgeRegistrationProvider;
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

@Mod(Artisanal.MOD_ID)
public class ArtisanalNeoForge {
    public ArtisanalNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeRegistrationProvider.EVENT_BUS = modEventBus;

        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeArtisanalConfig.SPEC, NeoForgeArtisanalConfig.FILE_NAME);

        Artisanal.bootstrap();

        modEventBus.addListener(ArtisanalNeoForge::createAttributes);
        modEventBus.addListener(ArtisanalNeoForge::commonSetup);
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
}
