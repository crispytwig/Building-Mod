package com.crispytwig.bbb.fabric.platform;

import com.crispytwig.bbb.client.renderer.CurtainBlockRenderer;
import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import com.crispytwig.bbb.platform.services.IClientHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FabricClientHelper implements IClientHelper {
    @Override
    public BlockEntityRenderer<CurtainBlockEntity> curtainRenderer() {
        return new CurtainBlockRenderer();
    }

    @Override
    public void renderModel(Level level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random, long seed, boolean checkSides) {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, model, state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)), checkSides, random, seed, packedOverlay);
    }

    @Override
    public List<RenderType> renderTypes(BakedModel model, BlockState state, RandomSource random) {
        return List.of(ItemBlockRenderTypes.getRenderType(state, false));
    }

    @Override
    public List<BakedQuad> quads(BakedModel model, BlockState state, Direction side, RandomSource random, RenderType type) {
        return model.getQuads(state, side, random);
    }
}
