package com.crispytwig.artisanal.mixin;

import com.crispytwig.artisanal.sound.CherrySounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WoodType.class)
public class WoodTypeMixin {
    @ModifyVariable(method = "register", at = @At("HEAD"), argsOnly = true)
    private static WoodType artisanal$cherryWoodSounds(WoodType type) {
        if (!CherrySounds.appliesTo(type.soundType())) {
            return type;
        }
        return new WoodType(type.name(), type.setType(), type.soundType(), type.hangingSignSoundType(),
                SoundEvents.CHERRY_WOOD_FENCE_GATE_CLOSE, SoundEvents.CHERRY_WOOD_FENCE_GATE_OPEN);
    }
}
