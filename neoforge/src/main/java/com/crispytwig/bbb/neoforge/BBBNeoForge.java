package com.crispytwig.bbb.neoforge;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.neoforge.platform.NeoForgeRegistrationProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BuildingButBetter.MOD_ID)
public class BBBNeoForge {
    public BBBNeoForge(IEventBus modEventBus) {
        NeoForgeRegistrationProvider.EVENT_BUS = modEventBus;

        BuildingButBetter.bootstrap();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            BBBNeoForgeClient.init(modEventBus);
        }
    }
}
