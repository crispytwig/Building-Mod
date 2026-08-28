package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.menu.SofaMenu;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.Services;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SofaMenu>> SOFA =
            MENU_TYPES.register("sofa", () -> Services.PLATFORM.createMenuType(SofaMenu::new));

    private ModMenuTypes() {
    }

    public static void init() {
    }
}
