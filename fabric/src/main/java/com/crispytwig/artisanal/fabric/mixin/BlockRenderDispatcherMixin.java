package com.crispytwig.artisanal.fabric.mixin;

import com.crispytwig.artisanal.client.renderer.PanelModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin {
    @Redirect(
            method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;getBlockModel(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel artisanal$panelModel(BlockRenderDispatcher dispatcher, BlockState modelState,
                                            BlockState state, BlockPos pos, BlockAndTintGetter level,
                                            PoseStack poseStack, VertexConsumer consumer, boolean checkSides,
                                            RandomSource random) {
        return PanelModel.wrap(dispatcher.getBlockModel(modelState), modelState, pos);
    }
}
