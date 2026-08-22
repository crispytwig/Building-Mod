package com.crispytwig.artisanal.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TimberFrameBlock extends Block {
    public static final MapCodec<TimberFrameBlock> CODEC = simpleCodec(TimberFrameBlock::new);

    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class);
    public static final EnumProperty<CrossType> CROSS = EnumProperty.create("cross", CrossType.class);

    public TimberFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, TrimType.SINGLE).setValue(CROSS, CrossType.NONE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, CROSS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return state;
        }
        return state.setValue(TYPE, TrimType.of(
                connectsTo(context.getLevel(), context.getClickedPos().below()),
                connectsTo(context.getLevel(), context.getClickedPos().above())));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        updateConnections(level, pos, state, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            updateConnections(level, pos, state, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    public static InteractionResult tryCycleCross(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TimberFrameBlock) || state.getValue(TYPE) != TrimType.SINGLE) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CrossType cross = state.getValue(CROSS);
        level.setBlock(pos, state.setValue(CROSS, player.isSecondaryUseActive() ? cross.previous() : cross.next()), Block.UPDATE_ALL);
        SoundType soundType = state.getSoundType();
        level.playSound(null, pos, soundType.getStepSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResult.CONSUME;
    }

    private static void updateConnections(LevelAccessor level, BlockPos pos, BlockState state, boolean connected) {
        for (Direction.AxisDirection direction : Direction.AxisDirection.values()) {
            if (!state.getValue(TYPE).isConnected(direction)) {
                continue;
            }
            BlockPos neighbourPos = pos.relative(Direction.get(direction, Direction.Axis.Y));
            BlockState neighbour = level.getBlockState(neighbourPos);
            if (neighbour.getBlock() instanceof TimberFrameBlock) {
                level.setBlock(neighbourPos, neighbour.setValue(TYPE,
                        neighbour.getValue(TYPE).with(direction.opposite(), connected)), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static boolean connectsTo(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof TimberFrameBlock;
    }
}
