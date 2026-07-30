package com.crispytwig.artisanal;

import com.crispytwig.artisanal.client.model.WrightModel;
import com.crispytwig.artisanal.client.renderer.WrightRenderer;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.item.ScepterOccupant;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ArtisanalClient {
    private ArtisanalClient() {
    }

    @FunctionalInterface
    public interface LayerRegistrar {
        void register(ModelLayerLocation location, Supplier<LayerDefinition> definition);
    }

    @FunctionalInterface
    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    @FunctionalInterface
    public interface ItemPropertyRegistrar {
        void register(Item item, ResourceLocation name, ClampedItemPropertyFunction function);
    }

    public static void registerLayerDefinitions(LayerRegistrar registrar) {
        registrar.register(WrightModel.LAYER_LOCATION, WrightModel::createBodyLayer);
    }

    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(ModEntityTypes.WRIGHT.get(), WrightRenderer::new);
    }

    public static void registerItemProperties(ItemPropertyRegistrar registrar) {
        registrar.register(ModItems.ARCHITECTS_SCEPTER.get(), Artisanal.location("captured"),
                (stack, level, entity, seed) -> {
                    ScepterOccupant occupant = ArchitectsScepterItem.getOccupant(stack);
                    return occupant == null ? 0.0F : occupant.getPredicateValue();
                });
    }
}
