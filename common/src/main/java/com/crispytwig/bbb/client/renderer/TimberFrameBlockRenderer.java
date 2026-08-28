package com.crispytwig.bbb.client.renderer;

import com.crispytwig.bbb.common.block.entity.TimberFrameBlockEntity;
import com.crispytwig.bbb.client.ClientServices;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TimberFrameBlockRenderer implements BlockEntityRenderer<TimberFrameBlockEntity> {
    private final RandomSource random = RandomSource.create();

    @Override
    public void render(TimberFrameBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        BlockState held = blockEntity.getHeldBlock();
        if (level == null || held.isAir()) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = blockEntity.getBlockState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        ClientServices.CLIENT.renderModel(level, dispatcher.getBlockModel(held), held, pos,
                poseStack, buffer, packedOverlay, random, held.getSeed(pos), false);
        ClientServices.CLIENT.renderModel(level, FacadeModel.wrap(dispatcher.getBlockModel(state), state, pos), state, pos,
                poseStack, buffer, packedOverlay, random, state.getSeed(pos), false);
    }
}
