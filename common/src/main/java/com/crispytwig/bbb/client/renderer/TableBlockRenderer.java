package com.crispytwig.bbb.client.renderer;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.TableBlock;
import com.crispytwig.bbb.common.block.entity.TableBlockEntity;
import com.crispytwig.bbb.client.ClientServices;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class TableBlockRenderer implements BlockEntityRenderer<TableBlockEntity> {
    private static final Map<Block, ModelResourceLocation[]> PART_MODELS = new HashMap<>();

    private final RandomSource random = RandomSource.create();

    public static ResourceLocation partLocation(String tableName, String part) {
        return BuildingButBetter.location("block/" + tableName + "_" + part);
    }

    public static void cachePartModels(Block block, String tableName) {
        ModelResourceLocation[] models = new ModelResourceLocation[TableBlock.LEG_PARTS.length + 1];
        models[0] = standalone(tableName, TableBlock.TOP_PART);
        for (int i = 0; i < TableBlock.LEG_PARTS.length; i++) {
            models[i + 1] = standalone(tableName, TableBlock.LEG_PARTS[i]);
        }
        PART_MODELS.put(block, models);
    }

    @Override
    public void render(TableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        BlockState state = blockEntity.getBlockState();
        ModelResourceLocation[] models = PART_MODELS.get(state.getBlock());
        if (level == null || models == null) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        long seed = state.getSeed(pos);
        int legs = TableBlock.legMask(level, pos);

        ClientServices.CLIENT.renderModel(level, modelManager.getModel(models[0]), state, pos, poseStack, buffer, packedOverlay, random, seed, false);
        for (int i = 0; i < TableBlock.LEG_PARTS.length; i++) {
            if (TableBlock.hasLeg(legs, i)) {
                ClientServices.CLIENT.renderModel(level, modelManager.getModel(models[i + 1]), state, pos, poseStack, buffer, packedOverlay, random, seed, false);
            }
        }
    }

    private static ModelResourceLocation standalone(String tableName, String part) {
        return new ModelResourceLocation(partLocation(tableName, part), "standalone");
    }
}
