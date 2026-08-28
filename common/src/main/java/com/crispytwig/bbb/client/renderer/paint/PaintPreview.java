package com.crispytwig.bbb.client.renderer.paint;

import com.crispytwig.bbb.client.ClientServices;
import com.crispytwig.bbb.common.paint.BlockRecolor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class PaintPreview implements VertexConsumer {
    private static final int START_SIZE = 4096;
    private static final int OPAQUE = 255;

    private float[] xyz = new float[START_SIZE * 3];
    private float[] uv = new float[START_SIZE * 2];
    private int[] rgb = new int[START_SIZE];
    private int[] light = new int[START_SIZE * 2];
    private float[] normal = new float[START_SIZE * 3];
    private int count;

    private @Nullable BlockPos origin;

    private float x, y, z;
    private float u, v;
    private int r = 255, g = 255, b = 255;
    private int lightU, lightV;

    public void clear() {
        count = 0;
        origin = null;
    }

    public void build(Level level, BlockPos from, BlockPos to, DyeColor color, @Nullable BlockState filter) {
        count = 0;
        origin = from;

        BlockRenderDispatcher blocks = Minecraft.getInstance().getBlockRenderer();
        PoseStack pose = new PoseStack();

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (filter != null && BlockRecolor.sameBlock(state, filter)) {
                continue;
            }
            BlockState painted = BlockRecolor.paint(state, color).orElse(null);
            if (painted == null) {
                continue;
            }

            long seed = painted.getSeed(pos);
            pose.pushPose();
            pose.translate(pos.getX() - from.getX(), pos.getY() - from.getY(), pos.getZ() - from.getZ());
            ClientServices.CLIENT.renderModel(level, blocks.getBlockModel(painted), painted, pos, pose,
                    type -> this, OverlayTexture.NO_OVERLAY, RandomSource.create(seed), seed, true);
            pose.popPose();
        }
    }

    public void draw(PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (count == 0 || origin == null) {
            return;
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        VertexConsumer out = buffers.getBuffer(PaintRenderTypes.PREVIEW);

        poseStack.pushPose();
        poseStack.translate(origin.getX() - camera.x, origin.getY() - camera.y, origin.getZ() - camera.z);
        var matrix = poseStack.last().pose();
        for (int i = 0; i < count; i++) {
            int p3 = i * 3;
            int p2 = i * 2;
            int packed = rgb[i];
            out.addVertex(matrix, xyz[p3], xyz[p3 + 1], xyz[p3 + 2])
                    .setColor((packed >> 16) & 0xFF, (packed >> 8) & 0xFF, packed & 0xFF, OPAQUE)
                    .setUv(uv[p2], uv[p2 + 1])
                    .setUv2(light[p2], light[p2 + 1])
                    .setNormal(normal[p3], normal[p3 + 1], normal[p3 + 2]);
        }
        poseStack.popPose();

        buffers.endBatch(PaintRenderTypes.PREVIEW);
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        this.lightU = u;
        this.lightV = v;
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float nx, float ny, float nz) {
        grow();
        int p3 = count * 3;
        int p2 = count * 2;
        xyz[p3] = x;
        xyz[p3 + 1] = y;
        xyz[p3 + 2] = z;
        uv[p2] = u;
        uv[p2 + 1] = v;
        rgb[count] = (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        light[p2] = lightU;
        light[p2 + 1] = lightV;
        normal[p3] = nx;
        normal[p3 + 1] = ny;
        normal[p3 + 2] = nz;
        count++;
        return this;
    }

    private void grow() {
        if ((count + 1) * 3 <= xyz.length) {
            return;
        }
        int size = count * 2;
        xyz = Arrays.copyOf(xyz, size * 3);
        uv = Arrays.copyOf(uv, size * 2);
        rgb = Arrays.copyOf(rgb, size);
        light = Arrays.copyOf(light, size * 2);
        normal = Arrays.copyOf(normal, size * 3);
    }
}
