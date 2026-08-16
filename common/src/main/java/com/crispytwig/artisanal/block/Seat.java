package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface Seat {
    Direction getSeatFacing(BlockState state);

    default double getSeatYOffset() {
        return 0.1;
    }

    default Vec3 getSeatOffset(BlockGetter level, BlockPos pos, BlockState state) {
        return new Vec3(0.5, getSeatYOffset(), 0.5);
    }

    @SuppressWarnings("unused")
    default boolean canSit(BlockState state, Level level, BlockPos pos) {
        return true;
    }
}
