package com.crispytwig.bbb.common.mixin.client;

import com.crispytwig.bbb.client.assets.LayerAssets;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
    @Shadow
    @Final
    private PackType type;

    @Inject(method = "createReload", at = @At("HEAD"))
    private void bbb$generateLayerAssets(Executor backgroundExecutor, Executor gameExecutor, CompletableFuture<Unit> waitingFor, List<PackResources> resourcePacks, CallbackInfoReturnable<ReloadInstance> cir) {
        if (this.type == PackType.CLIENT_RESOURCES) {
            LayerAssets.generate(new MultiPackResourceManager(this.type, resourcePacks));
        }
    }
}
