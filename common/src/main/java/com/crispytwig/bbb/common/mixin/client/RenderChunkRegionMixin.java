package com.crispytwig.bbb.common.mixin.client;

import com.crispytwig.bbb.client.paint.PaintOverride;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderChunkRegion.class)
public class RenderChunkRegionMixin {
    @ModifyReturnValue(method = "getBlockState", at = @At("RETURN"))
    private BlockState bbb$paintPreview(BlockState state, @Local(argsOnly = true) BlockPos pos) {
        return PaintOverride.apply(pos, state);
    }
}
