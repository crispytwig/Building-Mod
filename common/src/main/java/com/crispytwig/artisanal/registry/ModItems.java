package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Artisanal.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> OAK_BOARDS = registerBlockItem("oak_boards", ModBlocks.OAK_BOARDS);
    public static final DeferredHolder<Item, BlockItem> OAK_BOARD_STAIRS = registerBlockItem("oak_board_stairs", ModBlocks.OAK_BOARD_STAIRS);
    public static final DeferredHolder<Item, BlockItem> OAK_BOARD_SLAB = registerBlockItem("oak_board_slab", ModBlocks.OAK_BOARD_SLAB);
    public static final DeferredHolder<Item, BlockItem> POLISHED_OAK = registerBlockItem("polished_oak", ModBlocks.POLISHED_OAK);

    public static final DeferredHolder<Item, BlockItem> OAK_TRIM = registerBlockItem("oak_trim", ModBlocks.OAK_TRIM);
    public static final DeferredHolder<Item, BlockItem> OAK_TRIM_STAIRS = registerBlockItem("oak_trim_stairs", ModBlocks.OAK_TRIM_STAIRS);
    public static final DeferredHolder<Item, BlockItem> OAK_TRIM_SLAB = registerBlockItem("oak_trim_slab", ModBlocks.OAK_TRIM_SLAB);

    private ModItems() {
    }

    public static void init() {
    }

    public static DeferredHolder<Item, Item> registerItem(String name, Item.Properties properties) {
        return ITEMS.register(name, () -> new Item(properties));
    }

    public static DeferredHolder<Item, BlockItem> registerBlockItem(String name, Supplier<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
