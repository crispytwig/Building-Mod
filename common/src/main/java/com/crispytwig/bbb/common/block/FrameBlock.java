package com.crispytwig.bbb.common.block;

import com.crispytwig.bbb.common.block.entity.FrameBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class FrameBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<FrameBlock> CODEC = simpleCodec(FrameBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final String[] PARTS = {
            "top", "bottom",
            "left_middle", "left_top", "left_bottom",
            "right_middle", "right_top", "right_bottom",
            "corner_tl", "corner_bl", "corner_tr", "corner_br",
            "corner_tl_tip", "corner_bl_tip", "corner_tr_tip", "corner_br_tip"
    };

    private static final VoxelShape[] SHAPES = shapes();

    public FrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrameBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clicked = context.getClickedFace();
        Direction facing = clicked.getAxis().isHorizontal() ? clicked : context.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    public static List<String> visibleParts(BlockGetter level, BlockPos pos, Direction facing) {
        Direction left = facing.getClockWise();
        Direction right = facing.getCounterClockWise();
        BlockPos behind = pos.relative(facing.getOpposite());
        BlockPos behindLeft = behind.relative(left);
        BlockPos behindRight = behind.relative(right);

        boolean leftFlat = connects(level, pos, left, facing);
        boolean rightFlat = connects(level, pos, right, facing);
        boolean up = connects(level, pos, Direction.UP, facing);
        boolean down = connects(level, pos, Direction.DOWN, facing);
        boolean upLeft = isFrame(level, pos.above().relative(left), facing);
        boolean upRight = isFrame(level, pos.above().relative(right), facing);
        boolean downLeft = isFrame(level, pos.below().relative(left), facing);
        boolean downRight = isFrame(level, pos.below().relative(right), facing);
        boolean upFrame = isFrame(level, pos.above(), facing);
        boolean downFrame = isFrame(level, pos.below(), facing);

        boolean solidUpLeft = solidFace(level, pos.above().relative(left), Direction.DOWN);
        boolean solidUpRight = solidFace(level, pos.above().relative(right), Direction.DOWN);
        boolean solidDownLeft = solidFace(level, pos.below().relative(left), Direction.UP);
        boolean solidDownRight = solidFace(level, pos.below().relative(right), Direction.UP);

        boolean leftWrap = isFrame(level, behindLeft, left);
        boolean rightWrap = isFrame(level, behindRight, right);
        boolean leftWrapAbove = isFrame(level, behindLeft.above(), left);
        boolean leftWrapBelow = isFrame(level, behindLeft.below(), left);
        boolean rightWrapAbove = isFrame(level, behindRight.above(), right);
        boolean rightWrapBelow = isFrame(level, behindRight.below(), right);

        List<String> parts = new ArrayList<>();
        if (!up) {
            parts.add("top");
        }
        if (!down) {
            parts.add("bottom");
        }
        if (!leftFlat) {
            parts.add("left_middle");
            if (!upLeft && !(leftWrapAbove && !leftWrap && !upFrame)) {
                parts.add("left_top");
            }
            if (!downLeft && !(leftWrapBelow && !leftWrap && !downFrame)) {
                parts.add("left_bottom");
            }
            if (!up && !solidUpLeft) {
                parts.add("corner_tl");
                if (!leftWrap && !leftWrapAbove) {
                    parts.add("corner_tl_tip");
                }
            }
            if (!down && !solidDownLeft) {
                parts.add("corner_bl");
                if (!leftWrap && !leftWrapBelow) {
                    parts.add("corner_bl_tip");
                }
            }
        }
        if (!rightFlat && !rightWrap) {
            parts.add("right_middle");
            if (!upRight && !rightWrapAbove) {
                parts.add("right_top");
            }
            if (!downRight && !rightWrapBelow) {
                parts.add("right_bottom");
            }
            if (!up && !solidUpRight) {
                parts.add("corner_tr");
                if (!rightWrapAbove) {
                    parts.add("corner_tr_tip");
                }
            }
            if (!down && !solidDownRight) {
                parts.add("corner_br");
                if (!rightWrapBelow) {
                    parts.add("corner_br_tip");
                }
            }
        }
        return parts;
    }

    private static boolean solidFace(BlockGetter level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, face);
    }

    private static boolean isFrame(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof FrameBlock && state.getValue(FACING) == facing;
    }

    private static boolean connects(BlockGetter level, BlockPos pos, Direction toNeighbor, Direction facing) {
        BlockPos neighborPos = pos.relative(toNeighbor);
        BlockState neighbor = level.getBlockState(neighborPos);
        if (neighbor.getBlock() instanceof FrameBlock && neighbor.getValue(FACING) == facing) {
            return true;
        }
        return neighbor.isFaceSturdy(level, neighborPos, toNeighbor.getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    private static VoxelShape[] shapes() {
        VoxelShape[] shapes = new VoxelShape[4];
        shapes[Direction.NORTH.get2DDataValue()] = Block.box(0, 0, 12, 16, 16, 16);
        shapes[Direction.SOUTH.get2DDataValue()] = Block.box(0, 0, 0, 16, 16, 4);
        shapes[Direction.WEST.get2DDataValue()] = Block.box(12, 0, 0, 16, 16, 16);
        shapes[Direction.EAST.get2DDataValue()] = Block.box(0, 0, 0, 4, 16, 16);
        return shapes;
    }
}
