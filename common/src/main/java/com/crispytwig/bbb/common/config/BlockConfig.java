package com.crispytwig.bbb.common.config;

import com.crispytwig.bbb.common.block.LayerBlock;
import com.crispytwig.bbb.common.registry.ModBlocks;
import com.crispytwig.bbb.platform.Services;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlockConfig {
    private static volatile Set<BlockGroup> serverDisabled;

    private BlockConfig() {
    }

    public static Set<BlockGroup> disabledGroups() {
        Set<BlockGroup> fromServer = serverDisabled;
        return fromServer != null ? fromServer : Services.CONFIG.disabledGroups();
    }

    public static Set<BlockGroup> disabledInConfig() {
        return Services.CONFIG.disabledGroups();
    }

    public static boolean isEnabled(BlockGroup group) {
        return !disabledGroups().contains(group);
    }

    public static boolean isEnabled(Block block) {
        Set<BlockGroup> disabled = disabledGroups();
        if (disabled.isEmpty()) {
            return true;
        }
        Set<BlockGroup> required = groupsFor(block);
        if (required == null) {
            return true;
        }
        for (BlockGroup group : required) {
            if (disabled.contains(group)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEnabled(Item item) {
        return !(item instanceof BlockItem blockItem) || isEnabled(blockItem.getBlock());
    }

    @Nullable
    private static Set<BlockGroup> groupsFor(Block block) {
        if (block instanceof LayerBlock) {
            return Groups.LAYERS;
        }
        return Groups.MAP.get(block);
    }

    public static List<String> disabledKeys() {
        Set<BlockGroup> disabled = disabledGroups();
        List<String> keys = new ArrayList<>(disabled.size());
        for (BlockGroup group : BlockGroup.values()) {
            if (disabled.contains(group)) {
                keys.add(group.key());
            }
        }
        return keys;
    }

    public static void setFromServer(List<String> disabledKeys) {
        Set<BlockGroup> disabled = EnumSet.noneOf(BlockGroup.class);
        for (BlockGroup group : BlockGroup.values()) {
            if (disabledKeys.contains(group.key())) {
                disabled.add(group);
            }
        }
        serverDisabled = disabled;
    }

    public static void clearFromServer() {
        serverDisabled = null;
    }

    private static final class Groups {
        private static final Set<BlockGroup> LAYERS = Set.of(BlockGroup.LAYERS);
        private static final Map<Block, Set<BlockGroup>> MAP = build();

        private static Map<Block, Set<BlockGroup>> build() {
            Map<Block, Set<BlockGroup>> map = new IdentityHashMap<>();

            for (ModBlocks.WoodSet wood : ModBlocks.WOOD) {
                addWood(map, wood, null);
            }
            for (ModBlocks.ColoredWoodSet set : ModBlocks.COLORED_WOOD) {
                addWood(map, set, BlockGroup.COLORED_WOOD);
                add(map, BlockGroup.COLORED_WOOD, null, set.plankBlock(), set.plankStairs(), set.plankSlab());
            }

            addSet(map, BlockGroup.STONE_TILES, ModBlocks.STONE_TILES);
            add(map, BlockGroup.STONE_TILES, null, ModBlocks.STONE_PILLAR, ModBlocks.STONE_PILLAR_STAIRS, ModBlocks.STONE_PILLAR_SLAB);
            add(map, BlockGroup.PRISMARINE_TILES, null, ModBlocks.PRISMARINE_TILES, ModBlocks.PRISMARINE_TILE_STAIRS, ModBlocks.PRISMARINE_TILE_SLAB);

            for (ModBlocks.ColoredSet colored : ModBlocks.TERRACOTTA) {
                colored.sets().forEach(set -> addSet(map, BlockGroup.TERRACOTTA, set));
            }
            for (ModBlocks.ColoredSet colored : ModBlocks.PLASTER) {
                colored.sets().forEach(set -> addSet(map, BlockGroup.PLASTER, set));
            }

            ModBlocks.SOFAS.values().forEach(holder -> add(map, BlockGroup.SOFAS, null, holder));
            ModBlocks.CURTAINS.values().forEach(holder -> add(map, BlockGroup.CURTAINS, null, holder));

            return Map.copyOf(map);
        }

        private static void addWood(Map<Block, Set<BlockGroup>> map, ModBlocks.WoodVariant wood, @Nullable BlockGroup gate) {
            add(map, BlockGroup.BOARDS, gate, wood.boards(), wood.boardStairs(), wood.boardSlab());
            add(map, BlockGroup.POLISHED, gate, wood.polished(), wood.polishedSlab());
            add(map, BlockGroup.TRIM, gate, wood.trim(), wood.trimStairs());
            add(map, BlockGroup.PILLARS, gate, wood.pillar(), wood.pillarStairs(), wood.pillarSlab());
            add(map, BlockGroup.BEAMS, gate, wood.beam());
            add(map, BlockGroup.TABLES, gate, wood.table());
            add(map, BlockGroup.CHAIRS, gate, wood.chair());
            add(map, BlockGroup.FRAMES, gate, wood.frame());
            add(map, BlockGroup.TIMBER_FRAMES, gate, wood.timberFrame());
            add(map, BlockGroup.SHUTTERS, gate, wood.shutter());
            add(map, BlockGroup.LANTERNS, gate, wood.lantern());
            addAll(map, BlockGroup.WINDOWS, gate, wood.windows());
        }

        private static void addSet(Map<Block, Set<BlockGroup>> map, BlockGroup group, ModBlocks.BlockSet set) {
            add(map, group, null, set.block(), set.stairs(), set.slab());
        }

        @SafeVarargs
        private static void add(Map<Block, Set<BlockGroup>> map, BlockGroup group, @Nullable BlockGroup gate,
                                DeferredHolder<Block, ? extends Block>... holders) {
            addAll(map, group, gate, List.of(holders));
        }

        private static void addAll(Map<Block, Set<BlockGroup>> map, BlockGroup group, @Nullable BlockGroup gate,
                                   List<? extends DeferredHolder<Block, ? extends Block>> holders) {
            Set<BlockGroup> required = gate == null ? Set.of(group) : Set.of(group, gate);
            for (DeferredHolder<Block, ? extends Block> holder : holders) {
                map.put(holder.get(), required);
            }
        }
    }
}
