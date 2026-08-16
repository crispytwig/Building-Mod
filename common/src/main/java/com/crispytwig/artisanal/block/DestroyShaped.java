package com.crispytwig.artisanal.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface DestroyShaped {
    VoxelShape getDestroyShape(BlockState state);
}
