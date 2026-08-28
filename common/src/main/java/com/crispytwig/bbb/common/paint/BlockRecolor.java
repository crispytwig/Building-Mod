package com.crispytwig.bbb.common.paint;

import com.crispytwig.bbb.common.BuildingButBetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockRecolor {
    private static final DyeColor[] COLORS = DyeColor.values();

    private static final String[] WOODS = {
            "dark_oak", "mangrove", "crimson", "acacia", "bamboo", "spruce", "cherry",
            "jungle", "warped", "birch", "oak"
    };

    private static final Map<Block, Map<DyeColor, Optional<Block>>> RECOLOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Block, String> BASE_NAME_CACHE = new ConcurrentHashMap<>();

    private BlockRecolor() {
    }

    public static Optional<BlockState> paint(BlockState state, DyeColor color) {
        return find(state.getBlock(), color).map(block -> copyStates(state, block.defaultBlockState()));
    }

    public static boolean sameBlock(BlockState a, BlockState b) {
        if (a.is(b.getBlock())) {
            return false;
        }
        return !plainName(a.getBlock()).equals(plainName(b.getBlock()));
    }

    private static Optional<Block> find(Block block, DyeColor color) {
        return RECOLOR_CACHE
                .computeIfAbsent(block, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(color, c -> search(block, c));
    }

    private static Optional<Block> search(Block block, DyeColor color) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        String namespace = id.getNamespace();
        String target = color.getName();

        for (DyeColor dye : COLORS) {
            String token = dye.getName();
            int index = findWord(path, token);
            if (index >= 0) {
                String swapped = path.substring(0, index) + target + path.substring(index + token.length());
                Optional<Block> found = getBlock(namespace, swapped);
                if (found.isPresent()) {
                    return found;
                }
            }
        }

        for (String wood : WOODS) {
            int index = findWord(path, wood);
            if (index < 0) {
                continue;
            }
            String swapped = path.substring(0, index) + target + path.substring(index + wood.length());
            Optional<Block> found = getBlock(namespace, swapped);
            if (found.isEmpty()) {
                found = getBlock(namespace, BuildingButBetter.MOD_ID + "/" + swapped);
            }
            if (found.isEmpty() && !namespace.equals(BuildingButBetter.MOD_ID)) {
                found = getBlock(BuildingButBetter.MOD_ID, swapped);
            }
            if (found.isPresent()) {
                return found;
            }
        }

        for (int insert : wordStarts(path)) {
            String probe = path.substring(0, insert) + "white_" + path.substring(insert);
            if (getBlock(namespace, probe).isEmpty()) {
                continue;
            }
            Optional<Block> found = getBlock(namespace, path.substring(0, insert) + target + "_" + path.substring(insert));
            if (found.isPresent()) {
                return found;
            }
        }

        return Optional.empty();
    }

    private static Optional<Block> getBlock(String namespace, String path) {
        if (!ResourceLocation.isValidPath(path)) {
            return Optional.empty();
        }
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static BlockState copyStates(BlockState source, BlockState target) {
        BlockState result = target;
        for (Property<?> property : source.getProperties()) {
            if (result.hasProperty(property)) {
                result = copyState(property, source, result);
            }
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState copyState(Property<T> property, BlockState source, BlockState target) {
        return target.setValue(property, source.getValue(property));
    }

    private static String plainName(Block block) {
        return BASE_NAME_CACHE.computeIfAbsent(block, b -> {
            String path = BuiltInRegistries.BLOCK.getKey(b).getPath();
            for (DyeColor dye : COLORS) {
                path = removeWord(path, dye.getName());
            }
            return trimEndings(path);
        });
    }

    private static String removeWord(String path, String token) {
        int index = findWord(path, token);
        while (index >= 0) {
            String left = path.substring(0, index);
            String right = path.substring(index + token.length());
            if (left.endsWith("/") && right.startsWith("_")) {
                right = right.substring(1);
            } else if (left.endsWith("_") && right.startsWith("_")) {
                right = right.substring(1);
            } else if (left.endsWith("_") && right.isEmpty()) {
                left = left.substring(0, left.length() - 1);
            } else if (left.isEmpty() && right.startsWith("_")) {
                right = right.substring(1);
            }
            path = left + right;
            index = findWord(path, token);
        }
        return path;
    }

    private static String trimEndings(String path) {
        path = path.replace("_bricks_", "_brick_");
        for (String suffix : new String[]{"_stairs", "_slab", "_planks"}) {
            if (path.endsWith(suffix)) {
                path = path.substring(0, path.length() - suffix.length());
                break;
            }
        }
        if (path.endsWith("_bricks") || path.endsWith("shingles")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean isBoundary(char character) {
        return character == '_' || character == '/';
    }

    private static int findWord(String path, String token) {
        int index = path.indexOf(token);
        while (index >= 0) {
            int end = index + token.length();
            boolean leftBoundary = index == 0 || isBoundary(path.charAt(index - 1));
            boolean rightBoundary = end == path.length() || isBoundary(path.charAt(end));
            if (leftBoundary && rightBoundary) {
                return index;
            }
            index = path.indexOf(token, index + 1);
        }
        return -1;
    }

    private static int[] wordStarts(String path) {
        int count = 1;
        for (int i = 0; i < path.length() - 1; i++) {
            if (isBoundary(path.charAt(i))) {
                count++;
            }
        }
        int[] positions = new int[count];
        int next = 1;
        for (int i = 0; i < path.length() - 1; i++) {
            if (isBoundary(path.charAt(i))) {
                positions[next++] = i + 1;
            }
        }
        return positions;
    }
}
