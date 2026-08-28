package com.crispytwig.bbb.client.renderer.paint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class SelectionOutline {
    private static final int TUBE_SEGMENTS = 20;
    private static final int TUBE_SIDES = 8;
    private static final int SPHERE_STACKS = 10;
    private static final int SPHERE_SLICES = 10;

    private static final float THICKNESS = 7.0F / 100.0F;
    private static final float BLOCKS_PER_WOBBLE = 2.0F;
    private static final float WOBBLE_SPEED = 3.0F;

    private static final int[][] EDGES = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
    };

    private static final double[] TUBE_COS = new double[TUBE_SIDES];
    private static final double[] TUBE_SIN = new double[TUBE_SIDES];
    private static final double[] LON_COS = new double[SPHERE_SLICES + 1];
    private static final double[] LON_SIN = new double[SPHERE_SLICES + 1];
    private static final double[] LAT_COS = new double[SPHERE_STACKS + 1];
    private static final double[] LAT_SIN = new double[SPHERE_STACKS + 1];

    static {
        for (int j = 0; j < TUBE_SIDES; j++) {
            double angle = j * 2 * Math.PI / TUBE_SIDES;
            TUBE_COS[j] = Math.cos(angle);
            TUBE_SIN[j] = Math.sin(angle);
        }
        for (int j = 0; j <= SPHERE_SLICES; j++) {
            double longitude = j * 2 * Math.PI / SPHERE_SLICES;
            LON_COS[j] = Math.cos(longitude);
            LON_SIN[j] = Math.sin(longitude);
        }
        for (int i = 0; i <= SPHERE_STACKS; i++) {
            double latitude = Math.PI / 2 - i * Math.PI / SPHERE_STACKS;
            LAT_COS[i] = Math.cos(latitude);
            LAT_SIN[i] = Math.sin(latitude);
        }
    }

    private static final double[] CORNERS = new double[8 * 3];
    private static final float[] WOBBLE = new float[8];
    private static final double[] RING_A = new double[TUBE_SIDES * 3];
    private static final double[] RING_B = new double[TUBE_SIDES * 3];
    private static final double[] ROW_A = new double[(SPHERE_SLICES + 1) * 3];
    private static final double[] ROW_B = new double[(SPHERE_SLICES + 1) * 3];

    private SelectionOutline() {
    }

    public static void draw(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, AABB box, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        Vector3f look = minecraft.gameRenderer.getMainCamera().getLookVector();
        double lookX = look.x();
        double lookY = look.y();
        double lookZ = look.z();

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        setCorner(0, box.minX, box.minY, box.minZ);
        setCorner(1, box.maxX, box.minY, box.minZ);
        setCorner(2, box.maxX, box.minY, box.maxZ);
        setCorner(3, box.minX, box.minY, box.maxZ);
        setCorner(4, box.minX, box.maxY, box.minZ);
        setCorner(5, box.maxX, box.maxY, box.minZ);
        setCorner(6, box.maxX, box.maxY, box.maxZ);
        setCorner(7, box.minX, box.maxY, box.maxZ);

        float time = (System.currentTimeMillis() % 100000L) / 1000.0F;
        for (int i = 0; i < WOBBLE.length; i++) {
            WOBBLE[i] = Mth.sin(time * WOBBLE_SPEED + i * 4.0F);
        }

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(PaintRenderTypes.OUTLINE);

        for (int i = 0; i < 8; i++) {
            blob(matrix, consumer, i, WOBBLE[i], r, g, b);
        }
        for (int[] edge : EDGES) {
            tube(matrix, consumer, edge[0], edge[1], WOBBLE[edge[0]], WOBBLE[edge[1]], time,
                    lookX, lookY, lookZ, r, g, b);
        }

        poseStack.popPose();
        bufferSource.endBatch(PaintRenderTypes.OUTLINE);
    }

    private static void setCorner(int index, double x, double y, double z) {
        int p = index * 3;
        CORNERS[p] = x;
        CORNERS[p + 1] = y;
        CORNERS[p + 2] = z;
    }

    private static void tube(Matrix4f matrix, VertexConsumer consumer, int startCorner, int endCorner,
                             float startWobble, float endWobble, float time,
                             double lookX, double lookY, double lookZ, float r, float g, float b) {
        int s = startCorner * 3;
        int e = endCorner * 3;
        double startX = CORNERS[s];
        double startY = CORNERS[s + 1];
        double startZ = CORNERS[s + 2];
        double dx = CORNERS[e] - startX;
        double dy = CORNERS[e + 1] - startY;
        double dz = CORNERS[e + 2] - startZ;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-7D) {
            return;
        }
        double dirX = dx / length;
        double dirY = dy / length;
        double dirZ = dz / length;

        double ax = dirY * lookZ - dirZ * lookY;
        double ay = dirZ * lookX - dirX * lookZ;
        double az = dirX * lookY - dirY * lookX;
        double axisLength = Math.sqrt(ax * ax + ay * ay + az * az);
        if (axisLength < 1.0E-6D) {
            ax = dirY * dirX - dirZ * dirZ;
            ay = dirZ * dirY - dirX * dirX;
            az = dirX * dirZ - dirY * dirY;
            axisLength = Math.sqrt(ax * ax + ay * ay + az * az);
            if (axisLength < 1.0E-6D) {
                return;
            }
        }
        ax /= axisLength;
        ay /= axisLength;
        az /= axisLength;

        double bx = dirY * az - dirZ * ay;
        double by = dirZ * ax - dirX * az;
        double bz = dirX * ay - dirY * ax;
        double bLength = Math.sqrt(bx * bx + by * by + bz * bz);
        bx /= bLength;
        by /= bLength;
        bz /= bLength;

        double frequency = (Mth.PI * 2) / BLOCKS_PER_WOBBLE;
        double[] previous = RING_A;
        double[] current = RING_B;

        for (int i = 0; i <= TUBE_SEGMENTS; i++) {
            float t = (float) i / TUBE_SEGMENTS;
            double centreX = startX + dx * t;
            double centreY = startY + dy * t;
            double centreZ = startZ + dz * t;

            float ends = Mth.lerp(t, startWobble, endWobble);
            float middle = Mth.sin((float) (time * WOBBLE_SPEED + t * length * frequency));
            float blended = Mth.lerp(Mth.sin(t * Mth.PI), ends, middle);
            double thickness = THICKNESS + blended * THICKNESS * 0.6F;

            for (int j = 0; j < TUBE_SIDES; j++) {
                double offA = TUBE_COS[j] * thickness;
                double offB = TUBE_SIN[j] * thickness;
                int p = j * 3;
                current[p] = centreX + ax * offA + bx * offB;
                current[p + 1] = centreY + ay * offA + by * offB;
                current[p + 2] = centreZ + az * offA + bz * offB;
            }

            if (i > 0) {
                for (int j = 0; j < TUBE_SIDES; j++) {
                    int p = j * 3;
                    int q = ((j + 1) % TUBE_SIDES) * 3;
                    vertex(matrix, consumer, previous, p, r, g, b);
                    vertex(matrix, consumer, current, p, r, g, b);
                    vertex(matrix, consumer, current, q, r, g, b);
                    vertex(matrix, consumer, previous, q, r, g, b);
                }
            }

            double[] swap = previous;
            previous = current;
            current = swap;
        }
    }

    private static void blob(Matrix4f matrix, VertexConsumer consumer, int corner, float wobble,
                             float r, float g, float b) {
        int c = corner * 3;
        double centreX = CORNERS[c];
        double centreY = CORNERS[c + 1];
        double centreZ = CORNERS[c + 2];
        double radius = THICKNESS + wobble * THICKNESS * 0.5F;

        double[] previous = ROW_A;
        double[] current = ROW_B;

        for (int i = 0; i <= SPHERE_STACKS; i++) {
            double cos = LAT_COS[i];
            double sin = LAT_SIN[i];
            for (int j = 0; j <= SPHERE_SLICES; j++) {
                int p = j * 3;
                current[p] = centreX + LON_COS[j] * cos * radius;
                current[p + 1] = centreY + sin * radius;
                current[p + 2] = centreZ + LON_SIN[j] * cos * radius;
            }

            if (i > 0) {
                for (int j = 0; j < SPHERE_SLICES; j++) {
                    int p = j * 3;
                    int q = (j + 1) * 3;
                    vertex(matrix, consumer, previous, p, r, g, b);
                    vertex(matrix, consumer, previous, q, r, g, b);
                    vertex(matrix, consumer, current, q, r, g, b);
                    vertex(matrix, consumer, current, p, r, g, b);
                }
            }

            double[] swap = previous;
            previous = current;
            current = swap;
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, double[] points, int offset,
                               float r, float g, float b) {
        consumer.addVertex(matrix, (float) points[offset], (float) points[offset + 1], (float) points[offset + 2])
                .setColor(r, g, b, 1.0F);
    }
}
