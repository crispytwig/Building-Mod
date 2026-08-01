package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.sound.CherrySounds;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(
            method = "getSoundType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/SoundType;",
            at = @At("RETURN"),
            cancellable = true)
    private void artisanal$cherryWoodSounds(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        cir.setReturnValue(CherrySounds.replace(state, cir.getReturnValue()));
    }
}
