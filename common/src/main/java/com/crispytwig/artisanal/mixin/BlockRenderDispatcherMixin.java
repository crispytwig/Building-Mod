package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.client.renderer.PanelModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @WrapOperation(
            method = "renderBatched",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel artisanal$panelModel(BlockRenderDispatcher dispatcher, BlockState modelState, Operation<BakedModel> original,
                                            @Local(argsOnly = true) BlockPos pos) {
        return PanelModel.wrap(original.call(dispatcher, modelState), modelState, pos);
    }
}
