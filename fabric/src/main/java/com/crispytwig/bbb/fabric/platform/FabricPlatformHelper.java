package com.crispytwig.bbb.fabric.platform;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.BiFunction;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
    }

    @Override
    public BuildingButBetter.Flammability flammability(Block block) {
        FlammableBlockRegistry.Entry entry = FlammableBlockRegistry.getDefaultInstance().get(block);
        if (entry == null || entry.getBurnChance() == 0 && entry.getSpreadChance() == 0) {
            return null;
        }
        return new BuildingButBetter.Flammability(entry.getSpreadChance(), entry.getBurnChance());
    }

    @Override
    public void registerCopper(Map<ResourceLocation, ResourceLocation> oxidation, Map<ResourceLocation, ResourceLocation> waxing) {
        oxidation.forEach((less, more) -> OxidizableBlocksRegistry.registerOxidizableBlockPair(BuiltInRegistries.BLOCK.get(less), BuiltInRegistries.BLOCK.get(more)));
        waxing.forEach((unwaxed, waxed) -> OxidizableBlocksRegistry.registerWaxableBlockPair(BuiltInRegistries.BLOCK.get(unwaxed), BuiltInRegistries.BLOCK.get(waxed)));
    }

    @Override
    public void tabAfter(CreativeModeTab.Builder builder, ResourceLocation previous) {
    }
}
