package com.crispytwig.bbb.registry;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, BuildingButBetter.MOD_ID);

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
