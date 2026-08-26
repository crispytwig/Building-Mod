package com.crispytwig.bbb.block;

import com.crispytwig.bbb.block.entity.TimberFrameBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

public class TimberFrameBlock extends Block implements EntityBlock, Contents {
    public static final MapCodec<TimberFrameBlock> CODEC = simpleCodec(TimberFrameBlock::new);

    public static final EnumProperty<TrimType> TYPE = EnumProperty.create("type", TrimType.class);
    public static final EnumProperty<CrossType> CROSS = EnumProperty.create("cross", CrossType.class);
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public TimberFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(TYPE, TrimType.SINGLE)
                .setValue(CROSS, CrossType.NONE)
                .setValue(FILLED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE, CROSS, FILLED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TimberFrameBlockEntity(pos, state);
    }

    @Override
    public BlockState getContents(BlockGetter level, BlockPos pos, BlockState state) {
        if (!state.getValue(FILLED)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (level.getBlockEntity(pos) instanceof TimberFrameBlockEntity frame) {
            return frame.getHeldBlock();
        }
        return TimberFrameBlockEntity.removedHeldBlock(pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return state;
        }
        return state.setValue(TYPE, TrimType.of(
                connectsTo(context.getLevel(), context.getClickedPos().below()),
                connectsTo(context.getLevel(), context.getClickedPos().above())));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        updateConnections(level, pos, state, true);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            updateConnections(level, pos, state, false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(FILLED) || !(stack.getItem() instanceof BlockItem item) || !player.mayBuild()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockState held = item.getBlock().getStateForPlacement(new FillContext(player, hand, stack, hitResult));
        if (held == null || !canHold(held, level, pos)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TimberFrameBlockEntity frame) {
            frame.setHeldBlock(held);
            level.setBlock(pos, state.setValue(FILLED, true), Block.UPDATE_ALL);
            SoundType soundType = held.getSoundType();
            level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TimberFrameBlockEntity frame) {
            BlockState held = frame.getHeldBlock();
            ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
            if (!held.isAir() && (!held.requiresCorrectToolForDrops() || (tool != null && tool.isCorrectToolForDrops(held)))) {
                drops.addAll(held.getDrops(params));
            }
        }
        return drops;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState held = Contents.of(level, pos, state);
        if (!held.isAir() && held.getDestroySpeed(level, pos) >= 0.0F) {
            return held.getDestroyProgress(player, level, pos);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.getBlock() instanceof TimberFrameBlock) {
            return state.getValue(FILLED) == adjacentState.getValue(FILLED);
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FILLED) ? level.getMaxLightLevel() : super.getLightBlock(state, level, pos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(FILLED) && super.propagatesSkylightDown(state, level, pos);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FILLED) ? 0.2F : super.getShadeBrightness(state, level, pos);
    }

    public static InteractionResult tryCycleCross(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof TimberFrameBlock) || state.getValue(TYPE) != TrimType.SINGLE) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CrossType cross = state.getValue(CROSS);
        level.setBlock(pos, state.setValue(CROSS, player.isSecondaryUseActive() ? cross.previous() : cross.next()), Block.UPDATE_ALL);
        SoundType soundType = state.getSoundType();
        level.playSound(null, pos, soundType.getStepSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch());
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return InteractionResult.CONSUME;
    }

    private static boolean canHold(BlockState state, Level level, BlockPos pos) {
        return !state.hasBlockEntity()
                && state.isSolidRender(level, pos)
                && Block.isShapeFullBlock(state.getCollisionShape(level, pos));
    }

    private static void updateConnections(LevelAccessor level, BlockPos pos, BlockState state, boolean connected) {
        for (Direction.AxisDirection direction : Direction.AxisDirection.values()) {
            if (!state.getValue(TYPE).isConnected(direction)) {
                continue;
            }
            BlockPos neighbourPos = pos.relative(Direction.get(direction, Direction.Axis.Y));
            BlockState neighbour = level.getBlockState(neighbourPos);
            if (neighbour.getBlock() instanceof TimberFrameBlock) {
                level.setBlock(neighbourPos, neighbour.setValue(TYPE,
                        neighbour.getValue(TYPE).with(direction.opposite(), connected)), Block.UPDATE_CLIENTS);
            }
        }
    }

    private static boolean connectsTo(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof TimberFrameBlock;
    }

    private static class FillContext extends BlockPlaceContext {
        FillContext(Player player, InteractionHand hand, ItemStack stack, BlockHitResult hitResult) {
            super(player, hand, stack, hitResult);
            replaceClicked = true;
        }
    }
}
