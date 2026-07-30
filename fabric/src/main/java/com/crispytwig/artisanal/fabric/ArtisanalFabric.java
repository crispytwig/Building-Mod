package com.crispytwig.artisanal.fabric;

import com.crispytwig.artisanal.Artisanal;
import net.fabricmc.api.ModInitializer;

public class ArtisanalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Artisanal.bootstrap();
    }
}
