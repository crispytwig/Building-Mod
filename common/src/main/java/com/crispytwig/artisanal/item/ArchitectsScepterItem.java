package com.crispytwig.artisanal.item;

import com.crispytwig.artisanal.entity.Wright;
import com.crispytwig.artisanal.registry.ModDataComponents;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArchitectsScepterItem extends Item {

    //region Data
    private static final Direction[] DIRECTIONS = Direction.values();

    public ArchitectsScepterItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static ScepterOccupant getOccupant(ItemStack stack) {
        return stack.get(ModDataComponents.CAPTURED_ENTITY.get());
    }

    public static ItemStack findLinked(Player player, UUID builder) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (builder.equals(stack.get(ModDataComponents.BUILDER.get()))) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    //endregion

    //region Building
    private static List<BlockPos> taxicab(Level level, BlockPos from, BlockPos to, int limit) {
        List<BlockPos> plan = new ArrayList<>();
        if (limit <= 0) {
            return plan;
        }

        BoundingBox box = BoundingBox.fromCorners(from, to);
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> pending = new ArrayDeque<>();
        visited.add(from);
        pending.add(from);

        while (!pending.isEmpty() && plan.size() < limit && visited.size() <= 32768) {
            BlockPos pos = pending.removeFirst();
            if (level.getBlockState(pos).canBeReplaced()) {
                plan.add(pos);
            }

            for (Direction direction : DIRECTIONS) {
                BlockPos next = pos.relative(direction);
                if (box.isInside(next) && visited.add(next)) {
                    pending.addLast(next);
                }
            }
        }

        plan.sort(Comparator
                .<BlockPos>comparingInt(BlockPos::getY)
                .thenComparingInt(pos -> Math.abs(pos.getX() - from.getX()))
                .thenComparingInt(pos -> Math.abs(pos.getZ() - from.getZ())
                        * ((pos.getX() - from.getX()) % 2 == 0 ? 1 : -1)));
        return plan;
    }

    @Nullable
    private static Wright summonBuilder(ServerLevel level, Player player, BlockPos anchor, List<BlockPos> plan, ItemStack blocks) {
        Wright wright = ModEntityTypes.WRIGHT.get().create(level);
        if (wright == null) {
            return null;
        }

        Vec3 spawn = Vec3.atCenterOf(anchor).add(0.0, 0.9, 0.0);
        wright.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
        wright.startBuild(player.getUUID(), plan, blocks.copyWithCount(plan.size()));
        if (!player.getAbilities().instabuild) {
            blocks.shrink(plan.size());
        }
        level.addFreshEntity(wright);

        level.sendParticles(ParticleTypes.POOF, spawn.x, spawn.y, spawn.z, 12, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, anchor, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.6F, 1.4F);
        return wright;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) {
            return;
        }

        UUID builder = stack.get(ModDataComponents.BUILDER.get());
        if (builder == null) {
            return;
        }

        if (serverLevel.getEntity(builder) instanceof Wright) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.getCooldowns().addCooldown(this, 40);
            }
        } else {
            stack.remove(ModDataComponents.BUILDER.get());
            player.getCooldowns().removeCooldown(this);
        }
    }
    //endregion

    //region Interaction
    public static InteractionResult tryCapture(Player player, Entity target, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.ARCHITECTS_SCEPTER.get()) || getOccupant(stack) != null) {
            return InteractionResult.PASS;
        }

        ScepterOccupant occupant = ScepterOccupant.of(target);
        if (occupant == null || !target.isAlive()) {
            return InteractionResult.PASS;
        }

        UUID builder = stack.get(ModDataComponents.BUILDER.get());
        if (builder != null && !builder.equals(target.getUUID())) {
            return InteractionResult.PASS;
        }

        Level level = target.level();
        if (level.isClientSide) {
            player.swing(hand);
        } else {
            if (target instanceof Allay allay) {
                allay.getInventory().removeAllItems().forEach(allay::spawnAtLocation);
            }
            if (target instanceof Wright wright && wright.cancelBuild(player)) {
                player.getCooldowns().removeCooldown(stack.getItem());
            }
            stack.remove(ModDataComponents.BUILDER.get());
            stack.set(ModDataComponents.CAPTURED_ENTITY.get(), occupant);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            target.discard();
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || getOccupant(stack) != ScepterOccupant.WRIGHT) {
            return InteractionResult.PASS;
        }

        ItemStack blocks = player.getOffhandItem();
        if (blocks.isEmpty() || !(blocks.getItem() instanceof BlockItem)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos target = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(context.getClickedFace());
        GlobalPos anchor = stack.get(ModDataComponents.SELECTION_ANCHOR.get());

        if (anchor == null || anchor.dimension() != level.dimension()) {
            stack.set(ModDataComponents.SELECTION_ANCHOR.get(), new GlobalPos(level.dimension(), target));
            level.playSound(player, target, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.6F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        List<BlockPos> plan = taxicab(level, anchor.pos(), target, blocks.getCount());
        if (plan.isEmpty()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level instanceof ServerLevel serverLevel) {
            Wright wright = summonBuilder(serverLevel, player, anchor.pos(), plan, blocks);
            if (wright == null) {
                return InteractionResult.PASS;
            }
            stack.remove(ModDataComponents.CAPTURED_ENTITY.get());
            stack.set(ModDataComponents.BUILDER.get(), wright.getUUID());
        }
        stack.remove(ModDataComponents.SELECTION_ANCHOR.get());
        player.getCooldowns().addCooldown(this, Wright.estimateBuildTicks(plan.size()));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ScepterOccupant occupant = getOccupant(stack);

        if (occupant == ScepterOccupant.WRIGHT) {
            if (stack.remove(ModDataComponents.SELECTION_ANCHOR.get()) == null) {
                return InteractionResultHolder.pass(stack);
            }
            level.playSound(player, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (occupant != ScepterOccupant.ALLAY) {
            return InteractionResultHolder.pass(stack);
        }

        player.getCooldowns().addCooldown(this, AllayFlightHandler.COOLDOWN_TICKS);
        if (player instanceof ServerPlayer serverPlayer) {
            AllayFlightHandler.start(serverPlayer);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ScepterOccupant occupant = getOccupant(stack);
        if (occupant != null) {
            tooltip.add(occupant.getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        }
    }
    //endregion
}
