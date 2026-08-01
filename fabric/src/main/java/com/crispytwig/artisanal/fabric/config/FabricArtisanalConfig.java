package com.crispytwig.artisanal.fabric.config;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.services.IConfigHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FabricArtisanalConfig implements IConfigHelper {
    private static final boolean CHERRY_WOOD_SOUNDS = read();

    private static boolean read() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(Artisanal.MOD_ID + ".properties");
        Properties properties = new Properties();
        properties.setProperty("cherry_wood_sounds", "true");

        try {
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    properties.load(reader);
                }
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                properties.store(writer, null);
            }
        } catch (Exception ignored) {
        }
        return Boolean.parseBoolean(properties.getProperty("cherry_wood_sounds", "true"));
    }

    @Override
    public boolean cherryWoodSounds() {
        return CHERRY_WOOD_SOUNDS;
    }
}
