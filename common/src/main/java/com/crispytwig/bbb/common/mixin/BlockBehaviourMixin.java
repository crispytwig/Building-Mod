package com.crispytwig.bbb.common.mixin;

import com.crispytwig.bbb.common.config.BlockConfig;
import com.crispytwig.bbb.common.sound.CherrySounds;
import com.crispytwig.bbb.common.sound.PrismarineSounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(
            method = "getSoundType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/SoundType;",
            at = @At("RETURN"),
            cancellable = true)
    private void bbb$replaceSounds(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        cir.setReturnValue(PrismarineSounds.replace(state, CherrySounds.replace(state, cir.getReturnValue())));
    }

    @Inject(
            method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true)
    private void bbb$stopDrops(BlockState state, LootParams.Builder params, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!BlockConfig.isEnabled(state.getBlock())) {
            cir.setReturnValue(List.of());
        }
    }
}
