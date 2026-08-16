package com.crispytwig.artisanal.sound;

import com.crispytwig.artisanal.platform.Services;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class CherrySounds {
    private CherrySounds() {
    }

    public static SoundType replace(BlockState state, SoundType original) {
        if (!Services.CONFIG.cherryWoodSounds()) {
            return original;
        }
        if (original == SoundType.WOOD) {
            return SoundType.CHERRY_WOOD;
        }
        if (original == SoundType.HANGING_SIGN) {
            return SoundType.CHERRY_WOOD_HANGING_SIGN;
        }
        if (original == SoundType.GRASS) {
            if (state.is(BlockTags.LEAVES)) {
                return SoundType.CHERRY_LEAVES;
            }
            if (state.is(BlockTags.SAPLINGS)) {
                return SoundType.CHERRY_SAPLING;
            }
        }
        return original;
    }

    public static boolean appliesTo(SoundType type) {
        return type != SoundType.WOOD || !Services.CONFIG.cherryWoodSounds();
    }
}
