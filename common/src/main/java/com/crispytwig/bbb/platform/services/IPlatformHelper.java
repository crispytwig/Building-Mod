package com.crispytwig.bbb.platform.services;

import com.crispytwig.bbb.BuildingButBetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.BiFunction;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks);

    BuildingButBetter.Flammability flammability(Block block);

    void registerCopper(Map<ResourceLocation, ResourceLocation> oxidation, Map<ResourceLocation, ResourceLocation> waxing);

    void tabAfter(CreativeModeTab.Builder builder, ResourceLocation previous);
}
