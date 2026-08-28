package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Block>> FACADE_BLOCK =
            DATA_COMPONENT_TYPES.register("facade_block", () -> DataComponentType.<Block>builder()
                    .persistent(BuiltInRegistries.BLOCK.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DyeColor>> PAINT_COLOR =
            DATA_COMPONENT_TYPES.register("paint_color", () -> DataComponentType.<DyeColor>builder()
                    .persistent(DyeColor.CODEC)
                    .networkSynchronized(ByteBufCodecs.idMapper(DyeColor::byId, DyeColor::getId))
                    .build());

    private ModDataComponents() {
    }

    public static void init() {
    }
}
