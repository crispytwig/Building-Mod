package com.crispytwig.bbb.fabric.config;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.services.IConfigHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FabricBuildingButBetterConfig implements IConfigHelper {
    private static final Properties PROPERTIES = read();
    private static final boolean CHERRY_WOOD_SOUNDS = Boolean.parseBoolean(PROPERTIES.getProperty("cherry_wood_sounds", "true"));
    private static final boolean PRISMARINE_DEEPSLATE_SOUNDS = Boolean.parseBoolean(PROPERTIES.getProperty("prismarine_deepslate_sounds", "true"));

    private static Properties read() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(BuildingButBetter.MOD_ID + ".properties");
        Properties properties = new Properties();
        properties.setProperty("cherry_wood_sounds", "true");
        properties.setProperty("prismarine_deepslate_sounds", "true");

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
        return properties;
    }

    @Override
    public boolean cherryWoodSounds() {
        return CHERRY_WOOD_SOUNDS;
    }

    @Override
    public boolean prismarineDeepslateSounds() {
        return PRISMARINE_DEEPSLATE_SOUNDS;
    }
}
