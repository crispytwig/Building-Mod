package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.CurtainBlock;
import com.crispytwig.bbb.common.item.CurtainBlockItem;
import com.crispytwig.bbb.common.item.FacadeItem;
import com.crispytwig.bbb.common.item.PaintBrushItem;
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

    public static final DeferredHolder<Item, PaintBrushItem> PAINT_BRUSH = ITEMS.register("paint_brush",
            () -> new PaintBrushItem(new Item.Properties().stacksTo(1).durability(256)));

    private ModItems() {
    }

    public static void init() {
    }

    private static void registerBlockItem(DeferredHolder<Block, ? extends Block> holder) {
        ITEMS.register(holder.getId().getPath(), () -> {
            Block block = holder.get();
            if (block instanceof CurtainBlock) {
                return new CurtainBlockItem(block, new Item.Properties());
            }
            return new BlockItem(block, new Item.Properties());
        });
    }
}
