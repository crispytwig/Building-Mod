package com.crispytwig.artisanal.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LayerBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<LayerBlock> CODEC = simpleCodec(LayerBlock::new);

    public static final int MAX_LAYERS = 4;

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, MAX_LAYERS);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape[]> SHAPES = Arrays.stream(Direction.values())
            .collect(Collectors.toMap(Function.identity(), LayerBlock::shapes));

    public LayerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(LAYERS, 1)
                .setValue(WATERLOGGED, false));
    }

    private static VoxelShape[] shapes(Direction facing) {
        VoxelShape[] shapes = new VoxelShape[MAX_LAYERS];
        for (int layers = 1; layers <= MAX_LAYERS; layers++) {
            double depth = layers * 16.0 / MAX_LAYERS;
            shapes[layers - 1] = switch (facing) {
                case UP -> Block.box(0, 0, 0, 16, depth, 16);
                case DOWN -> Block.box(0, 16 - depth, 0, 16, 16, 16);
                case NORTH -> Block.box(0, 0, 16 - depth, 16, 16, 16);
                case SOUTH -> Block.box(0, 0, 0, 16, 16, depth);
                case WEST -> Block.box(16 - depth, 0, 0, 16, 16, 16);
                case EAST -> Block.box(0, 0, 0, depth, 16, 16);
            };
        }
        return shapes;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LAYERS, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[state.getValue(LAYERS) - 1];
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return state.getValue(LAYERS) < MAX_LAYERS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            int layers = Math.min(MAX_LAYERS, existing.getValue(LAYERS) + 1);
            return existing.setValue(LAYERS, layers).setValue(WATERLOGGED, layers < MAX_LAYERS && existing.getValue(WATERLOGGED));
        }
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(asItem())
                && state.getValue(LAYERS) < MAX_LAYERS
                && context.getClickedFace() == state.getValue(FACING)
                || super.canBeReplaced(state, context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return state.getValue(LAYERS) < MAX_LAYERS && SimpleWaterloggedBlock.super.canPlaceLiquid(player, level, pos, state, fluid);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.getValue(LAYERS) < MAX_LAYERS && SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
    }
}
