package com.crispytwig.bbb.client.paint;

import com.crispytwig.bbb.common.paint.BlockRecolor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public final class PaintOverride {
    private record Region(BoundingBox box, DyeColor color, @Nullable BlockState filter) {
    }

    private static volatile @Nullable Region active;

    private PaintOverride() {
    }

    public static void set(BlockPos from, BlockPos to, DyeColor color, @Nullable BlockState filter) {
        Region previous = active;
        Region region = new Region(BoundingBox.fromCorners(from, to), color, filter);
        active = region;
        redraw(previous == null ? null : previous.box());
        redraw(region.box());
    }

    public static void clear() {
        Region previous = active;
        if (previous == null) {
            return;
        }
        active = null;
        redraw(previous.box());
    }

    public static BlockState apply(BlockPos pos, BlockState state) {
        Region region = active;
        if (region == null || state.isAir() || !region.box().isInside(pos)) {
            return state;
        }
        if (region.filter() != null && BlockRecolor.sameBlock(state, region.filter())) {
            return state;
        }
        return BlockRecolor.paint(state, region.color()).orElse(state);
    }

    private static void redraw(@Nullable BoundingBox box) {
        if (box == null) {
            return;
        }
        LevelRenderer renderer = Minecraft.getInstance().levelRenderer;
        renderer.setBlocksDirty(box.minX() - 1, box.minY() - 1, box.minZ() - 1,
                box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
    }
}
