package com.crispytwig.bbb.common.block;

import com.crispytwig.bbb.common.block.entity.CurtainBlockEntity;
import com.crispytwig.bbb.common.item.CurtainBlockItem;
import com.crispytwig.bbb.common.registry.ModSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurtainBlock extends Block implements EntityBlock {
    public static final MapCodec<CurtainBlock> CODEC = simpleCodec(CurtainBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<CurtainSide> SIDE = EnumProperty.create("side", CurtainSide.class);

    public static final String[] PARTS = {
            "closed_top_single", "closed_top_middle", "closed_mid_middle", "closed_bot_middle",
            "open_single_left", "open_single_right",
            "open_top_left", "open_mid_left", "open_bot_left",
            "open_top_right", "open_mid_right", "open_bot_right",
            "open_top_middle"};

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(0, 0, 0, 16, 16, 3),
            Direction.SOUTH, Block.box(0, 0, 13, 16, 16, 16),
            Direction.WEST, Block.box(0, 0, 0, 3, 16, 16),
            Direction.EAST, Block.box(13, 0, 0, 16, 16, 16));

    public CurtainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(SIDE, CurtainSide.SINGLE));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CurtainBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, SIDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(OPEN, openHint(level, pos, facing))
                .setValue(SIDE, side(level, pos, facing));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(SIDE, side(level, pos, state.getValue(FACING)));
        }
        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof CurtainBlockItem
                && level.getBlockEntity(pos) instanceof CurtainBlockEntity curtain
                && curtain.getLength() < CurtainBlockEntity.MAX_LENGTH
                && !isPathBlocked(level, pos.below(curtain.getLength()))) {
            if (!level.isClientSide) {
                curtain.setLength(curtain.getLength() + 1);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(player, pos, SoundType.WOOL.getPlaceSound(), SoundSource.BLOCKS, 0.8F, 0.8F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isSecondaryUseActive()
                    && level.getBlockEntity(pos) instanceof CurtainBlockEntity curtain && curtain.getLength() > 1) {
                curtain.setLength(curtain.getLength() - 1);
                popResource(level, pos, new ItemStack(this));
                level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
            } else {
                boolean open = !state.getValue(OPEN);
                for (BlockPos rowPos : connectedRow(level, pos, state.getValue(FACING))) {
                    BlockState rowState = level.getBlockState(rowPos);
                    if (rowState.getBlock() == this) {
                        level.setBlock(rowPos, rowState.setValue(OPEN, open), Block.UPDATE_CLIENTS);
                    }
                }
                level.playSound(null, pos, open ? ModSounds.CURTAIN_OPEN.get() : ModSounds.CURTAIN_CLOSE.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        int length = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CurtainBlockEntity curtain
                ? curtain.getLength() : 1;
        return List.of(new ItemStack(this, length));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape base = SHAPES.get(state.getValue(FACING));
        int length = level.getBlockEntity(pos) instanceof CurtainBlockEntity curtain ? curtain.getLength() : 1;
        VoxelShape shape = base;
        for (int i = 1; i < length; i++) {
            shape = Shapes.or(shape, base.move(0.0, -i, 0.0));
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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

    private boolean isCurtain(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() == this && state.getValue(FACING) == facing;
    }

    private boolean isPathBlocked(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.canBeReplaced();
    }

    private CurtainSide side(BlockGetter level, BlockPos pos, Direction facing) {
        boolean hasLeft = isCurtain(level, pos.relative(facing.getCounterClockWise()), facing);
        boolean hasRight = isCurtain(level, pos.relative(facing.getClockWise()), facing);
        if (!hasLeft && !hasRight) {
            return CurtainSide.SINGLE;
        }
        if (!hasLeft) {
            return CurtainSide.LEFT;
        }
        if (!hasRight) {
            return CurtainSide.RIGHT;
        }
        return CurtainSide.MIDDLE;
    }

    private boolean openHint(BlockGetter level, BlockPos pos, Direction facing) {
        for (Direction direction : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockPos neighborPos = pos.relative(direction);
            if (isCurtain(level, neighborPos, facing)) {
                return level.getBlockState(neighborPos).getValue(OPEN);
            }
        }
        return false;
    }

    private List<BlockPos> connectedRow(Level level, BlockPos pos, Direction facing) {
        List<BlockPos> row = new ArrayList<>();
        BlockPos current = pos;
        while (isCurtain(level, current.relative(facing.getCounterClockWise()), facing)) {
            current = current.relative(facing.getCounterClockWise());
        }
        while (isCurtain(level, current, facing)) {
            row.add(current.immutable());
            current = current.relative(facing.getClockWise());
        }
        return row;
    }
}
