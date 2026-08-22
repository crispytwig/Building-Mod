package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.registry.ModLayers;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;freeze()V", shift = At.Shift.BEFORE))
    private static void artisanal$registerLayers(CallbackInfo ci) {
        ModLayers.register();
    }
}
