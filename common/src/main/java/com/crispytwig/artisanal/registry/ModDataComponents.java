package com.crispytwig.artisanal.registry;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.item.ScepterOccupant;
import com.crispytwig.artisanal.platform.registry.DeferredHolder;
import com.crispytwig.artisanal.platform.registry.DeferredRegister;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Artisanal.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ScepterOccupant>> CAPTURED_ENTITY =
            DATA_COMPONENT_TYPES.register("captured_entity", () -> DataComponentType.<ScepterOccupant>builder()
                    .persistent(ScepterOccupant.CODEC)
                    .networkSynchronized(ScepterOccupant.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> SELECTION_ANCHOR =
            DATA_COMPONENT_TYPES.register("selection_anchor", () -> DataComponentType.<GlobalPos>builder()
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> BUILDER =
            DATA_COMPONENT_TYPES.register("builder", () -> DataComponentType.<UUID>builder()
                    .persistent(UUIDUtil.CODEC)
                    .networkSynchronized(UUIDUtil.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Block>> PANEL_BLOCK =
            DATA_COMPONENT_TYPES.register("panel_block", () -> DataComponentType.<Block>builder()
                    .persistent(BuiltInRegistries.BLOCK.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
                    .build());

    private ModDataComponents() {
    }

    public static void init() {
    }
}
