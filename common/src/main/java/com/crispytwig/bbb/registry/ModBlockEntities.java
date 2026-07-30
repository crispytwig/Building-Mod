package com.crispytwig.bbb.registry;

import com.crispytwig.bbb.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BuildingButBetter.MOD_ID);

    private ModBlockEntities() {
    }

    public static void init() {
    }
}
