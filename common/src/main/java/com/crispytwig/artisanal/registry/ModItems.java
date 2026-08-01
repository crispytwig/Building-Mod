package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.item.PanelItem;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Artisanal.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> OAK_TRIM = registerBlockItem("oak_trim", ModBlocks.OAK_TRIM);
    public static final DeferredHolder<Item, BlockItem> OAK_TRIM_STAIRS = registerBlockItem("oak_trim_stairs", ModBlocks.OAK_TRIM_STAIRS);
    public static final DeferredHolder<Item, BlockItem> OAK_TRIM_SLAB = registerBlockItem("oak_trim_slab", ModBlocks.OAK_TRIM_SLAB);

    public static final DeferredHolder<Item, ArchitectsScepterItem> ARCHITECTS_SCEPTER = ITEMS.register("architects_scepter",
            () -> new ArchitectsScepterItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, SpawnEggItem> WRIGHT_SPAWN_EGG = ITEMS.register("wright_spawn_egg",
            () -> new SpawnEggItem(ModEntityTypes.WRIGHT.get(), 0xF1D319, 0xD19303, new Item.Properties()));

    public static final DeferredHolder<Item, PanelItem> PANEL = ITEMS.register("panel",
            () -> new PanelItem(new Item.Properties()));

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
