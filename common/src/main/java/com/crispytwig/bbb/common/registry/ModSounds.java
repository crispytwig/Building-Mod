package com.crispytwig.bbb.common.registry;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.platform.registry.DeferredHolder;
import com.crispytwig.bbb.platform.registry.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, BuildingButBetter.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BLOCK_PAINTED = SOUNDS.register("block_painted",
            () -> SoundEvent.createVariableRangeEvent(BuildingButBetter.location("block_painted")));

    private ModSounds() {
    }

    public static void init() {
    }
}
