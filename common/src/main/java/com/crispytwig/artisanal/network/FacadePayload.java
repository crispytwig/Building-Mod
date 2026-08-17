package com.crispytwig.artisanal.network;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.FacadeEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record FacadePayload(boolean replaceAll, List<FacadeEntry> entries) implements CustomPacketPayload {

    public static final Type<FacadePayload> TYPE = new Type<>(Artisanal.location("facades"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FacadePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FacadePayload::replaceAll,
            FacadeEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), FacadePayload::entries,
            FacadePayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
