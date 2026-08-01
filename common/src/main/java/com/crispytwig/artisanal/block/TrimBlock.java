package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrimBlock extends RotatedPillarBlock {
    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class);

    public TrimBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(TYPE, TrimType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getClickedFace().getAxis();
        BlockState state = defaultBlockState().setValue(AXIS, axis);
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return state;
        }
        return state.setValue(TYPE, TrimType.of(
                connectsTo(context.getLevel(), context.getClickedPos(), axis, Direction.AxisDirection.NEGATIVE),
                connectsTo(context.getLevel(), context.getClickedPos(), axis, Direction.AxisDirection.POSITIVE)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        updateConnections(level, pos, state, true);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            updateConnections(level, pos, state, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void setConnection(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction, boolean connected) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof TrimBlock && state.getValue(AXIS) == Direction.Axis.Y) {
            level.setBlock(pos, state.setValue(TYPE, state.getValue(TYPE).with(direction, connected)), Block.UPDATE_CLIENTS);
        }
    }

    private static void updateConnections(LevelAccessor level, BlockPos pos, BlockState state, boolean connected) {
        Direction.Axis axis = state.getValue(AXIS);
        for (Direction.AxisDirection direction : Direction.AxisDirection.values()) {
            if (!state.getValue(TYPE).isConnected(direction)) {
                continue;
            }
            BlockPos neighbourPos = pos.relative(Direction.get(direction, axis));
            BlockState neighbour = level.getBlockState(neighbourPos);
            if (neighbour.getBlock() instanceof TrimBlock && neighbour.getValue(AXIS) == axis) {
                level.setBlock(neighbourPos, neighbour.setValue(TYPE,
                        neighbour.getValue(TYPE).with(direction.opposite(), connected)), Block.UPDATE_CLIENTS);
            } else if (axis == Direction.Axis.Y && TrimStairBlock.connectsFrom(neighbour, direction)) {
                level.setBlock(neighbourPos, neighbour.setValue(TrimStairBlock.TYPE,
                        connected ? TrimType.TOP : TrimType.SINGLE), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static boolean connectsTo(LevelAccessor level, BlockPos pos, Direction.Axis axis, Direction.AxisDirection direction) {
        BlockState state = level.getBlockState(pos.relative(Direction.get(direction, axis)));
        if (state.getBlock() instanceof TrimBlock) {
            return state.getValue(AXIS) == axis;
        }
        return axis == Direction.Axis.Y && TrimStairBlock.connectsFrom(state, direction);
    }
}
