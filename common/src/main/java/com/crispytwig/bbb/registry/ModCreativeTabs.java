package com.crispytwig.bbb.registry;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup." + BuildingButBetter.MOD_ID))
                    .icon(Items.BRICKS::getDefaultInstance)
                    .displayItems((parameters, output) -> ModItems.ITEMS.getEntries()
                            .forEach(entry -> output.accept(entry.get())))
                    .build());

    private ModCreativeTabs() {
    }

    public static void init() {
    }
}
