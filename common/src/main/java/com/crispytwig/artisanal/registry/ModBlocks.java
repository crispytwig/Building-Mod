package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.BeamBlock;
import com.crispytwig.artisanal.block.ChairBlock;
import com.crispytwig.artisanal.block.ModStairBlock;
import com.crispytwig.artisanal.block.ShutterBlock;
import com.crispytwig.artisanal.block.TableBlock;
import com.crispytwig.artisanal.block.TrimBlock;
import com.crispytwig.artisanal.block.TrimStairBlock;
import com.crispytwig.artisanal.block.WindowBlock;
import com.crispytwig.artisanal.block.WindowPaneBlock;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Artisanal.MOD_ID);

    private static final List<DeferredHolder<Block, ? extends Block>> WOODEN_BLOCKS = new ArrayList<>();

    private static final DyeColor[] COLOR_ORDER = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    };

    public static final DeferredHolder<Block, Block> OAK_BOARDS = registerWooden("oak_boards", Block::new, oakProperties());
    public static final DeferredHolder<Block, ModStairBlock> OAK_BOARD_STAIRS = registerWoodenStairs("oak_board_stairs", OAK_BOARDS, oakProperties());
    public static final DeferredHolder<Block, SlabBlock> OAK_BOARD_SLAB = registerWooden("oak_board_slab", SlabBlock::new, oakProperties());
    public static final DeferredHolder<Block, Block> POLISHED_OAK = registerWooden("polished_oak", Block::new, oakProperties());
    public static final DeferredHolder<Block, SlabBlock> POLISHED_OAK_SLAB = registerWooden("polished_oak_slab", SlabBlock::new, oakProperties());

    public static final DeferredHolder<Block, TrimBlock> OAK_TRIM = registerWooden("oak_trim", TrimBlock::new, oakProperties());
    public static final DeferredHolder<Block, TrimStairBlock> OAK_TRIM_STAIRS = registerWoodenTrimStairs("oak_trim_stairs", OAK_TRIM, oakProperties());

    public static final DeferredHolder<Block, RotatedPillarBlock> OAK_PILLAR = registerWooden("oak_pillar", RotatedPillarBlock::new, oakProperties());
    public static final DeferredHolder<Block, ModStairBlock> OAK_PILLAR_STAIRS = registerWoodenStairs("oak_pillar_stairs", OAK_PILLAR, oakProperties());
    public static final DeferredHolder<Block, SlabBlock> OAK_PILLAR_SLAB = registerWooden("oak_pillar_slab", SlabBlock::new, oakProperties());
    public static final DeferredHolder<Block, BeamBlock> OAK_BEAM = registerWooden("oak_beam", BeamBlock::new, oakProperties().noOcclusion());

    public static final DeferredHolder<Block, TableBlock> OAK_TABLE = registerWooden("oak_table", TableBlock::new, decorativeProperties(MapColor.WOOD, SoundType.WOOD).strength(2.5F).dynamicShape());
    public static final DeferredHolder<Block, ChairBlock> OAK_CHAIR = registerWooden("oak_chair", ChairBlock::new, decorativeProperties(MapColor.WOOD, SoundType.WOOD).strength(2.5F));

    public static final DeferredHolder<Block, ShutterBlock> OAK_SHUTTER = registerWooden("oak_shutter", ShutterBlock::new, oakProperties().noOcclusion());

    public static final DeferredHolder<Block, WindowBlock> OAK_WINDOW = register("oak_window", WindowBlock::new, windowProperties(MapColor.WOOD));
    public static final DeferredHolder<Block, WindowPaneBlock> OAK_WINDOW_PANE = register("oak_window_pane", WindowPaneBlock::new, windowProperties(MapColor.WOOD));

    public static final DeferredHolder<Block, Block> PRISMARINE_TILES = register("prismarine_tiles", Block::new, prismarineProperties());
    public static final DeferredHolder<Block, ModStairBlock> PRISMARINE_TILE_STAIRS = registerStairs("prismarine_tile_stairs", PRISMARINE_TILES, prismarineProperties());
    public static final DeferredHolder<Block, SlabBlock> PRISMARINE_TILE_SLAB = register("prismarine_tile_slab", SlabBlock::new, prismarineProperties());

    public static final List<ColoredSet> TERRACOTTA = createTerracotta();

    public static final List<ColoredSet> PLASTER = createPlaster();

    public record BlockSet(DeferredHolder<Block, Block> block, DeferredHolder<Block, ModStairBlock> stairs, DeferredHolder<Block, SlabBlock> slab) {
    }

    public record ColoredSet(DyeColor color, List<BlockSet> sets) {
    }

    private ModBlocks() {
    }

    public static void init() {
    }

    public static List<DeferredHolder<Block, ? extends Block>> woodenBlocks() {
        return List.copyOf(WOODEN_BLOCKS);
    }

    public static Block vanillaTerracotta(DyeColor color) {
        if (color == null) {
            return Blocks.TERRACOTTA;
        }
        return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(color.getName() + "_terracotta"));
    }

    public static <T extends Block> DeferredHolder<Block, T> register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> factory.apply(properties));
    }

    public static DeferredHolder<Block, ModStairBlock> registerStairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new ModStairBlock(base.get().defaultBlockState(), properties));
    }

    private static <T extends Block> DeferredHolder<Block, T> registerWooden(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return markWooden(register(name, factory, properties));
    }

    private static DeferredHolder<Block, ModStairBlock> registerWoodenStairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties properties) {
        return markWooden(registerStairs(name, base, properties));
    }

    private static DeferredHolder<Block, TrimStairBlock> registerWoodenTrimStairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties properties) {
        return markWooden(BLOCKS.register(name, () -> new TrimStairBlock(base.get().defaultBlockState(), properties)));
    }

    private static <T extends Block> DeferredHolder<Block, T> markWooden(DeferredHolder<Block, T> holder) {
        WOODEN_BLOCKS.add(holder);
        return holder;
    }

    private static BlockSet registerSet(String pluralName, String singularName, Supplier<BlockBehaviour.Properties> properties) {
        DeferredHolder<Block, Block> block = register(pluralName, Block::new, properties.get());
        return new BlockSet(block, registerStairs(singularName + "_stairs", block, properties.get()), register(singularName + "_slab", SlabBlock::new, properties.get()));
    }

    private static List<ColoredSet> createTerracotta() {
        List<ColoredSet> sets = new ArrayList<>();
        sets.add(terracottaSet(null));
        for (DyeColor color : COLOR_ORDER) {
            sets.add(terracottaSet(color));
        }
        return List.copyOf(sets);
    }

    private static ColoredSet terracottaSet(DyeColor color) {
        String prefix = color == null ? "terracotta" : color.getName() + "_terracotta";
        MapColor mapColor = vanillaTerracotta(color).defaultMapColor();
        BlockSet bricks = registerSet(prefix + "_bricks", prefix + "_brick", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        BlockSet tiles = registerSet(prefix + "_tiles", prefix + "_tile", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        BlockSet shingles = registerSet(prefix + "_shingles", prefix + "_shingle", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        return new ColoredSet(color, List.of(bricks, tiles, shingles));
    }

    private static List<ColoredSet> createPlaster() {
        List<ColoredSet> sets = new ArrayList<>();
        for (DyeColor color : COLOR_ORDER) {
            String name = color.getName() + "_plaster";
            sets.add(new ColoredSet(color, List.of(registerSet(name, name, () -> stoneProperties(color.getMapColor(), SoundType.TUFF)))));
        }
        return List.copyOf(sets);
    }

    private static BlockBehaviour.Properties oakProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS);
    }

    private static BlockBehaviour.Properties prismarineProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS);
    }

    private static BlockBehaviour.Properties stoneProperties(MapColor mapColor, SoundType soundType) {
        return BlockBehaviour.Properties.of().mapColor(mapColor).strength(1.25F, 4.2F).sound(soundType);
    }

    private static BlockBehaviour.Properties windowProperties(MapColor mapColor) {
        return decorativeProperties(mapColor, SoundType.GLASS).strength(0.3F);
    }

    private static BlockBehaviour.Properties decorativeProperties(MapColor mapColor, SoundType soundType) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .sound(soundType)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entity) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }
}
