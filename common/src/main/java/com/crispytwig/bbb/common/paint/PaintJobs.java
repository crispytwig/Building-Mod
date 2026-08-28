package com.crispytwig.bbb.common.paint;

import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class PaintJobs {
    private static final List<PaintJob> ACTIVE = new ArrayList<>();

    private PaintJobs() {
    }

    public static void start(ServerPlayer player, BlockPos from, BlockPos to) {
        PaintBrushItem.Held held = PaintBrushItem.getHeld(player.getMainHandItem(), player.getOffhandItem());
        if (held == null) {
            return;
        }

        DyeColor color = PaintBrushItem.getColor(held.brush());
        if (color == null) {
            return;
        }

        if (!player.isCreative() && !from.closerThan(to, PaintBrushItem.MAX_RANGE)) {
            return;
        }

        ACTIVE.add(new PaintJob(player.serverLevel(), from, to, color, held.filter()));
    }

    public static void tick() {
        if (!ACTIVE.isEmpty()) {
            ACTIVE.removeIf(PaintJob::tick);
        }
    }

    public static void clear(ServerLevel level) {
        ACTIVE.removeIf(job -> job.level == level);
    }

    private static final class PaintJob {
        private final ServerLevel level;
        private final DyeColor color;
        private final @Nullable BlockState filter;
        private final List<List<BlockPos>> layers;
        private final BlockPos soundPos;
        private int next;
        private boolean announced;

        private PaintJob(ServerLevel level, BlockPos from, BlockPos to, DyeColor color, @Nullable BlockState filter) {
            this.level = level;
            this.color = color;
            this.filter = filter;
            this.soundPos = to.immutable();
            this.layers = groupByDistance(from, to);
            paintNext();
        }

        private static List<List<BlockPos>> groupByDistance(BlockPos from, BlockPos to) {
            Map<Integer, List<BlockPos>> byDistance = new TreeMap<>();
            for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
                int distance = Math.abs(from.getX() - pos.getX())
                        + Math.abs(from.getY() - pos.getY())
                        + Math.abs(from.getZ() - pos.getZ());
                byDistance.computeIfAbsent(distance, k -> new ArrayList<>()).add(pos.immutable());
            }
            return new ArrayList<>(byDistance.values());
        }

        private boolean tick() {
            paintNext();
            return next >= layers.size();
        }

        private void paintNext() {
            if (next >= layers.size()) {
                return;
            }
            for (BlockPos pos : layers.get(next)) {
                paintBlock(pos);
            }
            next++;
        }

        private void playSound() {
            if (announced) {
                return;
            }
            announced = true;
            level.playSound(null, soundPos, ModSounds.BLOCK_PAINTED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        private void paintBlock(BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                return;
            }
            if (filter != null && BlockRecolor.sameBlock(state, filter)) {
                return;
            }
            BlockRecolor.paint(state, color)
                    .filter(painted -> painted != state)
                    .ifPresent(painted -> {
                        BlockEntity old = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                        CompoundTag kept = old instanceof KeepsDataWhenPainted
                                ? old.saveWithoutMetadata(level.registryAccess()) : null;
                        BlockEntityType<?> keptType = old == null ? null : old.getType();
                        level.setBlock(pos, painted, Block.UPDATE_ALL);
                        if (kept != null) {
                            restore(pos, painted, kept, keptType);
                        }
                        playSound();
                    });
        }

        private void restore(BlockPos pos, BlockState painted, CompoundTag kept, BlockEntityType<?> keptType) {
            BlockEntity fresh = level.getBlockEntity(pos);
            if (fresh == null || fresh.getType() != keptType) {
                return;
            }
            fresh.loadWithComponents(kept, level.registryAccess());
            fresh.setChanged();
            level.sendBlockUpdated(pos, painted, painted, Block.UPDATE_CLIENTS);
        }
    }
}
