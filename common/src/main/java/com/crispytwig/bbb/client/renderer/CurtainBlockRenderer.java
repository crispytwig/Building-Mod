package com.crispytwig.bbb.client.renderer;

import com.crispytwig.bbb.client.ClientServices;
import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.CurtainBlock;
import com.crispytwig.bbb.common.block.CurtainSide;
import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CurtainBlockRenderer implements BlockEntityRenderer<CurtainBlockEntity> {
    private static final Map<Block, Map<String, ModelResourceLocation>> PART_MODELS = new HashMap<>();

    private final RandomSource random = RandomSource.create();

    public static ResourceLocation partLocation(String curtainName, String part) {
        return BuildingButBetter.location("block/" + curtainName + "_" + part);
    }

    public static void cachePartModels(Block block, String curtainName) {
        Map<String, ModelResourceLocation> models = new HashMap<>();
        for (String part : CurtainBlock.PARTS) {
            models.put(part, new ModelResourceLocation(partLocation(curtainName, part), "standalone"));
        }
        PART_MODELS.put(block, models);
    }

    @Override
    public boolean shouldRenderOffScreen(CurtainBlockEntity blockEntity) {
        return true;
    }

    @Override
    public void render(CurtainBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        renderParts(level, blockEntity.getBlockState(), blockEntity.getBlockPos(), blockEntity.getLength(),
                poseStack, buffer, packedOverlay, random);
    }

    public static void renderParts(Level level, BlockState state, BlockPos pos, int length,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, RandomSource random) {
        Map<String, ModelResourceLocation> models = PART_MODELS.get(state.getBlock());
        if (models == null) {
            return;
        }

        boolean open = state.getValue(CurtainBlock.OPEN);
        CurtainSide side = state.getValue(CurtainBlock.SIDE);
        Direction facing = state.getValue(CurtainBlock.FACING);
        int yRot = Math.floorMod(180 - (int) facing.toYRot(), 360);

        for (int i = 0; i < length; i++) {
            String part = partFor(open, side, i, length);
            if (part == null) {
                continue;
            }

            BlockPos cellPos = pos.below(i);
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(models.get(part));

            poseStack.pushPose();
            poseStack.translate(0.0, -i, 0.0);
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.translate(-0.5, 0.0, -0.5);
            poseStack.translate(0.0, 0.0, 0.001);

            ClientServices.CLIENT.renderModel(level, model, state, cellPos, poseStack, buffer, packedOverlay, random, state.getSeed(cellPos), false);

            poseStack.popPose();
        }
    }

    private static @Nullable String partFor(boolean open, CurtainSide side, int index, int length) {
        boolean single = length == 1;
        boolean top = single || index == 0;
        String vertical = top ? "top" : (index == length - 1 ? "bot" : "mid");

        if (open) {
            return switch (side) {
                case LEFT -> single ? "open_single_right" : "open_" + vertical + "_right";
                case RIGHT -> single ? "open_single_left" : "open_" + vertical + "_left";
                default -> top ? "open_top_middle" : null;
            };
        }
        return single ? "closed_top_single" : "closed_" + vertical + "_middle";
    }
}
