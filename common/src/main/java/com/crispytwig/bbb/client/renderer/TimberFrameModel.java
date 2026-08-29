package com.crispytwig.bbb.client.renderer;

import com.crispytwig.bbb.common.block.TimberFrameBlock;
import com.crispytwig.bbb.common.block.entity.TimberFrameBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimberFrameModel implements BakedModel {
    private static final Map<ModelKey, BakedModel> CACHE = new ConcurrentHashMap<>();

    private final BakedModel parent;
    private final Map<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
    private final List<BakedQuad> general = new ArrayList<>();

    private record ModelKey(BakedModel parent, BlockState state, BlockState held) {
    }

    public static BakedModel wrap(BakedModel model, BlockAndTintGetter level, BlockState state, BlockPos pos) {
        if (!(state.getBlock() instanceof TimberFrameBlock) || !state.getValue(TimberFrameBlock.FILLED)) {
            return model;
        }
        if (!(level.getBlockEntity(pos) instanceof TimberFrameBlockEntity frame)) {
            return model;
        }
        BlockState held = frame.getHeldBlock();
        if (held.isAir()) {
            return model;
        }
        return CACHE.computeIfAbsent(new ModelKey(model, state, held),
                key -> new TimberFrameModel(key.parent(), key.state(), key.held()));
    }

    public static void clearCache() {
        CACHE.clear();
    }

    private TimberFrameModel(BakedModel parent, BlockState state, BlockState held) {
        this.parent = parent;
        BakedModel inner = Minecraft.getInstance().getBlockRenderer().getBlockModel(held);
        RandomSource random = RandomSource.create();

        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = new ArrayList<>();
            random.setSeed(0L);
            quads.addAll(inner.getQuads(held, direction, random));
            random.setSeed(0L);
            quads.addAll(parent.getQuads(state, direction, random));
            faces.put(direction, List.copyOf(quads));
        }

        random.setSeed(0L);
        general.addAll(inner.getQuads(held, null, random));
        random.setSeed(0L);
        general.addAll(parent.getQuads(state, null, random));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
        return direction == null ? general : faces.get(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return parent.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return parent.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return parent.isCustomRenderer();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return parent.getParticleIcon();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return parent.getTransforms();
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return parent.getOverrides();
    }
}
