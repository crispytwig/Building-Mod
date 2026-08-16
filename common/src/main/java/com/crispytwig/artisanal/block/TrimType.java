package com.crispytwig.artisanal.block;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum TrimType implements StringRepresentable {
    SINGLE(false, false),
    BOTTOM(false, true),
    MIDDLE(true, true),
    TOP(true, false);

    private final boolean negative;
    private final boolean positive;

    TrimType(boolean negative, boolean positive) {
        this.negative = negative;
        this.positive = positive;
    }

    public static TrimType of(boolean negative, boolean positive) {
        if (negative) {
            return positive ? MIDDLE : TOP;
        }
        return positive ? BOTTOM : SINGLE;
    }

    public boolean isConnected(Direction.AxisDirection direction) {
        return direction == Direction.AxisDirection.POSITIVE ? positive : negative;
    }

    public TrimType with(Direction.AxisDirection direction, boolean connected) {
        return direction == Direction.AxisDirection.POSITIVE ? of(negative, connected) : of(connected, positive);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
