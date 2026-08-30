package com.crispytwig.bbb.fabric.config;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.config.BlockGroup;
import com.crispytwig.bbb.common.config.ConfigKeys;
import com.crispytwig.bbb.platform.services.IConfigHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

public final class FabricBuildingButBetterConfig implements IConfigHelper {
    private static final String BLOCK_GROUP_PREFIX = ConfigKeys.BLOCK_GROUPS + ".";

    private static volatile Snapshot snapshot = load();
    private static volatile long lastChecked = System.currentTimeMillis();

    private static final boolean CHERRY_WOOD_SOUNDS = snapshot.cherryWoodSounds();
    private static final boolean PRISMARINE_DEEPSLATE_SOUNDS = snapshot.prismarineDeepslateSounds();

    private record Snapshot(Set<BlockGroup> disabled, boolean cherryWoodSounds,
                            boolean prismarineDeepslateSounds, long timestamp) {
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(BuildingButBetter.MOD_ID + ".properties");
    }

    private static long timestamp() {
        try {
            Path path = path();
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : -1L;
        } catch (Exception ignored) {
            return -1L;
        }
    }

    private static Properties defaults() {
        Properties defaults = new Properties();
        defaults.setProperty(ConfigKeys.CHERRY_WOOD_SOUNDS, "true");
        defaults.setProperty(ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS, "true");
        for (BlockGroup group : BlockGroup.values()) {
            defaults.setProperty(BLOCK_GROUP_PREFIX + group.key(), "true");
        }
        return defaults;
    }

    private static Snapshot load() {
        Properties values = defaults();
        Path path = path();
        try {
            Properties onDisk = new Properties();
            boolean existed = Files.exists(path);
            if (existed) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    onDisk.load(reader);
                }
            }
            boolean complete = existed && onDisk.keySet().containsAll(values.keySet());
            values.putAll(onDisk);
            if (!complete) {
                try (Writer writer = Files.newBufferedWriter(path)) {
                    values.store(writer, "Disable blocks, removing them from Creative tabs, loot, and recipes."
                            + " " + ConfigKeys.CHERRY_WOOD_SOUNDS + " and " + ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS
                            + " are read during block registration and need a game restart.");
                }
            }
        } catch (Exception ignored) {
        }

        Set<BlockGroup> disabled = EnumSet.noneOf(BlockGroup.class);
        for (BlockGroup group : BlockGroup.values()) {
            if (!bool(values, BLOCK_GROUP_PREFIX + group.key(), true)) {
                disabled.add(group);
            }
        }
        return new Snapshot(Collections.unmodifiableSet(disabled),
                bool(values, ConfigKeys.CHERRY_WOOD_SOUNDS, true),
                bool(values, ConfigKeys.PRISMARINE_DEEPSLATE_SOUNDS, true),
                timestamp());
    }

    private static Snapshot current() {
        long now = System.currentTimeMillis();
        if (now - lastChecked < 1000L) {
            return snapshot;
        }
        lastChecked = now;
        Snapshot held = snapshot;
        if (timestamp() != held.timestamp()) {
            held = load();
            snapshot = held;
        }
        return held;
    }

    private static boolean bool(Properties source, String key, boolean fallback) {
        String value = source.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
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
        return current().disabled();
    }

    @Override
    public boolean needsRestart() {
        Snapshot now = current();
        return now.cherryWoodSounds() != CHERRY_WOOD_SOUNDS
                || now.prismarineDeepslateSounds() != PRISMARINE_DEEPSLATE_SOUNDS;
    }
}
