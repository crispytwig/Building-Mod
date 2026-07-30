package com.crispytwig.artisanal.client.model;

import com.crispytwig.artisanal.entity.util.SmoothAnimationState;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class ArtisanalEntityModel<E extends Entity> extends HierarchicalModel<E> {
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final Map<String, Optional<ModelPart>> partsByName = new HashMap<>();
    private ModelPart[] allParts;

    public ArtisanalEntityModel() {
        super();
    }

    public ArtisanalEntityModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    protected String getRootPartName() {
        return "root";
    }

    @Override
    public @NotNull Optional<ModelPart> getAnyDescendantWithName(String name) {
        Optional<ModelPart> cached = this.partsByName.get(name);
        if (cached != null) {
            return cached;
        }
        Optional<ModelPart> resolved = this.root().getAllParts()
                .filter(part -> part.hasChild(name))
                .findFirst()
                .map(part -> part.getChild(name));
        if (resolved.isEmpty() && name.equals(this.getRootPartName())) {
            resolved = Optional.of(this.root());
        }
        this.partsByName.put(name, resolved);
        return resolved;
    }

    protected void resetPose() {
        if (this.allParts == null) {
            this.allParts = this.root().getAllParts().toArray(ModelPart[]::new);
        }
        for (ModelPart part : this.allParts) {
            part.resetPose();
        }
    }

    @Override
    public final void setupAnim(E entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.resetPose();
        this.setupAnimations(entity, limbSwing, limbSwingAmount, ageInTicks, ageInTicks - entity.tickCount, netHeadYaw, headPitch);
    }

    protected abstract void setupAnimations(E entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch);

    protected static void applyHeadLook(ModelPart part, float netHeadYaw, float headPitch) {
        part.xRot += headPitch * Mth.DEG_TO_RAD;
        part.yRot += netHeadYaw * Mth.DEG_TO_RAD;
    }

    protected static float movementAnimationSpeed(float limbSwingAmount, float baseSpeed, double referenceLimbSwing) {
        return baseSpeed * Mth.clamp(limbSwingAmount / (float) Math.max(referenceLimbSwing, 0.05D), 0.4F, 2.0F);
    }

    protected void animateSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks, float partialTick) {
        this.animateSmooth(state, definition, ageInTicks, partialTick, 1.0F);
    }

    protected void animateSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks, float partialTick, float speed) {
        float factor = state.factor(partialTick);
        if (factor <= SmoothAnimationState.ACTIVE_THRESHOLD) {
            return;
        }
        state.updateTime(ageInTicks, speed);
        KeyframeAnimations.animate(this, definition, state.getAccumulatedTime(), factor, ANIMATION_VECTOR_CACHE);
    }

    protected void animateIdleSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks, float partialTick, float limbSwingAmount) {
        this.animateIdleSmooth(state, definition, ageInTicks, partialTick, limbSwingAmount, 2.5F, 1.0F);
    }

    protected void animateIdleSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks, float partialTick, float limbSwingAmount, float animationScaleFactor, float speed) {
        float factor = state.factor(partialTick) * (1.0F - Math.min(limbSwingAmount * animationScaleFactor, 1.0F));
        if (factor <= SmoothAnimationState.ACTIVE_THRESHOLD) {
            return;
        }
        state.updateTime(ageInTicks, speed);
        KeyframeAnimations.animate(this, definition, state.getAccumulatedTime(), factor, ANIMATION_VECTOR_CACHE);
    }
}
