package com.crispytwig.bbb.fabric;

import com.crispytwig.bbb.BuildingButBetter;
import net.fabricmc.api.ModInitializer;

public class BBBFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BuildingButBetter.bootstrap();
    }
}
