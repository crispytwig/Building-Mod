package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.entity.TableBlockEntity;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import com.crispytwig.artisanal.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Artisanal.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TableBlockEntity>> TABLE =
            BLOCK_ENTITY_TYPES.register("table", () -> Services.PLATFORM.createBlockEntityType(TableBlockEntity::new, ModBlocks.OAK_TABLE.get()));

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
