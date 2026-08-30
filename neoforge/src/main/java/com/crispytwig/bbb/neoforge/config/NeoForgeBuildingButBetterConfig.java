package com.crispytwig.bbb.neoforge.config;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.config.BlockGroup;
import com.crispytwig.bbb.common.config.ConfigKeys;
import com.crispytwig.bbb.platform.services.IConfigHelper;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class NeoForgeBuildingButBetterConfig implements IConfigHelper {
    public static final String FILE_NAME = BuildingButBetter.MOD_ID + "-common.toml";
    public static final ModConfigSpec SPEC;

    private static final boolean[] STARTUP_SOUNDS = readSounds();
    private static final boolean CHERRY_WOOD_SOUNDS = STARTUP_SOUNDS[0];
    private static final boolean PRISMARINE_DEEPSLATE_SOUNDS = STARTUP_SOUNDS[1];

    private static final Map<BlockGroup, ModConfigSpec.BooleanValue> BLOCK_GROUP_VALUES = new EnumMap<>(BlockGroup.class);
    private static final ModConfigSpec.BooleanValue CHERRY_WOOD_SOUNDS_LIVE;
    private static final ModConfigSpec.BooleanValue PRISMARINE_DEEPSLATE_SOUNDS_LIVE;

    private static volatile Set<BlockGroup> disabledCache;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CHERRY_WOOD_SOUNDS_LIVE = builder
                .comment("Makes every wood set use the Cherry wood set's sounds.",
                        "Read while blocks are being registered: requires a game restart to take effect.")
                .translation("bbb.configuration." + ConfigKeys.CHERRY_WOOD_SOUNDS)
                .define(ConfigKeys.CHERRY_WOOD_SOUNDS, true);
        PRISMARINE_DEEPSLATE_SOUNDS_LIVE = builder
                .comment("Makes every Prismarine block use Deepslate sounds.",
                        "Read while blocks are being registered: requires a game restart to take effect.")
                .translation("bbb.configuration." + ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS)
                .define(ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS, true);

        builder.comment("Disable blocks, removing them from Creative tabs, loot, and recipes.")
                .translation("bbb.configuration." + ConfigKeys.BLOCK_GROUPS)
                .push(ConfigKeys.BLOCK_GROUPS);
        for (BlockGroup group : BlockGroup.values()) {
            BLOCK_GROUP_VALUES.put(group, builder
                    .translation("bbb.configuration." + ConfigKeys.BLOCK_GROUPS + "." + group.key())
                    .define(group.key(), true));
        }
        builder.pop();

        SPEC = builder.build();
    }

    public static void clearCache() {
        disabledCache = null;
    }

    private static boolean[] readSounds() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            return new boolean[]{true, true};
        }
        try (FileConfig file = FileConfig.of(path)) {
            file.load();
            return new boolean[]{
                    file.getOrElse(ConfigKeys.CHERRY_WOOD_SOUNDS, true),
                    file.getOrElse(ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS, true)};
        } catch (Exception ignored) {
            return new boolean[]{true, true};
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

    @Override
    public Set<BlockGroup> disabledGroups() {
        Set<BlockGroup> cached = disabledCache;
        if (cached != null) {
            return cached;
        }
        if (!SPEC.isLoaded()) {
            return Set.of();
        }
        Set<BlockGroup> disabled = EnumSet.noneOf(BlockGroup.class);
        BLOCK_GROUP_VALUES.forEach((group, value) -> {
            if (!value.get()) {
                disabled.add(group);
            }
        });
        cached = Collections.unmodifiableSet(disabled);
        disabledCache = cached;
        return cached;
    }

    @Override
    public boolean needsRestart() {
        if (!SPEC.isLoaded()) {
            return false;
        }
        return CHERRY_WOOD_SOUNDS_LIVE.get() != CHERRY_WOOD_SOUNDS
                || PRISMARINE_DEEPSLATE_SOUNDS_LIVE.get() != PRISMARINE_DEEPSLATE_SOUNDS;
    }
}
