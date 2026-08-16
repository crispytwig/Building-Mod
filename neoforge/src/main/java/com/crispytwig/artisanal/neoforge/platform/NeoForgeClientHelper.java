package com.crispytwig.artisanal.neoforge.platform;

import com.crispytwig.artisanal.platform.services.IClientHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class NeoForgeClientHelper implements IClientHelper {
    @Override
    public void renderModel(Level level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random, long seed) {
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            renderer.tesselateBlock(level, model, state, pos, poseStack, buffer.getBuffer(type), false, random, seed, packedOverlay, ModelData.EMPTY, type);
        }
    }
}
