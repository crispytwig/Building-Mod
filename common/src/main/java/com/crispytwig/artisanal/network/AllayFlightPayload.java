package com.crispytwig.artisanal.network;

import com.crispytwig.artisanal.Artisanal;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record AllayFlightPayload(int entityId, int remainingTicks) implements CustomPacketPayload {

    public static final Type<AllayFlightPayload> TYPE = new Type<>(Artisanal.location("allay_flight"));

    public static final StreamCodec<ByteBuf, AllayFlightPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AllayFlightPayload::entityId,
            ByteBufCodecs.VAR_INT, AllayFlightPayload::remainingTicks,
            AllayFlightPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
