package com.crispytwig.bbb.entity.ai;

import com.crispytwig.bbb.block.Seat;
import com.crispytwig.bbb.entity.SeatEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.state.BlockState;

public class SitBehavior extends Behavior<Villager> {
    private static final int SEARCH_RANGE = 10;
    private static final int MAX_WALK_TICKS = 10 * 20;
    private static final int MIN_SIT_TICKS = 10 * 20;
    private static final int MAX_SIT_TICKS = 60 * 20;
    private static final int MIN_COOLDOWN_TICKS = 60 * 20;
    private static final int MAX_COOLDOWN_TICKS = 300 * 20;

    private final float speedModifier;
    private BlockPos targetSeat;
    private int walkTimeRemaining;
    private int sitTimeRemaining;
    private boolean sitting;
    private long cooldownUntil;

    public SitBehavior(float speedModifier) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = speedModifier;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        if (villager.isPassenger() || !isRelaxed(villager) || level.getGameTime() < cooldownUntil || level.random.nextInt(20) != 0) {
            return false;
        }

        BlockPos origin = villager.blockPosition();
        BlockPos chosen = null;
        int found = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RANGE, -2, -SEARCH_RANGE), origin.offset(SEARCH_RANGE, 2, SEARCH_RANGE))) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof Seat seat && seat.canSit(state, level, pos)) {
                found++;
                if (level.random.nextInt(found) == 0) {
                    chosen = pos.immutable();
                }
            }
        }
        if (chosen == null || SeatEntity.isOccupied(level, chosen)) {
            cooldownUntil = level.getGameTime() + MIN_COOLDOWN_TICKS;
            return false;
        }
        targetSeat = chosen;
        return true;
    }

    @Override
    protected void start(ServerLevel level, Villager villager, long gameTime) {
        if (targetSeat == null) {
            return;
        }
        walkTimeRemaining = MAX_WALK_TICKS;
        sitTimeRemaining = 0;
        sitting = false;
        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetSeat, speedModifier, 1));
    }

    @Override
    protected void tick(ServerLevel level, Villager villager, long gameTime) {
        if (targetSeat == null) {
            return;
        }
        if (sitting) {
            sitTimeRemaining--;
            return;
        }

        walkTimeRemaining--;
        if (villager.blockPosition().distSqr(targetSeat) >= 4) {
            return;
        }

        BlockState state = level.getBlockState(targetSeat);
        if (state.getBlock() instanceof Seat seat && seat.canSit(state, level, targetSeat)
                && !SeatEntity.isOccupied(level, targetSeat)
                && SeatEntity.sitOnSeat(level, targetSeat, villager, seat)) {
            sitting = true;
            sitTimeRemaining = MIN_SIT_TICKS + level.random.nextInt(MAX_SIT_TICKS - MIN_SIT_TICKS + 1);
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager villager, long gameTime) {
        if (targetSeat == null || !isRelaxed(villager)) {
            return false;
        }
        if (!(level.getBlockState(targetSeat).getBlock() instanceof Seat)) {
            return false;
        }
        return sitting ? sitTimeRemaining > 0 && villager.isPassenger() : walkTimeRemaining > 0;
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel level, Villager villager, long gameTime) {
        if (targetSeat != null && villager.isPassenger()) {
            SeatEntity.eject(level, targetSeat);
        }
        cooldownUntil = level.getGameTime() + MIN_COOLDOWN_TICKS + level.random.nextInt(MAX_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS + 1);
        targetSeat = null;
        sitting = false;
        walkTimeRemaining = 0;
        sitTimeRemaining = 0;
    }

    private static boolean isRelaxed(Villager villager) {
        if (villager.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)
                || villager.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)) {
            return false;
        }
        return villager.getBrain().isActive(Activity.IDLE) || villager.getBrain().isActive(Activity.PLAY);
    }
}
