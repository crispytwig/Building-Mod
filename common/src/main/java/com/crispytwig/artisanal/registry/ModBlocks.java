package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.BeamBlock;
import com.crispytwig.artisanal.block.ChairBlock;
import com.crispytwig.artisanal.block.FrameBlock;
import com.crispytwig.artisanal.block.ModStairBlock;
import com.crispytwig.artisanal.block.ShutterBlock;
import com.crispytwig.artisanal.block.TableBlock;
import com.crispytwig.artisanal.block.TimberFrameBlock;
import com.crispytwig.artisanal.block.TrimBlock;
import com.crispytwig.artisanal.block.TrimStairBlock;
import com.crispytwig.artisanal.block.WindowBlock;
import com.crispytwig.artisanal.block.WindowPaneBlock;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
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

    private static final DyeColor[] COLOR_ORDER = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    };

    public static final List<WoodSet> WOOD = createWood();

    public static final DeferredHolder<Block, Block> PRISMARINE_TILES = register("prismarine_tiles", Block::new, prismarineProperties());
    public static final DeferredHolder<Block, ModStairBlock> PRISMARINE_TILE_STAIRS = registerStairs("prismarine_tile_stairs", PRISMARINE_TILES, prismarineProperties());
    public static final DeferredHolder<Block, SlabBlock> PRISMARINE_TILE_SLAB = register("prismarine_tile_slab", SlabBlock::new, prismarineProperties());

    public static final List<ColoredSet> TERRACOTTA = createTerracotta();

    public static final List<ColoredSet> PLASTER = createPlaster();

    public record BlockSet(DeferredHolder<Block, Block> block, DeferredHolder<Block, ModStairBlock> stairs, DeferredHolder<Block, SlabBlock> slab) {
    }

    public record ColoredSet(DyeColor color, List<BlockSet> sets) {
    }

    public record WoodSet(String name, TagKey<Item> logs, Block planks, Block slab, boolean burnable,
                          DeferredHolder<Block, Block> boards,
                          DeferredHolder<Block, ModStairBlock> boardStairs,
                          DeferredHolder<Block, SlabBlock> boardSlab,
                          DeferredHolder<Block, Block> polished,
                          DeferredHolder<Block, SlabBlock> polishedSlab,
                          DeferredHolder<Block, TrimBlock> trim,
                          DeferredHolder<Block, TrimStairBlock> trimStairs,
                          DeferredHolder<Block, RotatedPillarBlock> pillar,
                          DeferredHolder<Block, ModStairBlock> pillarStairs,
                          DeferredHolder<Block, SlabBlock> pillarSlab,
                          DeferredHolder<Block, BeamBlock> beam,
                          DeferredHolder<Block, TableBlock> table,
                          DeferredHolder<Block, ChairBlock> chair,
                          DeferredHolder<Block, FrameBlock> frame,
                          DeferredHolder<Block, TimberFrameBlock> timberFrame,
                          DeferredHolder<Block, ShutterBlock> shutter,
                          DeferredHolder<Block, WindowBlock> window,
                          DeferredHolder<Block, WindowPaneBlock> windowPane) {
        public List<DeferredHolder<Block, ? extends Block>> blocks() {
            return List.of(boards, boardStairs, boardSlab, polished, polishedSlab, trim, trimStairs,
                    pillar, pillarStairs, pillarSlab, beam, table, chair, frame, timberFrame, shutter);
        }

        public List<DeferredHolder<Block, ? extends Block>> windows() {
            return List.of(window, windowPane);
        }
    }

    private ModBlocks() {
    }

    public static void init() {
    }

    public static List<DeferredHolder<Block, ? extends Block>> woodenBlocks() {
        return WOOD.stream().flatMap(set -> set.blocks().stream()).toList();
    }

    public static List<DeferredHolder<Block, ? extends Block>> burnableBlocks() {
        return WOOD.stream().filter(WoodSet::burnable).flatMap(set -> set.blocks().stream()).toList();
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

    private static List<WoodSet> createWood() {
        return List.of(
                woodSet("oak", ItemTags.OAK_LOGS, Blocks.OAK_PLANKS, Blocks.OAK_SLAB, true),
                woodSet("spruce", ItemTags.SPRUCE_LOGS, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_SLAB, true),
                woodSet("birch", ItemTags.BIRCH_LOGS, Blocks.BIRCH_PLANKS, Blocks.BIRCH_SLAB, true),
                woodSet("jungle", ItemTags.JUNGLE_LOGS, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_SLAB, true),
                woodSet("acacia", ItemTags.ACACIA_LOGS, Blocks.ACACIA_PLANKS, Blocks.ACACIA_SLAB, true),
                woodSet("dark_oak", ItemTags.DARK_OAK_LOGS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_SLAB, true),
                woodSet("mangrove", ItemTags.MANGROVE_LOGS, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_SLAB, true),
                woodSet("cherry", ItemTags.CHERRY_LOGS, Blocks.CHERRY_PLANKS, Blocks.CHERRY_SLAB, true),
                woodSet("bamboo", ItemTags.BAMBOO_BLOCKS, Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_SLAB, true),
                woodSet("crimson", ItemTags.CRIMSON_STEMS, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_SLAB, false),
                woodSet("warped", ItemTags.WARPED_STEMS, Blocks.WARPED_PLANKS, Blocks.WARPED_SLAB, false));
    }

    private static WoodSet woodSet(String name, TagKey<Item> logs, Block planks, Block slab, boolean burnable) {
        Supplier<BlockBehaviour.Properties> properties = () -> BlockBehaviour.Properties.ofFullCopy(planks);
        DeferredHolder<Block, Block> boards = register(name + "_boards", Block::new, properties.get());
        DeferredHolder<Block, Block> polished = register("polished_" + name, Block::new, properties.get());
        DeferredHolder<Block, TrimBlock> trim = register(name + "_trim", TrimBlock::new, properties.get());
        DeferredHolder<Block, RotatedPillarBlock> pillar = register(name + "_pillar", RotatedPillarBlock::new, properties.get());
        return new WoodSet(name, logs, planks, slab, burnable,
                boards,
                registerStairs(name + "_board_stairs", boards, properties.get()),
                register(name + "_board_slab", SlabBlock::new, properties.get()),
                polished,
                register("polished_" + name + "_slab", SlabBlock::new, properties.get()),
                trim,
                BLOCKS.register(name + "_trim_stairs", () -> new TrimStairBlock(trim.get().defaultBlockState(), properties.get())),
                pillar,
                registerStairs(name + "_pillar_stairs", pillar, properties.get()),
                register(name + "_pillar_slab", SlabBlock::new, properties.get()),
                register(name + "_beam", BeamBlock::new, properties.get().noOcclusion()),
                register(name + "_table", TableBlock::new, decorativeProperties(planks.defaultMapColor(), planks.defaultBlockState().getSoundType()).strength(2.5F).dynamicShape()),
                register(name + "_chair", ChairBlock::new, decorativeProperties(planks.defaultMapColor(), planks.defaultBlockState().getSoundType()).strength(2.5F)),
                register(name + "_frame", FrameBlock::new, properties.get().noOcclusion()),
                register(name + "_timber_frame", TimberFrameBlock::new, properties.get().noOcclusion().sound(SoundType.SCAFFOLDING)),
                register(name + "_shutter", ShutterBlock::new, properties.get().noOcclusion()),
                register(name + "_window", WindowBlock::new, windowProperties(planks.defaultMapColor())),
                register(name + "_window_pane", WindowPaneBlock::new, windowProperties(planks.defaultMapColor())));
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
        BlockSet cobbled = registerSet("cobbled_" + prefix, "cobbled_" + prefix, () -> stoneProperties(mapColor, SoundType.DRIPSTONE_BLOCK));
        BlockSet tiles = registerSet(prefix + "_tiles", prefix + "_tile", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        BlockSet bricks = registerSet(prefix + "_bricks", prefix + "_brick", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        BlockSet shingles = registerSet(prefix + "_shingles", prefix + "_shingle", () -> stoneProperties(mapColor, SoundType.DEEPSLATE_TILES));
        return new ColoredSet(color, List.of(cobbled, tiles, bricks, shingles));
    }

    private static List<ColoredSet> createPlaster() {
        List<ColoredSet> sets = new ArrayList<>();
        for (DyeColor color : COLOR_ORDER) {
            String name = color.getName() + "_plaster";
            sets.add(new ColoredSet(color, List.of(registerSet(name, name, () -> stoneProperties(color.getMapColor(), SoundType.TUFF)))));
        }
        return List.copyOf(sets);
    }

    private static BlockBehaviour.Properties prismarineProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS);
    }

    private static BlockBehaviour.Properties stoneProperties(MapColor mapColor, SoundType soundType) {
        return BlockBehaviour.Properties.of().mapColor(mapColor).strength(1.25F, 4.2F).sound(soundType).requiresCorrectToolForDrops();
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
