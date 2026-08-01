package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record PanelEntry(BlockPos pos, Direction face, Optional<Block> block) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PanelEntry> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PanelEntry::pos,
            Direction.STREAM_CODEC, PanelEntry::face,
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.BLOCK)), PanelEntry::block,
            PanelEntry::new);
}
