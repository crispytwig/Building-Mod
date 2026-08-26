package com.crispytwig.bbb.block.entity;

import com.crispytwig.bbb.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TableBlockEntity extends BlockEntity {
    public TableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TABLE.get(), pos, state);
    }
}
