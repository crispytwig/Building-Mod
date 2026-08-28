package com.crispytwig.bbb.common.item;

import com.crispytwig.bbb.client.paint.PaintBrushClient;
import com.crispytwig.bbb.common.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaintBrushItem extends Item {
    public static final int MAX_RANGE = 24;

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    public record Held(ItemStack brush, @Nullable BlockState filter) {
    }

    public static @Nullable Held getHeld(ItemStack main, ItemStack off) {
        ItemStack brush;
        ItemStack other;
        if (main.getItem() instanceof PaintBrushItem) {
            brush = main;
            other = off;
        } else if (off.getItem() instanceof PaintBrushItem) {
            brush = off;
            other = main;
        } else {
            return null;
        }
        BlockState filter = other.getItem() instanceof BlockItem blockItem
                ? blockItem.getBlock().defaultBlockState()
                : null;
        return new Held(brush, filter);
    }

    public static @Nullable DyeColor getColor(ItemStack stack) {
        return stack.get(ModDataComponents.PAINT_COLOR.get());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        DyeColor color = getColor(stack);
        if (color != null) {
            tooltip.add(Component.translatable("color.minecraft." + color.getName())
                    .setStyle(Style.EMPTY.withColor(color.getTextureDiffuseColor())));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide && PaintBrushClient.onRightClick()) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack stack, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        return dye(stack, slot.getItem(), player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack other, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access) {
        if (action != ClickAction.SECONDARY) {
            return false;
        }
        return dye(stack, other, player);
    }

    private static boolean dye(ItemStack brush, ItemStack dye, Player player) {
        if (!(dye.getItem() instanceof DyeItem dyeItem)) {
            return false;
        }
        DyeColor color = dyeItem.getDyeColor();
        if (color == getColor(brush)) {
            return false;
        }
        brush.set(ModDataComponents.PAINT_COLOR.get(), color);
        dye.shrink(1);
        player.playSound(SoundEvents.DYE_USE, 0.8F, 1.0F);
        return true;
    }
}
