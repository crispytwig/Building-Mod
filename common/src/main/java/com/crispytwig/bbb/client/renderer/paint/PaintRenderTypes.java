package com.crispytwig.bbb.client.renderer.paint;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.List;

public final class PaintRenderTypes extends RenderType {
    public static final RenderType PREVIEW = of(
            "bbb_paint_preview",
            DefaultVertexFormat.BLOCK,
            256 * 256,
            List.of(BLOCK_SHEET_MIPPED, RENDERTYPE_CUTOUT_MIPPED_SHADER, NO_TRANSPARENCY,
                    LEQUAL_DEPTH_TEST, CULL, LIGHTMAP, POLYGON_OFFSET_LAYERING,
                    COLOR_DEPTH_WRITE));

    public static final RenderType OUTLINE = of(
            "bbb_paint_outline",
            DefaultVertexFormat.POSITION_COLOR,
            2048,
            List.of(POSITION_COLOR_SHADER, TRANSLUCENT_TRANSPARENCY, NO_DEPTH_TEST, NO_CULL,
                    COLOR_DEPTH_WRITE));

    private static RenderType of(String name, VertexFormat format, int bufferSize, List<RenderStateShard> shards) {
        return new PaintRenderTypes(name, format, VertexFormat.Mode.QUADS, bufferSize, false, shards);
    }

    private PaintRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean sortOnUpload, List<RenderStateShard> shards) {
        super(name, format, mode, bufferSize, false, sortOnUpload,
                () -> shards.forEach(RenderStateShard::setupRenderState),
                () -> {
                    for (int i = shards.size() - 1; i >= 0; i--) {
                        shards.get(i).clearRenderState();
                    }
                });
    }
}
