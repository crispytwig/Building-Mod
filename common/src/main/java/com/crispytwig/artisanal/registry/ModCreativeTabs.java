package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Artisanal.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + Artisanal.MOD_ID))
                    .icon(Items.BRICKS::getDefaultInstance)
                    .displayItems((parameters, output) -> displayItems(output, false))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> COLORED_TAB = CREATIVE_MODE_TABS.register("colored_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                    .title(Component.translatable("itemGroup." + Artisanal.MOD_ID + ".colored"))
                    .icon(Items.RED_TERRACOTTA::getDefaultInstance)
                    .displayItems((parameters, output) -> displayItems(output, true))
                    .build());

    private ModCreativeTabs() {
    }

    public static void init() {
    }

    private static void displayItems(CreativeModeTab.Output output, boolean colored) {
        Set<Block> coloredBlocks = coloredBlocks();
        ModItems.ITEMS.getEntries().stream()
                .map(DeferredHolder::get)
                .filter(item -> isColored(item, coloredBlocks) == colored)
                .forEach(output::accept);
    }

    private static boolean isColored(Item item, Set<Block> coloredBlocks) {
        return item instanceof BlockItem blockItem && coloredBlocks.contains(blockItem.getBlock());
    }

    private static Set<Block> coloredBlocks() {
        return Stream.concat(ModBlocks.TERRACOTTA.stream(), ModBlocks.PLASTER.stream())
                .filter(colored -> colored.color() != null)
                .flatMap(colored -> colored.sets().stream())
                .flatMap(set -> Stream.of(set.block().get(), set.stairs().get(), set.slab().get()))
                .collect(Collectors.toSet());
    }
}
