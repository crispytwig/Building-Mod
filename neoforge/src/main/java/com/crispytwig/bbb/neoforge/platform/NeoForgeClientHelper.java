package com.crispytwig.bbb.neoforge.platform;

import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import com.crispytwig.bbb.neoforge.client.renderer.NeoForgeCurtainBlockRenderer;
import com.crispytwig.bbb.platform.services.IClientHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeClientHelper implements IClientHelper {
    @Override
    public BlockEntityRenderer<CurtainBlockEntity> curtainRenderer() {
        return new NeoForgeCurtainBlockRenderer();
    }

    @Override
    public void renderModel(Level level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random, long seed, boolean checkSides) {
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        for (RenderType type : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            renderer.tesselateBlock(level, model, state, pos, poseStack, buffer.getBuffer(type), checkSides, random, seed, packedOverlay, ModelData.EMPTY, type);
        }
    }

    @Override
    public List<RenderType> renderTypes(BakedModel model, BlockState state, RandomSource random) {
        List<RenderType> types = new ArrayList<>();
        model.getRenderTypes(state, random, ModelData.EMPTY).forEach(types::add);
        return types;
    }

    @Override
    public List<BakedQuad> quads(BakedModel model, BlockState state, Direction side, RandomSource random, RenderType type) {
        return model.getQuads(state, side, random, ModelData.EMPTY, type);
    }
}
