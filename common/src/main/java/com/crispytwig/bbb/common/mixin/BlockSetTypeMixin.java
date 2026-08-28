package com.crispytwig.bbb.common.mixin;

import com.crispytwig.bbb.common.sound.CherrySounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockSetType.class)
public class BlockSetTypeMixin {
    @ModifyVariable(method = "register", at = @At("HEAD"), argsOnly = true)
    private static BlockSetType bbb$cherryWoodSounds(BlockSetType type) {
        if (CherrySounds.appliesTo(type.soundType())) {
            return type;
        }
        return new BlockSetType(type.name(), type.canOpenByHand(), type.canOpenByWindCharge(), type.canButtonBeActivatedByArrows(),
                type.pressurePlateSensitivity(), type.soundType(),
                SoundEvents.CHERRY_WOOD_DOOR_CLOSE, SoundEvents.CHERRY_WOOD_DOOR_OPEN,
                SoundEvents.CHERRY_WOOD_TRAPDOOR_CLOSE, SoundEvents.CHERRY_WOOD_TRAPDOOR_OPEN,
                SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_OFF, SoundEvents.CHERRY_WOOD_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.CHERRY_WOOD_BUTTON_CLICK_OFF, SoundEvents.CHERRY_WOOD_BUTTON_CLICK_ON);
    }
}
