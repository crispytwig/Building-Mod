package com.crispytwig.bbb.mixin;

import com.crispytwig.bbb.block.Contents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
            method = "playBlockFallSound",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType bbb$fallSound(BlockState state) {
        LivingEntity entity = (LivingEntity) (Object) this;
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getY() - 0.2F, entity.getZ());
        return Contents.soundType(entity.level(), pos, state);
    }
}
