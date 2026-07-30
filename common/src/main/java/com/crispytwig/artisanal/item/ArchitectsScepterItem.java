package com.crispytwig.artisanal.item;

import com.crispytwig.artisanal.flight.AllayFlightHandler;
import com.crispytwig.artisanal.registry.ModDataComponents;
import com.crispytwig.artisanal.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArchitectsScepterItem extends Item {
    public ArchitectsScepterItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static ScepterOccupant getOccupant(ItemStack stack) {
        return stack.get(ModDataComponents.CAPTURED_ENTITY.get());
    }

    public static InteractionResult tryCapture(Player player, Entity target, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.ARCHITECTS_SCEPTER.get()) || getOccupant(stack) != null) {
            return InteractionResult.PASS;
        }

        ScepterOccupant occupant = ScepterOccupant.of(target);
        if (occupant == null || !target.isAlive()) {
            return InteractionResult.PASS;
        }

        Level level = target.level();
        if (level.isClientSide) {
            player.swing(hand);
        } else {
            if (target instanceof Allay allay) {
                allay.getInventory().removeAllItems().forEach(allay::spawnAtLocation);
            }
            stack.set(ModDataComponents.CAPTURED_ENTITY.get(), occupant);
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            target.discard();
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getOccupant(stack) != ScepterOccupant.ALLAY) {
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
}
