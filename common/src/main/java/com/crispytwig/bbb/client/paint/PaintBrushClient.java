package com.crispytwig.bbb.client.paint;

import com.crispytwig.bbb.client.renderer.paint.PaintPreview;
import com.crispytwig.bbb.client.renderer.paint.SelectionOutline;
import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.network.PaintSelectionPayload;
import com.crispytwig.bbb.common.registry.ModSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class PaintBrushClient {
    private static final int CANCEL_COLOR = 0xC5B548;

    private static final PaintPreview PREVIEW = new PaintPreview();

    private static @Nullable BlockPos corner;
    private static @Nullable BlockPos hovered;
    private static @Nullable DyeColor color;
    private static boolean cancelling;

    private static @Nullable BlockPos builtHovered;
    private static @Nullable DyeColor builtColor;
    private static @Nullable BlockState builtFilter;

    private PaintBrushClient() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.isPaused() || player == null || minecraft.level == null) {
            return;
        }

        PaintBrushItem.Held held = held(player);
        if (held == null) {
            clear();
            return;
        }

        hovered = lookingAt(minecraft);
        color = PaintBrushItem.getColor(held.brush());
        cancelling = player.isShiftKeyDown();

        if (corner == null || hovered == null || color == null || tooFar(player)) {
            forget();
            return;
        }

        if (changed(hovered, color, held.filter())) {
            builtHovered = hovered;
            builtColor = color;
            builtFilter = held.filter();
            PREVIEW.build(player.level(), corner, hovered, color, held.filter());
        }
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers) {
        if (corner == null || hovered == null || color == null) {
            return;
        }
        SelectionOutline.draw(poseStack, buffers, AABB.encapsulatingFullBlocks(corner, hovered),
                cancelling ? CANCEL_COLOR : color.getFireworkColor());
        PREVIEW.draw(poseStack, buffers);
    }

    public static boolean onRightClick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || held(player) == null) {
            return false;
        }

        if (player.isShiftKeyDown() || hovered == null) {
            if (corner == null) {
                return false;
            }
            clear();
            return true;
        }

        if (corner == null) {
            corner = hovered;
            player.level().playSound(player, corner, ModSounds.BLOCK_PAINTED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }

        if (tooFar(player)) {
            return true;
        }

        Objects.requireNonNull(minecraft.getConnection()).send(
                new ServerboundCustomPayloadPacket(new PaintSelectionPayload(corner, hovered)));
        clear();
        return true;
    }

    public static void clear() {
        corner = null;
        color = null;
        cancelling = false;
        forget();
    }

    private static void forget() {
        builtHovered = null;
        builtColor = null;
        builtFilter = null;
        PREVIEW.clear();
    }

    private static boolean changed(BlockPos hovered, DyeColor color, @Nullable BlockState filter) {
        return !Objects.equals(builtHovered, hovered) || builtColor != color || builtFilter != filter;
    }

    private static PaintBrushItem.@Nullable Held held(LocalPlayer player) {
        return PaintBrushItem.getHeld(player.getMainHandItem(), player.getOffhandItem());
    }

    private static @Nullable BlockPos lookingAt(Minecraft minecraft) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || minecraft.level == null) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        return minecraft.level.getBlockState(pos).isAir() ? null : pos;
    }

    private static boolean tooFar(LocalPlayer player) {
        return corner != null && hovered != null
                && !player.isCreative() && !corner.closerThan(hovered, PaintBrushItem.MAX_RANGE);
    }
}
