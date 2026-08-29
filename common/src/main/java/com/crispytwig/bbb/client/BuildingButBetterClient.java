package com.crispytwig.bbb.client;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.ChairBlock;
import com.crispytwig.bbb.common.block.CurtainBlock;
import com.crispytwig.bbb.common.block.FrameBlock;
import com.crispytwig.bbb.common.block.SofaBlock;
import com.crispytwig.bbb.common.block.TableBlock;
import com.crispytwig.bbb.common.block.TimberFrameBlock;
import com.crispytwig.bbb.common.block.WindowBlock;
import com.crispytwig.bbb.common.block.WindowPaneBlock;
import com.crispytwig.bbb.client.renderer.CurtainBlockRenderer;
import com.crispytwig.bbb.client.renderer.FrameBlockRenderer;
import com.crispytwig.bbb.client.renderer.SeatEntityRenderer;
import com.crispytwig.bbb.client.renderer.SofaBlockRenderer;
import com.crispytwig.bbb.client.renderer.TableBlockRenderer;
import com.crispytwig.bbb.client.screen.SofaScreen;
import com.crispytwig.bbb.common.registry.ModMenuTypes;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.common.registry.ModBlockEntities;
import com.crispytwig.bbb.common.item.PaintBrushItem;
import com.crispytwig.bbb.common.registry.ModBlocks;
import com.crispytwig.bbb.common.registry.ModEntityTypes;
import com.crispytwig.bbb.common.registry.ModItems;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BuildingButBetterClient {
    private BuildingButBetterClient() {
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
    public interface ScreenFactory<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> {
        S create(M menu, Inventory playerInventory, Component title);
    }

    @FunctionalInterface
    public interface ScreenRegistrar {
        <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ScreenFactory<M, S> factory);
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
        registrar.register(ModBlockEntities.FRAME.get(), context -> new FrameBlockRenderer());
        registrar.register(ModBlockEntities.SOFA.get(), context -> new SofaBlockRenderer());
        registrar.register(ModBlockEntities.CURTAIN.get(), context -> ClientServices.CLIENT.curtainRenderer());
    }

    public static void registerScreens(ScreenRegistrar registrar) {
        registrar.register(ModMenuTypes.SOFA.get(), SofaScreen::new);
    }

    public static void registerRenderTypes(RenderTypeRegistrar registrar) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            if (block instanceof ChairBlock || block instanceof TimberFrameBlock || block instanceof CurtainBlock) {
                registrar.register(RenderType.cutout(), block);
            } else if (block instanceof WindowBlock || block instanceof WindowPaneBlock) {
                registrar.register(RenderType.translucent(), block);
            }
        }
    }

    public static void registerExtraModels(Consumer<ResourceLocation> registrar) {
        for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
            String name = holder.getId().getPath();
            if (holder.get() instanceof TableBlock) {
                TableBlockRenderer.cachePartModels(holder.get(), name);
                registrar.accept(TableBlockRenderer.partLocation(name, TableBlock.TOP_PART));
                for (String part : TableBlock.LEG_PARTS) {
                    registrar.accept(TableBlockRenderer.partLocation(name, part));
                }
            } else if (holder.get() instanceof SofaBlock) {
                SofaBlockRenderer.cachePartModels(holder.get(), name);
                for (String part : SofaBlock.PARTS) {
                    registrar.accept(SofaBlockRenderer.partLocation(name, part));
                }
            } else if (holder.get() instanceof FrameBlock) {
                FrameBlockRenderer.cachePartModels(holder.get(), name);
                for (String part : FrameBlock.PARTS) {
                    registrar.accept(FrameBlockRenderer.partLocation(name, part));
                }
            } else if (holder.get() instanceof CurtainBlock) {
                CurtainBlockRenderer.cachePartModels(holder.get(), name);
                for (String part : CurtainBlock.PARTS) {
                    registrar.accept(CurtainBlockRenderer.partLocation(name, part));
                }
            }
        }
    }

    public static void registerItemProperties(ItemPropertyRegistrar registrar) {
        registrar.register(ModItems.PAINT_BRUSH.get(), BuildingButBetter.location("color"), (stack, level, entity, seed) -> {
            DyeColor color = PaintBrushItem.getColor(stack);
            return color == null ? 0.0F : (color.getId() + 1) / 17.0F;
        });
    }
}
