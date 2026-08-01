package com.crispytwig.artisanal.neoforge.config;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.services.IConfigHelper;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;

public final class NeoForgeArtisanalConfig implements IConfigHelper {
    public static final String FILE_NAME = Artisanal.MOD_ID + "-common.toml";
    public static final ModConfigSpec SPEC;
    private static final String CHERRY_WOOD_SOUNDS_KEY = "cherry_wood_sounds";
    private static final boolean CHERRY_WOOD_SOUNDS = read();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("artisanal.configuration." + CHERRY_WOOD_SOUNDS_KEY)
                .define(CHERRY_WOOD_SOUNDS_KEY, true);
        SPEC = builder.build();
    }

    private static boolean read() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            return true;
        }
        try (FileConfig file = FileConfig.of(path)) {
            file.load();
            return file.getOrElse(CHERRY_WOOD_SOUNDS_KEY, true);
        } catch (Exception ignored) {
            return true;
        }
    }

    @Override
    public boolean cherryWoodSounds() {
        return CHERRY_WOOD_SOUNDS;
    }
}
