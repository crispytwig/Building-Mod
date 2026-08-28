package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import com.crispytwig.bbb.common.block.entity.FrameBlockEntity;
import com.crispytwig.bbb.common.block.entity.SofaBlockEntity;
import com.crispytwig.bbb.common.block.entity.TableBlockEntity;
import com.crispytwig.bbb.common.block.entity.TimberFrameBlockEntity;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import com.crispytwig.bbb.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TableBlockEntity>> TABLE =
            BLOCK_ENTITY_TYPES.register("table", () -> Services.PLATFORM.createBlockEntityType(TableBlockEntity::new, blocks(ModBlocks.WoodVariant::table)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrameBlockEntity>> FRAME =
            BLOCK_ENTITY_TYPES.register("frame", () -> Services.PLATFORM.createBlockEntityType(FrameBlockEntity::new, blocks(ModBlocks.WoodVariant::frame)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimberFrameBlockEntity>> TIMBER_FRAME =
            BLOCK_ENTITY_TYPES.register("timber_frame", () -> Services.PLATFORM.createBlockEntityType(TimberFrameBlockEntity::new, blocks(ModBlocks.WoodVariant::timberFrame)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SofaBlockEntity>> SOFA =
            BLOCK_ENTITY_TYPES.register("sofa", () -> Services.PLATFORM.createBlockEntityType(SofaBlockEntity::new,
                    ModBlocks.SOFAS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CurtainBlockEntity>> CURTAIN =
            BLOCK_ENTITY_TYPES.register("curtain", () -> Services.PLATFORM.createBlockEntityType(CurtainBlockEntity::new,
                    ModBlocks.CURTAINS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)));

    private ModBlockEntities() {
    }

    public static void init() {
    }

    private static Block[] blocks(Function<ModBlocks.WoodVariant, DeferredHolder<Block, ? extends Block>> part) {
        return ModBlocks.allWood().stream().map(part).map(DeferredHolder::get).toArray(Block[]::new);
    }
}
