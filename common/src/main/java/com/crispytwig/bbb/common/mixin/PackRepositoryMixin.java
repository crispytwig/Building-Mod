package com.crispytwig.bbb.common.mixin;

import com.crispytwig.bbb.common.data.BuildingButBetterPack;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Stream;

// Adapted from ClutterNoMore: https://github.com/Alchemists-Of-Yore/ClutterNoMore
@Mixin(value = PackRepository.class, priority = 500)
public class PackRepositoryMixin {
    @ModifyReturnValue(method = "openAllSelected", at = @At("RETURN"))
    private List<PackResources> bbb$injectRuntimePack(List<PackResources> opened) {
        if (opened.contains(BuildingButBetterPack.INSTANCE)) {
            return opened;
        }
        return Stream.concat(Stream.of(BuildingButBetterPack.INSTANCE), opened.stream()).toList();
    }
}
