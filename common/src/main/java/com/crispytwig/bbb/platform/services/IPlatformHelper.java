package com.crispytwig.bbb.platform.services;

import com.crispytwig.bbb.common.BuildingButBetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.BiFunction;

public interface IPlatformHelper {
    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory);
    }

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory);

    BuildingButBetter.Flammability flammability(Block block);

    void registerCopper(Map<ResourceLocation, ResourceLocation> oxidation, Map<ResourceLocation, ResourceLocation> waxing);

    void tabAfter(CreativeModeTab.Builder builder, ResourceLocation previous);
}
