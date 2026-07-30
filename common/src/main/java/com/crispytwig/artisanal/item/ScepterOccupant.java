package com.crispytwig.artisanal.item;

import com.crispytwig.artisanal.entity.Wright;
import com.crispytwig.artisanal.registry.ModEntityTypes;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public enum ScepterOccupant implements StringRepresentable {
    ALLAY("allay", 0.5F, () -> EntityType.ALLAY),
    WRIGHT("wright", 1.0F, ModEntityTypes.WRIGHT);

    public static final Codec<ScepterOccupant> CODEC = StringRepresentable.fromEnum(ScepterOccupant::values);
    public static final StreamCodec<ByteBuf, ScepterOccupant> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private final String name;
    private final float predicateValue;
    private final Supplier<? extends EntityType<?>> type;

    ScepterOccupant(String name, float predicateValue, Supplier<? extends EntityType<?>> type) {
        this.name = name;
        this.predicateValue = predicateValue;
        this.type = type;
    }

    @Nullable
    public static ScepterOccupant of(Entity entity) {
        if (entity instanceof Wright) {
            return WRIGHT;
        }
        return entity.getType() == EntityType.ALLAY ? ALLAY : null;
    }

    public EntityType<?> getEntityType() {
        return this.type.get();
    }

    public Component getDisplayName() {
        return this.getEntityType().getDescription();
    }

    public float getPredicateValue() {
        return this.predicateValue;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
