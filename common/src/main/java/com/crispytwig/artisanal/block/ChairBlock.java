package com.crispytwig.artisanal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChairBlock extends AbstractSeatBlock implements DestroyShaped {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty TUCKED = BooleanProperty.create("tucked");

    private static final double TUCK_OFFSET = 5.0 / 16.0;

    private static final VoxelShape DESTROY_SHAPE = Block.box(2.0, 2.0, 2.0, 14.0, 10.0, 14.0);

    private static final VoxelShape SHAPE_N = Shapes.or(Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0), Block.box(2.0, 10.0, 12.0, 14.0, 22.0, 14.0));
    private static final VoxelShape SHAPE_S = rotateShape(SHAPE_N, Direction.SOUTH);
    private static final VoxelShape SHAPE_W = rotateShape(SHAPE_N, Direction.WEST);
    private static final VoxelShape SHAPE_E = rotateShape(SHAPE_N, Direction.EAST);

    private static final VoxelShape TUCKED_N = SHAPE_N.move(0, 0, -TUCK_OFFSET);
    private static final VoxelShape TUCKED_S = SHAPE_S.move(0, 0, TUCK_OFFSET);
    private static final VoxelShape TUCKED_W = SHAPE_W.move(-TUCK_OFFSET, 0, 0);
    private static final VoxelShape TUCKED_E = SHAPE_E.move(TUCK_OFFSET, 0, 0);

    public ChairBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TUCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TUCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            facing = facing.getOpposite();
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean tucked = state.getValue(TUCKED);
        return switch (state.getValue(FACING)) {
            case SOUTH -> tucked ? TUCKED_S : SHAPE_S;
            case WEST -> tucked ? TUCKED_W : SHAPE_W;
            case EAST -> tucked ? TUCKED_E : SHAPE_E;
            default -> tucked ? TUCKED_N : SHAPE_N;
        };
    }

    @Override
    public VoxelShape getDestroyShape(BlockState state) {
        return DESTROY_SHAPE;
    }

    @Override
    public Direction getSeatFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public Vec3 getSeatOffset(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        double forward = state.getValue(TUCKED) ? -0.2 : 0.0;
        return new Vec3(0.5 - facing.getStepX() * forward, getSeatYOffset(), 0.5 - facing.getStepZ() * forward);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        Direction facing = state.getValue(FACING);
        boolean tucked = state.getValue(TUCKED);
        if (!tucked && !canTuck(level, pos, facing)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            tuckParticles((ServerLevel) level, pos, facing, tucked);
            level.setBlock(pos, state.setValue(TUCKED, !tucked), Block.UPDATE_ALL);
            tuckParticles((ServerLevel) level, pos, facing, !tucked);
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private boolean canTuck(Level level, BlockPos pos, Direction facing) {
        BlockPos tablePos = pos.relative(facing);
        if (!(level.getBlockState(tablePos).getBlock() instanceof TableBlock)) {
            return false;
        }
        int mask = TableBlock.legMask(level, tablePos);
        return switch (facing) {
            case NORTH -> !TableBlock.hasLeg(mask, 2) && !TableBlock.hasLeg(mask, 3);
            case SOUTH -> !TableBlock.hasLeg(mask, 0) && !TableBlock.hasLeg(mask, 1);
            case EAST -> !TableBlock.hasLeg(mask, 0) && !TableBlock.hasLeg(mask, 2);
            case WEST -> !TableBlock.hasLeg(mask, 1) && !TableBlock.hasLeg(mask, 3);
            default -> false;
        };
    }

    static VoxelShape rotateShape(VoxelShape shape, Direction to) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> rotated[0] = Shapes.or(rotated[0], switch (to) {
            case SOUTH -> Shapes.box(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
            case WEST -> Shapes.box(z1, y1, 1 - x2, z2, y2, 1 - x1);
            case EAST -> Shapes.box(1 - z2, y1, x1, 1 - z1, y2, x2);
            default -> Shapes.box(x1, y1, z1, x2, y2, z2);
        }));
        return rotated[0];
    }

    static void tuckParticles(ServerLevel level, BlockPos pos, Direction facing, boolean tucked) {
        BlockState ground = level.getBlockState(pos.below());
        if (ground.isAir()) {
            return;
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        double tuck = tucked ? -0.3125 : 0.0;
        double[][] legs = switch (facing) {
            case SOUTH -> new double[][]{{0.125, 0.125 - tuck}, {0.875, 0.125 - tuck}};
            case EAST -> new double[][]{{0.125 - tuck, 0.125}, {0.125 - tuck, 0.875}};
            case WEST -> new double[][]{{0.875 + tuck, 0.125}, {0.875 + tuck, 0.875}};
            default -> new double[][]{{0.125, 0.875 + tuck}, {0.875, 0.875 + tuck}};
        };
        for (double[] leg : legs) {
            level.sendParticles(particle, pos.getX() + leg[0], pos.getY() + 0.1, pos.getZ() + leg[1], 1, 0.05, 0.02, 0.05, 0.0);
        }
    }
}
