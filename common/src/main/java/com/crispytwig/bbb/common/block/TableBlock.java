package com.crispytwig.bbb.common.block;

import com.crispytwig.bbb.common.block.entity.TableBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TableBlock extends BaseEntityBlock implements DestroyShaped {
    public static final MapCodec<TableBlock> CODEC = simpleCodec(TableBlock::new);

    public static final String TOP_PART = "top";
    public static final String[] LEG_PARTS = {"front_left_leg", "front_right_leg", "back_left_leg", "back_right_leg"};

    private static final VoxelShape DESTROY_SHAPE = Block.box(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
    private static final VoxelShape[] SHAPES = shapes();

    public TableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TableBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[legMask(level, pos)];
    }

    @Override
    public VoxelShape getDestroyShape(BlockState state) {
        return DESTROY_SHAPE;
    }

    public static int legMask(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        boolean north = isTable(level, neighbor.setWithOffset(pos, Direction.NORTH));
        boolean south = isTable(level, neighbor.setWithOffset(pos, Direction.SOUTH));
        boolean east = isTable(level, neighbor.setWithOffset(pos, Direction.EAST));
        boolean west = isTable(level, neighbor.setWithOffset(pos, Direction.WEST));

        int mask = !north && !west ? 1 : 0;
        if (!north && !east) {
            mask |= 2;
        }
        if (!south && !west) {
            mask |= 4;
        }
        if (!south && !east) {
            mask |= 8;
        }
        return mask;
    }

    public static boolean hasLeg(int mask, int leg) {
        return (mask & (1 << leg)) != 0;
    }

    private static boolean isTable(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof TableBlock;
    }

    private static VoxelShape[] shapes() {
        VoxelShape top = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
        VoxelShape leg = Block.box(2.0, 0.0, 2.0, 6.0, 13.0, 6.0);
        VoxelShape[] legs = {
                leg,
                ChairBlock.rotateShape(leg, Direction.EAST),
                ChairBlock.rotateShape(leg, Direction.WEST),
                ChairBlock.rotateShape(leg, Direction.SOUTH)
        };

        VoxelShape[] shapes = new VoxelShape[16];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = top;
            for (int i = 0; i < legs.length; i++) {
                if (hasLeg(mask, i)) {
                    shape = Shapes.or(shape, legs[i]);
                }
            }
            shapes[mask] = shape;
        }
        return shapes;
    }
}
