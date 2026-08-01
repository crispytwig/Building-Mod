package com.crispytwig.artisanal.client.renderer;

import com.crispytwig.artisanal.client.PanelClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PanelModel implements BakedModel {
    private static final Map<ModelKey, BakedModel> CACHE = new ConcurrentHashMap<>();

    private final BakedModel parent;
    private final Map<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
    private final List<BakedQuad> general = new ArrayList<>();

    private record ModelKey(BakedModel parent, BlockState state, Map<Direction, Block> panels) {
    }

    public static BakedModel wrap(BakedModel model, BlockState state, BlockPos pos) {
        if (PanelClient.isEmpty()) {
            return model;
        }
        Map<Direction, Block> panels = PanelClient.panels(pos);
        if (panels == null) {
            return model;
        }
        return CACHE.computeIfAbsent(new ModelKey(model, state, panels), key -> new PanelModel(key.parent(), key.state(), key.panels()));
    }

    private PanelModel(BakedModel parent, BlockState state, Map<Direction, Block> panels) {
        this.parent = parent;
        RandomSource random = RandomSource.create();
        panels.forEach((face, block) -> {
            random.setSeed(0L);
            TextureAtlasSprite sprite = sprite(block, face);
            faces.put(face, parent.getQuads(state, face, random).stream().map(quad -> retexture(quad, sprite)).toList());
        });
        random.setSeed(0L);
        for (BakedQuad quad : parent.getQuads(state, null, random)) {
            Block block = panels.get(quad.getDirection());
            general.add(block == null ? quad : retexture(quad, sprite(block, quad.getDirection())));
        }
    }

    private static TextureAtlasSprite sprite(Block block, Direction face) {
        BlockState state = block.defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        RandomSource random = RandomSource.create();
        random.setSeed(0L);
        List<BakedQuad> quads = model.getQuads(state, face, random);
        if (quads.isEmpty()) {
            random.setSeed(0L);
            quads = model.getQuads(state, null, random);
        }
        return quads.isEmpty() ? model.getParticleIcon() : quads.getFirst().getSprite();
    }

    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite sprite) {
        TextureAtlasSprite source = quad.getSprite();
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        for (int i = 0; i < 4; i++) {
            int index = i * stride + 4;
            float u = (Float.intBitsToFloat(vertices[index]) - source.getU0()) / (source.getU1() - source.getU0());
            float v = (Float.intBitsToFloat(vertices[index + 1]) - source.getV0()) / (source.getV1() - source.getV0());
            vertices[index] = Float.floatToRawIntBits(Mth.lerp(u, sprite.getU0(), sprite.getU1()));
            vertices[index + 1] = Float.floatToRawIntBits(Mth.lerp(v, sprite.getV0(), sprite.getV1()));
        }
        return new BakedQuad(vertices, -1, quad.getDirection(), sprite, quad.isShade());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource random) {
        if (direction == null) {
            return general;
        }
        List<BakedQuad> panel = faces.get(direction);
        return panel != null ? panel : parent.getQuads(state, direction, random);
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
