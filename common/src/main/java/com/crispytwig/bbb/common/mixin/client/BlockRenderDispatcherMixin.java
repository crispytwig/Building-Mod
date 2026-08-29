package com.crispytwig.bbb.common.mixin.client;

import com.crispytwig.bbb.client.renderer.FacadeModel;
import com.crispytwig.bbb.client.renderer.TimberFrameModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @WrapOperation(
            method = {
                    "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V",
                    "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel bbb$facadeModel(BlockRenderDispatcher dispatcher, BlockState modelState, Operation<BakedModel> original,
                                       @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockAndTintGetter level) {
        return TimberFrameModel.wrap(FacadeModel.wrap(original.call(dispatcher, modelState), modelState, pos), level, modelState, pos);
    }
}
