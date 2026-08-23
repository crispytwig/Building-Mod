package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface Contents {
    BlockState getContents(BlockGetter level, BlockPos pos, BlockState state);

    static BlockState of(@Nullable BlockGetter level, BlockPos pos, BlockState state) {
        if (level != null && state.getBlock() instanceof Contents contents) {
            return contents.getContents(level, pos, state);
        }
        return Blocks.AIR.defaultBlockState();
    }

    static SoundType soundType(@Nullable BlockGetter level, BlockPos pos, BlockState state) {
        BlockState contents = of(level, pos, state);
        return contents.isAir() ? state.getSoundType() : contents.getSoundType();
    }
}
