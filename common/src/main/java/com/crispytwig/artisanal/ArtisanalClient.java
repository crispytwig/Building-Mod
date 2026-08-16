package com.crispytwig.artisanal;

import com.crispytwig.artisanal.block.ChairBlock;
import com.crispytwig.artisanal.block.TableBlock;
import com.crispytwig.artisanal.block.WindowBlock;
import com.crispytwig.artisanal.block.WindowPaneBlock;
import com.crispytwig.artisanal.client.renderer.SeatEntityRenderer;
import com.crispytwig.artisanal.client.renderer.TableBlockRenderer;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.registry.ModBlockEntities;
import com.crispytwig.artisanal.registry.ModBlocks;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;
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
    public interface BlockEntityRendererRegistrar {
        <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider);
    }

    @FunctionalInterface
    public interface ItemPropertyRegistrar {
        void register(Item item, ResourceLocation name, ClampedItemPropertyFunction function);
    }

    @FunctionalInterface
    public interface RenderTypeRegistrar {
        void register(RenderType type, Block block);
    }

    public static void registerLayerDefinitions(LayerRegistrar registrar) {
    }

    public static void registerRenderers(RendererRegistrar registrar) {
        registrar.register(ModEntityTypes.SEAT.get(), SeatEntityRenderer::new);
    }

    public static void registerBlockEntityRenderers(BlockEntityRendererRegistrar registrar) {
        registrar.register(ModBlockEntities.TABLE.get(), context -> new TableBlockRenderer());
    }

    public static void registerRenderTypes(RenderTypeRegistrar registrar) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (block instanceof ChairBlock) {
                registrar.register(RenderType.cutout(), block);
            } else if (block instanceof WindowBlock || block instanceof WindowPaneBlock) {
                registrar.register(RenderType.translucent(), block);
            }
        }
    }

    public static void registerExtraModels(Consumer<ResourceLocation> registrar) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            if (!(holder.get() instanceof TableBlock)) {
                continue;
            }
            String tableName = holder.getId().getPath();
            TableBlockRenderer.cachePartModels(holder.get(), tableName);
            registrar.accept(TableBlockRenderer.partLocation(tableName, TableBlock.TOP_PART));
            for (String part : TableBlock.LEG_PARTS) {
                registrar.accept(TableBlockRenderer.partLocation(tableName, part));
            }
        }
    }

    public static void registerItemProperties(ItemPropertyRegistrar registrar) {
    }
}
