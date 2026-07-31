package com.crispytwig.artisanal.entity;

import com.crispytwig.artisanal.entity.util.SmoothAnimationState;
import com.crispytwig.artisanal.item.ArchitectsScepterItem;
import com.crispytwig.artisanal.item.ScepterOccupant;
import com.crispytwig.artisanal.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Wright extends Allay {
    
    //region Data
    private static final byte SWING_EVENT = 100;

    public final SmoothAnimationState idleAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState flyAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState holdItemAnimationState = SmoothAnimationState.pose();
    public final AnimationState swingAnimationState = new AnimationState();

    private final List<BlockPos> buildQueue = new ArrayList<>();
    @Nullable
    private UUID buildOwner;
    private int placeCooldown;

    public Wright(EntityType<? extends Wright> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Allay.createAttributes();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.isBuilding()) {
            tag.putLongArray("BuildQueue", this.buildQueue.stream().mapToLong(BlockPos::asLong).toArray());
        }
        if (this.buildOwner != null) {
            tag.putUUID("BuildOwner", this.buildOwner);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.buildQueue.clear();
        for (long packed : tag.getLongArray("BuildQueue")) {
            this.buildQueue.add(BlockPos.of(packed));
        }
        this.buildOwner = tag.hasUUID("BuildOwner") ? tag.getUUID("BuildOwner") : null;
    }
    //endregion

    //region Behavior
    public static int estimateBuildTicks(int blocks) {
        return 20 + blocks * 5;
    }

    public boolean isBuilding() {
        return !this.buildQueue.isEmpty();
    }

    public void startBuild(UUID owner, List<BlockPos> plan, ItemStack payload) {
        this.buildOwner = owner;
        this.buildQueue.clear();
        this.buildQueue.addAll(plan);
        this.setItemInHand(InteractionHand.MAIN_HAND, payload);
        this.setPersistenceRequired();
    }

    public boolean cancelBuild(Player player) {
        if (!this.isBuilding()) {
            return false;
        }
        this.releasePayload(player);
        return true;
    }

    private void releasePayload(@Nullable Player recipient) {
        this.buildQueue.clear();
        this.buildOwner = null;
        ItemStack leftover = this.getMainHandItem().copy();
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (leftover.isEmpty()) {
            return;
        }
        if (recipient == null) {
            this.spawnAtLocation(leftover);
        } else {
            recipient.getInventory().placeItemBackInInventory(leftover);
        }
    }

    private static float yawTo(double x, double z) {
        return (float) (Mth.atan2(z, x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    @Override
    public void tick() {
        this.noPhysics = this.isBuilding();
        super.tick();
        this.noPhysics = false;

        if (this.level().isClientSide) {
            this.setupAnimationStates();
        } else if (this.isBuilding()) {
            this.tickBuild();
        }
    }

    @Override
    protected void customServerAiStep() {
        if (!this.isBuilding()) {
            super.customServerAiStep();
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

    private void tickBuild() {
        ItemStack held = this.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem)) {
            this.finishBuild();
            return;
        }

        BlockPos next = this.buildQueue.getFirst();
        Vec3 center = Vec3.atCenterOf(next);
        Vec3 offset = center.add(0.0, 1.1, 0.0).subtract(this.position());

        this.setXxa(0.0F);
        this.setYya(0.0F);
        this.setZza(0.0F);
        Vec3 toTarget = center.subtract(this.getEyePosition());
        double horizontal = toTarget.horizontalDistance();
        float lookYaw = horizontal > 0.1 ? yawTo(toTarget.x, toTarget.z) : this.yBodyRot;
        this.setYHeadRot(Mth.approachDegrees(this.yBodyRot, lookYaw, this.getMaxHeadYRot()));
        this.setXRot((float) (-Mth.atan2(toTarget.y, horizontal) * Mth.RAD_TO_DEG));

        Vec3 heading = offset.multiply(1.0, 0.0, 1.0);
        if (heading.lengthSqr() < 0.04 && this.buildQueue.size() > 1) {
            heading = Vec3.atCenterOf(this.buildQueue.get(1)).subtract(center).multiply(1.0, 0.0, 1.0);
        }
        if (heading.lengthSqr() > 1.0E-4) {
            this.setYRot(Mth.rotLerp(0.4F, this.getYRot(), yawTo(heading.x, heading.z)));
            this.yBodyRot = this.getYRot();
        }

        double distance = offset.length();
        if (distance > 0.2) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.85).add(offset.scale(0.05 / distance)));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.6));
        }

        if (this.placeCooldown > 0) {
            this.placeCooldown--;
            return;
        }

        if (this.distanceToSqr(center) > 2.2 * 2.2) {
            return;
        }

        this.buildQueue.removeFirst();
        this.placeCooldown = 4;
        if (blockItem.place(new DirectionalPlaceContext(this.level(), next, this.getDirection(), held, Direction.UP)).consumesAction()) {
            this.level().broadcastEntityEvent(this, SWING_EVENT);
        }

        if (this.buildQueue.isEmpty()) {
            this.finishBuild();
        }
    }

    private void finishBuild() {
        Player owner = this.buildOwner == null ? null : this.level().getPlayerByUUID(this.buildOwner);
        this.releasePayload(owner);

        ItemStack scepter = owner == null ? ItemStack.EMPTY : ArchitectsScepterItem.findLinked(owner, this.getUUID());
        if (scepter.isEmpty()) {
            return;
        }

        scepter.remove(ModDataComponents.BUILDER.get());
        scepter.set(ModDataComponents.CAPTURED_ENTITY.get(), ScepterOccupant.WRIGHT);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.3, this.getZ(), 8, 0.15, 0.15, 0.15, 0.01);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 1.0F);
        this.discard();
    }
    //endregion

    //region Animation
    @Override
    public void handleEntityEvent(byte id) {
        if (id == SWING_EVENT) {
            this.swingAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.walkAnimation.speed() > 0.15F;

        this.flyAnimationState.animateWhen(moving, this.tickCount);
        this.idleAnimationState.animateWhen(!moving, this.tickCount);
        this.holdItemAnimationState.animateWhen(this.hasItemInHand(), this.tickCount);
    }
    //endregion
}
