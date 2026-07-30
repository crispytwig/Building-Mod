package com.crispytwig.artisanal.client.renderer;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.client.model.WrightModel;
import com.crispytwig.artisanal.entity.Wright;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class WrightRenderer extends MobRenderer<Wright, WrightModel> {
    private static final ResourceLocation TEXTURE = Artisanal.location("textures/entity/wright.png");

    public WrightRenderer(EntityRendererProvider.Context context) {
        super(context, new WrightModel(context.bakeLayer(WrightModel.LAYER_LOCATION)), 0.3F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Wright entity) {
        return TEXTURE;
    }
}
