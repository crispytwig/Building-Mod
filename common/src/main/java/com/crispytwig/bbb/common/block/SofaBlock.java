package com.crispytwig.bbb.common.block;

import com.crispytwig.bbb.common.block.entity.SofaBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SofaBlock extends AbstractSeatBlock implements EntityBlock {
    public static final MapCodec<SofaBlock> CODEC = simpleCodec(SofaBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LEFT = BooleanProperty.create("left");
    public static final BooleanProperty RIGHT = BooleanProperty.create("right");

    public static final String[] PARTS = {"backrest", "seat", "legs_left", "legs_right", "arm_left", "arm_right"};

    public static final float TUCK_BACKREST_DOWN = -3.0F;
    public static final float TUCK_BACKREST_BACK = -1.0F;
    public static final float TUCK_DOWN = -2.0F;
    public static final float TUCK_FRONT = 2.0F;
    public static final float TUCK_ARM_SIDE = 3.98F;

    private static final VoxelShape BACKREST = modelBox(0.0, 2.0, 10.0, 16.0, 19.0, 17.0);
    private static final VoxelShape SEAT = modelBox(0.0, 2.0, -2.0, 16.0, 10.0, 10.0);
    private static final VoxelShape LEFT_ARM = modelBox(-4.0, 3.98, -2.02, 3.04, 14.02, 13.02);
    private static final VoxelShape RIGHT_ARM = modelBox(12.96, 3.98, -2.02, 20.0, 14.02, 13.02);
    private static final VoxelShape LEFT_LEGS = Shapes.or(modelBox(1.0, 0.0, 0.0, 4.0, 2.0, 3.0), modelBox(1.0, 0.0, 12.0, 4.0, 2.0, 15.0));
    private static final VoxelShape RIGHT_LEGS = Shapes.or(modelBox(12.0, 0.0, 0.0, 15.0, 2.0, 3.0), modelBox(12.0, 0.0, 12.0, 15.0, 2.0, 15.0));

    private static final Map<Integer, VoxelShape> SHAPES = new ConcurrentHashMap<>();

    private static final Map<Direction, AABB> LEFT_CREVICE = new EnumMap<>(Direction.class);
    private static final Map<Direction, AABB> RIGHT_CREVICE = new EnumMap<>(Direction.class);

    static {
        AABB left = new AABB(0.0, 10.0 / 16.0, 8.0 / 16.0, 2.0 / 16.0, 12.0 / 16.0, 11.0 / 16.0);
        AABB right = new AABB(14.0 / 16.0, 10.0 / 16.0, 8.0 / 16.0, 1.0, 12.0 / 16.0, 11.0 / 16.0);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            LEFT_CREVICE.put(facing, rotate(left, quarterTurns(facing)));
            RIGHT_CREVICE.put(facing, rotate(right, quarterTurns(facing)));
        }
    }

    public SofaBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LEFT, false).setValue(RIGHT, false));
    }

    @Override
    protected MapCodec<SofaBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SofaBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEFT, RIGHT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(LEFT, isSofa(level.getBlockState(pos.relative(facing.getCounterClockWise())), facing))
                .setValue(RIGHT, isSofa(level.getBlockState(pos.relative(facing.getClockWise())), facing));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            Direction facing = state.getValue(FACING);
            boolean connects = isSofa(neighborState, facing);
            if (direction == facing.getCounterClockWise()) {
                return state.setValue(LEFT, connects);
            }
            if (direction == facing.getClockWise()) {
                return state.setValue(RIGHT, connects);
            }
        }
        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (tryOpenCrevice(state, level, pos, player, hitResult)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (tryOpenCrevice(state, level, pos, player, hitResult)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof SofaBlockEntity sofa) {
                Containers.dropContents(level, pos, sofa);
                sofa.clearContent();
            }
            Direction facing = state.getValue(FACING);
            BlockPos leftPos = pos.relative(facing.getCounterClockWise());
            if (level.getBlockEntity(leftPos) instanceof SofaBlockEntity leftSofa
                    && isSofa(leftSofa.getBlockState(), facing)
                    && leftSofa.getBlockState().getValue(RIGHT)) {
                Containers.dropContents(level, leftPos, leftSofa);
                leftSofa.clearContent();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean left = state.getValue(LEFT);
        boolean right = state.getValue(RIGHT);
        boolean behind = occupied(level, pos, facing.getOpposite());
        boolean above = occupied(level, pos, Direction.UP);
        boolean front = occupied(level, pos, facing);
        boolean leftBlocked = !left && occupied(level, pos, facing.getCounterClockWise());
        boolean rightBlocked = !right && occupied(level, pos, facing.getClockWise());

        int key = facing.get2DDataValue()
                | (left ? 1 << 2 : 0) | (right ? 1 << 3 : 0)
                | (behind ? 1 << 4 : 0) | (above ? 1 << 5 : 0) | (front ? 1 << 6 : 0)
                | (leftBlocked ? 1 << 7 : 0) | (rightBlocked ? 1 << 8 : 0);
        return SHAPES.computeIfAbsent(key, unused -> buildShape(facing, left, right, behind, above, front, leftBlocked, rightBlocked));
    }

    @Override
    public Direction getSeatFacing(BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    public Vec3 getSeatOffset(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        boolean left = state.getValue(LEFT);
        boolean right = state.getValue(RIGHT);
        boolean single = !left && !right;
        boolean leftArmTucked = !single && !left && occupied(level, pos, facing.getCounterClockWise());
        boolean rightArmTucked = !single && !right && occupied(level, pos, facing.getClockWise());

        double armShift = TUCK_ARM_SIDE / 32.0;
        double x = 0.5 + (leftArmTucked ? armShift : 0.0) - (rightArmTucked ? armShift : 0.0);
        double y = getSeatYOffset() + (occupied(level, pos, Direction.UP) ? TUCK_DOWN / 16.0 : 0.0);
        double z = 5.0 / 16.0 + (occupied(level, pos, facing) ? TUCK_FRONT / 16.0 : 0.0);

        for (int i = 0; i < quarterTurns(facing); i++) {
            double rotated = 1.0 - z;
            z = x;
            x = rotated;
        }
        return new Vec3(x, y, z);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(LEFT, state.getValue(RIGHT))
                .setValue(RIGHT, state.getValue(LEFT));
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance * 0.75F);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        Vec3 delta = entity.getDeltaMovement();
        if (entity.isSuppressingBounce() || delta.y >= -0.6) {
            super.updateEntityAfterFallOn(level, entity);
        } else {
            entity.setDeltaMovement(delta.x, -delta.y * 0.4 * (entity instanceof LivingEntity ? 1.0 : 0.8), delta.z);
        }
    }

    public static boolean occupied(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighbor = pos.relative(direction);
        BlockState state = level.getBlockState(neighbor);
        return state.getBlock() instanceof SofaBlock || state.isFaceSturdy(level, neighbor, direction.getOpposite());
    }

    public static int quarterTurns(Direction facing) {
        return Math.floorMod((int) facing.toYRot() + 180, 360) / 90;
    }

    private boolean tryOpenCrevice(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos owner = creviceOwner(state, level, pos, hitResult);
        if (owner == null) {
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(owner) instanceof SofaBlockEntity sofa) {
            player.openMenu(sofa);
            level.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    private BlockPos creviceOwner(BlockState state, Level level, BlockPos pos, BlockHitResult hitResult) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();
        double tuck = occupied(level, pos, facing) ? TUCK_FRONT / 16.0 : 0.0;
        double drop = occupied(level, pos, Direction.UP) ? TUCK_DOWN / 16.0 : 0.0;
        Vec3 local = hitResult.getLocation().subtract(
                pos.getX() + back.getStepX() * tuck,
                pos.getY() + drop,
                pos.getZ() + back.getStepZ() * tuck);
        if (state.getValue(RIGHT) && RIGHT_CREVICE.get(facing).inflate(0.05).contains(local)) {
            return pos;
        }
        if (state.getValue(LEFT) && LEFT_CREVICE.get(facing).inflate(0.05).contains(local)) {
            return pos.relative(facing.getCounterClockWise());
        }
        return null;
    }

    private static boolean isSofa(BlockState state, Direction facing) {
        return state.getBlock() instanceof SofaBlock && state.getValue(FACING) == facing;
    }

    private static VoxelShape buildShape(Direction facing, boolean left, boolean right, boolean behind, boolean above, boolean front, boolean leftBlocked, boolean rightBlocked) {
        VoxelShape shape = move(BACKREST, 0.0F, above ? TUCK_BACKREST_DOWN : 0.0F, behind ? TUCK_BACKREST_BACK : 0.0F);
        shape = Shapes.or(shape, move(SEAT, 0.0F, above ? TUCK_DOWN : 0.0F, front ? TUCK_FRONT : 0.0F));

        boolean single = !left && !right;
        if (!left) {
            if (!(single && leftBlocked)) {
                shape = Shapes.or(shape, move(LEFT_ARM, leftBlocked ? TUCK_ARM_SIDE : 0.0F, above ? TUCK_DOWN : 0.0F, front ? TUCK_FRONT : 0.0F));
            }
            if (!above) {
                shape = Shapes.or(shape, LEFT_LEGS);
            }
        }
        if (!right) {
            if (!(single && rightBlocked)) {
                shape = Shapes.or(shape, move(RIGHT_ARM, rightBlocked ? -TUCK_ARM_SIDE : 0.0F, above ? TUCK_DOWN : 0.0F, front ? TUCK_FRONT : 0.0F));
            }
            if (!above) {
                shape = Shapes.or(shape, RIGHT_LEGS);
            }
        }
        return rotate(shape, quarterTurns(facing));
    }

    private static VoxelShape move(VoxelShape shape, float x, float y, float z) {
        return x == 0.0F && y == 0.0F && z == 0.0F ? shape : shape.move(x / 16.0, y / 16.0, z / 16.0);
    }

    private static VoxelShape modelBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Shapes.create(new AABB(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0));
    }

    private static AABB rotate(AABB box, int quarterTurns) {
        AABB rotated = box;
        for (int i = 0; i < quarterTurns; i++) {
            rotated = new AABB(1.0 - rotated.maxZ, rotated.minY, rotated.minX, 1.0 - rotated.minZ, rotated.maxY, rotated.maxX);
        }
        return rotated;
    }

    private static VoxelShape rotate(VoxelShape shape, int quarterTurns) {
        VoxelShape result = shape;
        for (int i = 0; i < quarterTurns; i++) {
            VoxelShape[] rotated = {Shapes.empty()};
            result.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    rotated[0] = Shapes.or(rotated[0], Shapes.create(new AABB(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX))));
            result = rotated[0];
        }
        return result;
    }
}
