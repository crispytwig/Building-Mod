package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.entity.FrameBlockEntity;
import com.crispytwig.artisanal.block.entity.TableBlockEntity;
import com.crispytwig.artisanal.block.entity.TimberFrameBlockEntity;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import com.crispytwig.artisanal.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Artisanal.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TableBlockEntity>> TABLE =
            BLOCK_ENTITY_TYPES.register("table", () -> Services.PLATFORM.createBlockEntityType(TableBlockEntity::new, blocks(ModBlocks.WoodSet::table)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FrameBlockEntity>> FRAME =
            BLOCK_ENTITY_TYPES.register("frame", () -> Services.PLATFORM.createBlockEntityType(FrameBlockEntity::new, blocks(ModBlocks.WoodSet::frame)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TimberFrameBlockEntity>> TIMBER_FRAME =
            BLOCK_ENTITY_TYPES.register("timber_frame", () -> Services.PLATFORM.createBlockEntityType(TimberFrameBlockEntity::new, blocks(ModBlocks.WoodSet::timberFrame)));

    private ModBlockEntities() {
    }

    public static void init() {
    }

    private static Block[] blocks(Function<ModBlocks.WoodSet, DeferredHolder<Block, ? extends Block>> part) {
        return ModBlocks.WOOD.stream().map(part).map(DeferredHolder::get).toArray(Block[]::new);
    }
}
