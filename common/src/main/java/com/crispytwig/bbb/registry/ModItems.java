package com.crispytwig.bbb.registry;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.item.FacadeItem;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BuildingButBetter.MOD_ID);

    static {
        ModBlocks.BLOCKS.getEntries().forEach(ModItems::registerBlockItem);
    }

    public static final DeferredHolder<Item, FacadeItem> FACADE = ITEMS.register("facade",
            () -> new FacadeItem(new Item.Properties()));

    private ModItems() {
    }

    public static void init() {
    }

    private static void registerBlockItem(DeferredHolder<Block, ? extends Block> holder) {
        ITEMS.register(holder.getId().getPath(), () -> new BlockItem(holder.get(), new Item.Properties()));
    }
}
