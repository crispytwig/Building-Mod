package com.crispytwig.bbb.block.entity;

import com.crispytwig.bbb.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRAME.get(), pos, state);
    }
}
