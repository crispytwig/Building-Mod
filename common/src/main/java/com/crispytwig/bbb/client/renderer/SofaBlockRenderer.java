package com.crispytwig.bbb.client.renderer;

import com.crispytwig.bbb.client.paint.PaintOverride;
import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.SofaBlock;
import com.crispytwig.bbb.common.block.entity.SofaBlockEntity;
import com.crispytwig.bbb.client.ClientServices;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class SofaBlockRenderer implements BlockEntityRenderer<SofaBlockEntity> {
    private static final Direction[] CULL_FACES = {
            null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private static final Map<Block, Map<String, ModelResourceLocation>> PART_MODELS = new HashMap<>();

    private final RandomSource random = RandomSource.create();

    public static ResourceLocation partLocation(String sofaName, String part) {
        return BuildingButBetter.location("block/" + sofaName + "_" + part);
    }

    public static void cachePartModels(Block block, String sofaName) {
        Map<String, ModelResourceLocation> models = new HashMap<>();
        for (String part : SofaBlock.PARTS) {
            models.put(part, new ModelResourceLocation(partLocation(sofaName, part), "standalone"));
        }
        PART_MODELS.put(block, models);
    }

    @Override
    public void render(SofaBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        BlockState state = PaintOverride.apply(blockEntity.getBlockPos(), blockEntity.getBlockState());
        Map<String, ModelResourceLocation> models = PART_MODELS.get(state.getBlock());
        if (level == null || models == null) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        Direction facing = state.getValue(SofaBlock.FACING);
        boolean left = state.getValue(SofaBlock.LEFT);
        boolean right = state.getValue(SofaBlock.RIGHT);
        boolean behind = SofaBlock.occupied(level, pos, facing.getOpposite());
        boolean above = SofaBlock.occupied(level, pos, Direction.UP);
        boolean front = SofaBlock.occupied(level, pos, facing);
        boolean leftBlocked = SofaBlock.occupied(level, pos, facing.getCounterClockWise());
        boolean rightBlocked = SofaBlock.occupied(level, pos, facing.getClockWise());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(Math.floorMod(180 - (int) facing.toYRot(), 360)));
        poseStack.translate(-0.5, 0.0, -0.5);

        renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "backrest",
                0.0F, above ? SofaBlock.TUCK_BACKREST_DOWN : 0.0F, behind ? SofaBlock.TUCK_BACKREST_BACK : 0.0F);
        renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "seat",
                0.0F, above ? SofaBlock.TUCK_DOWN : 0.0F, front ? SofaBlock.TUCK_FRONT : 0.0F);

        boolean single = !left && !right;
        if (!left) {
            if (!(single && leftBlocked)) {
                renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "arm_left",
                        leftBlocked ? SofaBlock.TUCK_ARM_SIDE : 0.0F, above ? SofaBlock.TUCK_DOWN : 0.0F, front ? SofaBlock.TUCK_FRONT : 0.0F);
            }
            if (!above) {
                renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "legs_left", 0.0F, 0.0F, 0.0F);
            }
        }
        if (!right) {
            if (!(single && rightBlocked)) {
                renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "arm_right",
                        rightBlocked ? -SofaBlock.TUCK_ARM_SIDE : 0.0F, above ? SofaBlock.TUCK_DOWN : 0.0F, front ? SofaBlock.TUCK_FRONT : 0.0F);
            }
            if (!above) {
                renderPart(level, models, state, pos, facing, poseStack, buffer, packedLight, packedOverlay, "legs_right", 0.0F, 0.0F, 0.0F);
            }
        }

        poseStack.popPose();
    }

    private void renderPart(Level level, Map<String, ModelResourceLocation> models, BlockState state, BlockPos pos, Direction facing,
                            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                            String part, float offsetX, float offsetY, float offsetZ) {
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        BakedModel model = modelManager.getModel(models.get(part));
        long seed = state.getSeed(pos);

        poseStack.pushPose();
        poseStack.translate(offsetX / 16.0F, offsetY / 16.0F, offsetZ / 16.0F);
        PoseStack.Pose pose = poseStack.last();

        for (RenderType type : ClientServices.CLIENT.renderTypes(model, state, random)) {
            VertexConsumer consumer = buffer.getBuffer(type);
            for (Direction cull : CULL_FACES) {
                random.setSeed(seed);
                for (BakedQuad quad : ClientServices.CLIENT.quads(model, state, cull, random, type)) {
                    float shade = level.getShade(worldDirection(quad.getDirection(), facing), quad.isShade());
                    consumer.putBulkData(pose, quad, shade, shade, shade, 1.0F, packedLight, packedOverlay);
                }
            }
        }

        poseStack.popPose();
    }

    private static Direction worldDirection(Direction modelDirection, Direction facing) {
        if (modelDirection.getAxis() == Direction.Axis.Y) {
            return modelDirection;
        }
        Direction worldDirection = modelDirection;
        for (int i = 0; i < SofaBlock.quarterTurns(facing); i++) {
            worldDirection = worldDirection.getClockWise();
        }
        return worldDirection;
    }
}
