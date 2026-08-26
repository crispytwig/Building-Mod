package com.crispytwig.bbb.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record FacadeEntry(BlockPos pos, Direction face, Optional<Block> block) {
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeEntry> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, FacadeEntry::pos,
            Direction.STREAM_CODEC, FacadeEntry::face,
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.BLOCK)), FacadeEntry::block,
            FacadeEntry::new);
}
