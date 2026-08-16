package com.crispytwig.artisanal.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WindowPaneBlock extends TransparentBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<WindowPaneBlock> CODEC = simpleCodec(WindowPaneBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class);

    private static final VoxelShape[] SHAPES = shapes();

    public WindowPaneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(WATERLOGGED, false)
                .setValue(TYPE, TrimType.SINGLE));
    }

    @Override
    protected MapCodec<? extends TransparentBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, WATERLOGGED, TYPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[sideMask(state)];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(NORTH, attachesTo(level, pos, Direction.NORTH))
                .setValue(SOUTH, attachesTo(level, pos, Direction.SOUTH))
                .setValue(EAST, attachesTo(level, pos, Direction.EAST))
                .setValue(WEST, attachesTo(level, pos, Direction.WEST))
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER)
                .setValue(TYPE, WindowBlock.columnType(level, pos, this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), attachesTo(level, pos, direction));
        }
        return state.setValue(TYPE, WindowBlock.columnType(level, pos, this));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private boolean attachesTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighbor = level.getBlockState(neighborPos);
        return neighbor.getBlock() instanceof WindowPaneBlock
                || neighbor.getBlock() instanceof IronBarsBlock
                || neighbor.is(BlockTags.WALLS)
                || neighbor.isFaceSturdy(level, neighborPos, direction.getOpposite());
    }

    private static int sideMask(BlockState state) {
        int mask = state.getValue(NORTH) ? 1 : 0;
        if (state.getValue(EAST)) {
            mask |= 2;
        }
        if (state.getValue(SOUTH)) {
            mask |= 4;
        }
        if (state.getValue(WEST)) {
            mask |= 8;
        }
        return mask;
    }

    private static VoxelShape[] shapes() {
        VoxelShape post = Block.box(7, 0, 7, 9, 16, 9);
        VoxelShape north = Block.box(7, 0, 0, 9, 16, 9);
        VoxelShape east = Block.box(7, 0, 7, 16, 16, 9);
        VoxelShape south = Block.box(7, 0, 7, 9, 16, 16);
        VoxelShape west = Block.box(0, 0, 7, 9, 16, 9);

        VoxelShape[] shapes = new VoxelShape[16];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = post;
            if ((mask & 1) != 0) {
                shape = Shapes.or(shape, north);
            }
            if ((mask & 2) != 0) {
                shape = Shapes.or(shape, east);
            }
            if ((mask & 4) != 0) {
                shape = Shapes.or(shape, south);
            }
            if ((mask & 8) != 0) {
                shape = Shapes.or(shape, west);
            }
            shapes[mask] = shape;
        }
        return shapes;
    }
}
