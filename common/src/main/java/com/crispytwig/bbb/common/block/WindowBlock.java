package com.crispytwig.bbb.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class WindowBlock extends TransparentBlock {
    public static final MapCodec<WindowBlock> CODEC = simpleCodec(WindowBlock::new);
    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class);

    public WindowBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, TrimType.SINGLE));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(TYPE, columnType(context.getLevel(), context.getClickedPos(), this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return state.setValue(TYPE, columnType(level, pos, this));
        }
        return state;
    }

    public static TrimType columnType(BlockGetter level, BlockPos pos, Block block) {
        return TrimType.of(level.getBlockState(pos.below()).is(block), level.getBlockState(pos.above()).is(block));
    }
}
