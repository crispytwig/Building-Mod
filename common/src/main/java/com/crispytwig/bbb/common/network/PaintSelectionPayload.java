package com.crispytwig.bbb.common.network;

import com.crispytwig.bbb.common.BuildingButBetter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record PaintSelectionPayload(BlockPos from, BlockPos to) implements CustomPacketPayload {
    public static final Type<PaintSelectionPayload> TYPE = new Type<>(BuildingButBetter.location("paint_selection"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaintSelectionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PaintSelectionPayload::from,
            BlockPos.STREAM_CODEC, PaintSelectionPayload::to,
            PaintSelectionPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
