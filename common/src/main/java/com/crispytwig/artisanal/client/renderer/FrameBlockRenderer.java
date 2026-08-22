package com.crispytwig.artisanal.client.renderer;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.FrameBlock;
import com.crispytwig.artisanal.block.entity.FrameBlockEntity;
import com.crispytwig.artisanal.client.ClientServices;
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

public class FrameBlockRenderer implements BlockEntityRenderer<FrameBlockEntity> {
    private static final Direction[] CULL_FACES = {
            null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private static final Map<Block, Map<String, ModelResourceLocation>> PART_MODELS = new HashMap<>();

    private final RandomSource random = RandomSource.create();

    public static ResourceLocation partLocation(String frameName, String part) {
        return Artisanal.location("block/" + frameName + "_" + part);
    }

    public static void cachePartModels(Block block, String frameName) {
        Map<String, ModelResourceLocation> models = new HashMap<>();
        for (String part : FrameBlock.PARTS) {
            models.put(part, new ModelResourceLocation(partLocation(frameName, part), "standalone"));
        }
        PART_MODELS.put(block, models);
    }

    @Override
    public void render(FrameBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        BlockState state = blockEntity.getBlockState();
        Map<String, ModelResourceLocation> models = PART_MODELS.get(state.getBlock());
        if (level == null || models == null) {
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        Direction facing = state.getValue(FrameBlock.FACING);
        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        long seed = state.getSeed(pos);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(Math.floorMod(180 - (int) facing.toYRot(), 360)));
        poseStack.translate(-0.5, 0.0, -0.5);
        PoseStack.Pose pose = poseStack.last();

        for (String part : FrameBlock.visibleParts(level, pos, facing)) {
            BakedModel model = modelManager.getModel(models.get(part));
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
        }

        poseStack.popPose();
    }

    private static Direction worldDirection(Direction modelDirection, Direction facing) {
        if (modelDirection.getAxis() == Direction.Axis.Y) {
            return modelDirection;
        }
        int steps = switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        Direction worldDirection = modelDirection;
        for (int i = 0; i < steps; i++) {
            worldDirection = worldDirection.getClockWise();
        }
        return worldDirection;
    }
}
