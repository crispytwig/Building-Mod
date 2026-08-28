package com.crispytwig.bbb.common.block;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum CrossType implements StringRepresentable {
    NONE,
    VERTICAL,
    RIGHT_CROSS,
    HORIZONTAL,
    LEFT_CROSS,
    PLUS,
    CROSS;

    public CrossType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public CrossType previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
