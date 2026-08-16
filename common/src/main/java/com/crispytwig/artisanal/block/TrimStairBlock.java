package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;

public class TrimStairBlock extends ModStairBlock {
    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class, TrimType.SINGLE, TrimType.TOP);

    public TrimStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
        registerDefaultState(defaultBlockState().setValue(TYPE, TrimType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return state;
        }
        BlockState trim = context.getLevel().getBlockState(context.getClickedPos().relative(towardsTrim(state)));
        boolean connected = trim.getBlock() instanceof TrimBlock && trim.getValue(TrimBlock.AXIS) == Direction.Axis.Y;
        return state.setValue(TYPE, connected ? TrimType.TOP : TrimType.SINGLE);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        updateTrim(level, pos, state, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            updateTrim(level, pos, state, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static boolean connectsFrom(BlockState state, Direction.AxisDirection direction) {
        return state.getBlock() instanceof TrimStairBlock
                && state.getValue(HALF) == (direction == Direction.AxisDirection.POSITIVE ? Half.BOTTOM : Half.TOP);
    }

    private static Direction towardsTrim(BlockState state) {
        return state.getValue(HALF) == Half.BOTTOM ? Direction.DOWN : Direction.UP;
    }

    private static void updateTrim(LevelAccessor level, BlockPos pos, BlockState state, boolean connected) {
        if (state.getValue(TYPE) == TrimType.TOP) {
            Direction direction = towardsTrim(state);
            TrimBlock.setConnection(level, pos.relative(direction), direction.getOpposite().getAxisDirection(), connected);
        }
    }
}
