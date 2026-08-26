package com.crispytwig.bbb.neoforge.mixin;

import com.crispytwig.bbb.block.Contents;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Redirect(method = "crack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TerrainParticle;updateSprite(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/particle/TerrainParticle;"))
    private TerrainParticle bbb$keepPickedSprite(TerrainParticle particle, BlockState state, BlockPos pos) {
        return state.getBlock() instanceof Contents ? particle : particle.updateSprite(state, pos);
    }
}
