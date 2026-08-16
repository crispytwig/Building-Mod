package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.ModStairBlock;
import com.crispytwig.artisanal.block.TrimBlock;
import com.crispytwig.artisanal.block.TrimStairBlock;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Artisanal.MOD_ID);

    public static final DeferredHolder<Block, Block> OAK_BOARDS = register("oak_boards",
            Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, ModStairBlock> OAK_BOARD_STAIRS = registerStairs("oak_board_stairs",
            OAK_BOARDS, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, SlabBlock> OAK_BOARD_SLAB = register("oak_board_slab",
            SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, Block> POLISHED_OAK = register("polished_oak",
            Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, TrimBlock> OAK_TRIM = register("oak_trim",
            TrimBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, TrimStairBlock> OAK_TRIM_STAIRS = registerTrimStairs("oak_trim_stairs",
            OAK_TRIM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    public static final DeferredHolder<Block, SlabBlock> OAK_TRIM_SLAB = register("oak_trim_slab",
            SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));

    private ModBlocks() {
    }

    public static void init() {
    }

    public static <T extends Block> DeferredHolder<Block, T> register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> factory.apply(properties));
    }

    public static DeferredHolder<Block, ModStairBlock> registerStairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new ModStairBlock(base.get().defaultBlockState(), properties));
    }

    public static DeferredHolder<Block, TrimStairBlock> registerTrimStairs(String name, Supplier<? extends Block> base, BlockBehaviour.Properties properties) {
        return BLOCKS.register(name, () -> new TrimStairBlock(base.get().defaultBlockState(), properties));
    }
}
