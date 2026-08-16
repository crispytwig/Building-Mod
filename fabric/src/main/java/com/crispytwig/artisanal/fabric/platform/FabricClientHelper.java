package com.crispytwig.artisanal.fabric.platform;

import com.crispytwig.artisanal.platform.services.IClientHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FabricClientHelper implements IClientHelper {
    @Override
    public void renderModel(Level level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random, long seed) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)), false, random, seed, packedOverlay);
    }
}
