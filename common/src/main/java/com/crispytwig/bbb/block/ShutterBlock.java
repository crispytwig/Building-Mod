package com.crispytwig.bbb.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ShutterBlock extends Block {
    public static final MapCodec<ShutterBlock> CODEC = simpleCodec(ShutterBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(0, 0, 0, 16, 16, 3),
            Direction.SOUTH, Block.box(0, 0, 13, 16, 16, 16),
            Direction.WEST, Block.box(0, 0, 0, 3, 16, 16),
            Direction.EAST, Block.box(13, 0, 0, 16, 16, 16));

    public ShutterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(HINGE, DoorHingeSide.LEFT));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, HINGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(OPEN)) {
            facing = state.getValue(HINGE) == DoorHingeSide.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
        }
        return SHAPES.get(facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockState state = defaultBlockState().setValue(FACING, facing);

        for (BlockPos neighborPos : new BlockPos[]{context.getClickedPos().below(), context.getClickedPos().above()}) {
            BlockState neighbor = context.getLevel().getBlockState(neighborPos);
            if (neighbor.is(this) && neighbor.getValue(FACING) == facing) {
                return state.setValue(HINGE, neighbor.getValue(HINGE));
            }
        }

        return state.setValue(HINGE, hinge(context));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(asItem())) {
            BlockPos targetPos = hitResult.getLocation().y - pos.getY() > 0.5 ? pos.above() : pos.below();
            if (level.getBlockState(targetPos).canBeReplaced(new BlockPlaceContext(player, hand, stack, hitResult.withPosition(targetPos)))) {
                level.setBlock(targetPos, defaultBlockState()
                        .setValue(FACING, state.getValue(FACING))
                        .setValue(HINGE, state.getValue(HINGE)), 3);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(player, targetPos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
                return ItemInteractionResult.SUCCESS;
            }
        }

        toggle(state, level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, powered), 2);
            if (state.getValue(OPEN) != powered) {
                playToggleSound(null, level, pos, powered);
                propagate(level, pos, powered);
            }
        }
    }

    private void toggle(BlockState state, Level level, BlockPos pos, @Nullable Player player) {
        boolean open = !state.getValue(OPEN);
        level.setBlock(pos, state.cycle(OPEN), 10);
        playToggleSound(player, level, pos, open);
        propagate(level, pos, open);
    }

    private void propagate(Level level, BlockPos pos, boolean open) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return;
        }

        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN}) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            while (true) {
                mutablePos.move(direction);
                BlockState neighbor = level.getBlockState(mutablePos);
                if (!neighbor.is(this)
                        || neighbor.getValue(FACING) != state.getValue(FACING)
                        || neighbor.getValue(HINGE) != state.getValue(HINGE)) {
                    break;
                }
                if (neighbor.getValue(OPEN) != open) {
                    level.setBlock(mutablePos, neighbor.setValue(OPEN, open), 2);
                }
            }
        }
    }

    private void playToggleSound(@Nullable Player player, Level level, BlockPos pos, boolean open) {
        level.playSound(player, pos, open ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    private DoorHingeSide hinge(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection();

        BlockPos counterClockWisePos = pos.relative(facing.getCounterClockWise());
        BlockPos clockWisePos = pos.relative(facing.getClockWise());
        BlockState counterClockWise = level.getBlockState(counterClockWisePos);
        BlockState clockWise = level.getBlockState(clockWisePos);

        if (counterClockWise.is(this) != clockWise.is(this)) {
            return counterClockWise.is(this) ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        }

        int sturdySides = (counterClockWise.isCollisionShapeFullBlock(level, counterClockWisePos) ? -1 : 0)
                + (clockWise.isCollisionShapeFullBlock(level, clockWisePos) ? 1 : 0);
        if (sturdySides != 0) {
            return sturdySides > 0 ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
        }

        int stepX = facing.getStepX();
        int stepZ = facing.getStepZ();
        Vec3 clickLocation = context.getClickLocation();
        double x = clickLocation.x - pos.getX();
        double z = clickLocation.z - pos.getZ();
        return (stepX >= 0 || !(z < 0.5)) && (stepX <= 0 || !(z > 0.5)) && (stepZ >= 0 || !(x > 0.5)) && (stepZ <= 0 || !(x < 0.5))
                ? DoorHingeSide.LEFT
                : DoorHingeSide.RIGHT;
    }
}
