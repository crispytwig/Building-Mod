package com.crispytwig.artisanal.entity;

import com.crispytwig.artisanal.entity.util.SmoothAnimationState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Wright extends Allay {
    public final SmoothAnimationState idleAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState flyAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState holdItemAnimationState = SmoothAnimationState.pose();

    public Wright(EntityType<? extends Wright> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Allay.createAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }
    }

    @Override
    public int getMaxHeadYRot() {
        return 40;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (player.getItemInHand(hand).is(Items.AMETHYST_SHARD)) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    private void setupAnimationStates() {
        boolean moving = this.walkAnimation.speed() > 0.15F;

        this.flyAnimationState.animateWhen(moving, this.tickCount);
        this.idleAnimationState.animateWhen(!moving, this.tickCount);
        this.holdItemAnimationState.animateWhen(this.hasItemInHand(), this.tickCount);
    }
}
