package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Artisanal.MOD_ID);

    static {
        ModBlocks.BLOCKS.getEntries().forEach(ModItems::registerBlockItem);
    }

    private ModItems() {
    }

    public static void init() {
    }

    private static void registerBlockItem(DeferredHolder<Block, ? extends Block> holder) {
        ITEMS.register(holder.getId().getPath(), () -> new BlockItem(holder.get(), new Item.Properties()));
    }
}
