package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.entity.ai.SitBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerGoalPackages.class)
public class VillagerGoalPackagesMixin {
    @Inject(method = "getIdlePackage", at = @At("RETURN"), cancellable = true)
    private static void artisanal$getIdlePackage(VillagerProfession profession, float speedModifier, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        cir.setReturnValue(artisanal$withSitBehavior(cir.getReturnValue(), speedModifier));
    }

    @Inject(method = "getPlayPackage", at = @At("RETURN"), cancellable = true)
    private static void artisanal$getPlayPackage(float speedModifier, CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>>> cir) {
        cir.setReturnValue(artisanal$withSitBehavior(cir.getReturnValue(), speedModifier));
    }

    @Unique
    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> artisanal$withSitBehavior(ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> behaviors, float speedModifier) {
        return ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Villager>>>builder().addAll(behaviors).add(Pair.of(2, new SitBehavior(speedModifier))).build();
    }
}
