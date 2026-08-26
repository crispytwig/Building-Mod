package com.crispytwig.bbb.sound;

import com.crispytwig.bbb.platform.Services;
import com.crispytwig.bbb.registry.ModTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class PrismarineSounds {
    private PrismarineSounds() {
    }

    public static SoundType replace(BlockState state, SoundType original) {
        if (!Services.CONFIG.prismarineDeepslateSounds()) {
            return original;
        }
        if (state.is(ModTags.PRISMARINE)) {
            return SoundType.DEEPSLATE;
        }
        return original;
    }
}
