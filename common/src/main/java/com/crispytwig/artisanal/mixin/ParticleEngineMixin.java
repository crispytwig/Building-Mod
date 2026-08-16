package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.block.DestroyShaped;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Redirect(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
    private VoxelShape artisanal$destroyShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getBlock() instanceof DestroyShaped destroyShaped) {
            return destroyShaped.getDestroyShape(state);
        }
        return state.getShape(level, pos);
    }
}
