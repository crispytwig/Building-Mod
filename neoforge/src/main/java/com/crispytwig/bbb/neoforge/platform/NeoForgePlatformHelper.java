package com.crispytwig.bbb.neoforge.platform;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.data.BuildingButBetterPack;
import com.crispytwig.bbb.platform.services.IPlatformHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Map;
import java.util.function.BiFunction;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        return IMenuTypeExtension.create((containerId, playerInventory, buffer) -> factory.create(containerId, playerInventory));
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> factory, Block... blocks) {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
    }

    @Override
    public BuildingButBetter.Flammability flammability(Block block) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        int encouragement = fire.getIgniteOdds(block.defaultBlockState());
        int flammability = fire.getBurnOdds(block.defaultBlockState());
        return encouragement == 0 && flammability == 0 ? null : new BuildingButBetter.Flammability(encouragement, flammability);
    }

    // Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
    @Override
    public void registerCopper(Map<ResourceLocation, ResourceLocation> oxidation, Map<ResourceLocation, ResourceLocation> waxing) {
        writeDataMap("oxidizables", "next_oxidation_stage", oxidation);
        writeDataMap("waxables", "waxed", waxing);
    }

    @Override
    public void tabAfter(CreativeModeTab.Builder builder, ResourceLocation previous) {
        builder.withTabsBefore(previous);
    }

    private static void writeDataMap(String name, String field, Map<ResourceLocation, ResourceLocation> pairs) {
        JsonObject values = new JsonObject();
        pairs.forEach((from, to) -> {
            JsonObject value = new JsonObject();
            value.addProperty(field, to.toString());
            values.add(from.toString(), value);
        });
        JsonObject dataMap = new JsonObject();
        dataMap.add("values", values);
        BuildingButBetterPack.INSTANCE.add(PackType.SERVER_DATA,
                ResourceLocation.fromNamespaceAndPath("neoforge", "data_maps/block/" + name + ".json"), dataMap);
    }
}
