package com.crispytwig.bbb.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeamBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<BeamBlock> CODEC = simpleCodec(BeamBlock::new);

    public static final BooleanProperty X = BooleanProperty.create("x");
    public static final BooleanProperty Y = BooleanProperty.create("y");
    public static final BooleanProperty Z = BooleanProperty.create("z");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape X_SHAPE = Block.box(0, 4, 4, 16, 12, 12);
    private static final VoxelShape Y_SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape Z_SHAPE = Block.box(4, 4, 0, 12, 12, 16);

    public BeamBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(X, false)
                .setValue(Y, false)
                .setValue(Z, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(X, Y, Z, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        if (state.getValue(X)) {
            shape = Shapes.or(shape, X_SHAPE);
        }
        if (state.getValue(Y)) {
            shape = Shapes.or(shape, Y_SHAPE);
        }
        if (state.getValue(Z)) {
            shape = Shapes.or(shape, Z_SHAPE);
        }
        return shape.isEmpty() ? Shapes.block() : shape;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        BooleanProperty axis = axisProperty(context.getClickedFace());
        if (existing.is(this)) {
            return existing.getValue(axis) ? null : existing.setValue(axis, true);
        }
        return defaultBlockState()
                .setValue(axis, true)
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(asItem())) {
            return !state.getValue(axisProperty(context.getClickedFace()));
        }
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private static BooleanProperty axisProperty(Direction face) {
        return switch (face.getAxis()) {
            case X -> X;
            case Y -> Y;
            case Z -> Z;
        };
    }
}
