package com.crispytwig.artisanal.entity.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

// Credit to https://github.com/platypushasnohat/Unusual-Prehistory-2/blob/main/src/main/java/com/barl_inc/unusual_prehistory/entity/utils/SmoothAnimationState.java
public class SmoothAnimationState extends AnimationState {
    public static final float ACTIVE_THRESHOLD = 0.05F;

    private final float lerpSpeed;
    private float factorOld;
    private float factor;

    public SmoothAnimationState() {
        this(0.5F);
    }

    public SmoothAnimationState(float lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    public static SmoothAnimationState pose() {
        return new SmoothAnimationState(0.25F);
    }

    public static SmoothAnimationState instant() {
        return new SmoothAnimationState(1.0F);
    }

    @Override
    public void animateWhen(boolean condition, int tickCount) {
        this.factorOld = this.factor;
        this.factor = Mth.clamp(this.factor + ((condition ? 1.0F : 0.0F) - this.factor) * this.lerpSpeed, 0.0F, 1.0F);
        if (condition) {
            this.startIfStopped(tickCount);
        } else if (this.factor < 0.001F) {
            this.stop();
        }
    }

    public float factor(float partialTick) {
        return Mth.lerp(partialTick, this.factorOld, this.factor);
    }
}
