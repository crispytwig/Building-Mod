package com.crispytwig.bbb.neoforge.config;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.services.IConfigHelper;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;

public final class NeoForgeBuildingButBetterConfig implements IConfigHelper {
    public static final String FILE_NAME = BuildingButBetter.MOD_ID + "-common.toml";
    public static final ModConfigSpec SPEC;
    private static final String CHERRY_WOOD_SOUNDS_KEY = "cherry_wood_sounds";
    private static final String PRISMARINE_DEEPSLATE_SOUNDS_KEY = "prismarine_deepslate_sounds";
    private static final boolean CHERRY_WOOD_SOUNDS = read(CHERRY_WOOD_SOUNDS_KEY);
    private static final boolean PRISMARINE_DEEPSLATE_SOUNDS = read(PRISMARINE_DEEPSLATE_SOUNDS_KEY);

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("bbb.configuration." + CHERRY_WOOD_SOUNDS_KEY)
                .define(CHERRY_WOOD_SOUNDS_KEY, true);
        builder.translation("bbb.configuration." + PRISMARINE_DEEPSLATE_SOUNDS_KEY)
                .define(PRISMARINE_DEEPSLATE_SOUNDS_KEY, true);
        SPEC = builder.build();
    }

    private static boolean read(String key) {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            return true;
        }
        try (FileConfig file = FileConfig.of(path)) {
            file.load();
            return file.getOrElse(key, true);
        } catch (Exception ignored) {
            return true;
        }
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
