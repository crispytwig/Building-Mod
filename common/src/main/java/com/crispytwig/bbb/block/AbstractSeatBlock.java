package com.crispytwig.bbb.block;

import com.crispytwig.bbb.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractSeatBlock extends Block implements Seat {
    public AbstractSeatBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return SeatEntity.trySit(this, state, level, pos, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            SeatEntity.eject(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
