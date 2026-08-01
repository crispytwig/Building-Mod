package com.crispytwig.artisanal.network;

import com.crispytwig.artisanal.Artisanal;
import com.crispytwig.artisanal.block.PanelEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record PanelPayload(boolean replaceAll, List<PanelEntry> entries) implements CustomPacketPayload {

    public static final Type<PanelPayload> TYPE = new Type<>(Artisanal.location("panels"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PanelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PanelPayload::replaceAll,
            PanelEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), PanelPayload::entries,
            PanelPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
