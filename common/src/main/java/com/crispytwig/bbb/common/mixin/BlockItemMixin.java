package com.crispytwig.bbb.common.mixin;

import com.crispytwig.bbb.common.config.BlockConfig;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(
            method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void bbb$stopPlacing(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!BlockConfig.isEnabled(((BlockItem) (Object) this).getBlock())) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
