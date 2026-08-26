package com.crispytwig.bbb.platform.services;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IClientHelper {
    void renderModel(Level level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random, long seed);

    List<RenderType> renderTypes(BakedModel model, BlockState state, RandomSource random);

    List<BakedQuad> quads(BakedModel model, BlockState state, Direction side, RandomSource random, RenderType type);
}
