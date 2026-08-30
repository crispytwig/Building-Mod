package com.crispytwig.bbb.common.network;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.config.BlockConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BlockConfigPayload(List<String> disabledGroups) implements CustomPacketPayload {
    public static final Type<BlockConfigPayload> TYPE = new Type<>(BuildingButBetter.location("block_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockConfigPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), BlockConfigPayload::disabledGroups,
            BlockConfigPayload::new);

    public static BlockConfigPayload current() {
        return new BlockConfigPayload(BlockConfig.disabledKeys());
    }

    public void send(ServerPlayer player) {
        player.connection.send(new ClientboundCustomPayloadPacket(this));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
