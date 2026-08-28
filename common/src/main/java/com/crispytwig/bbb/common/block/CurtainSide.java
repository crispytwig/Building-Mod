package com.crispytwig.bbb.common.block;

import net.minecraft.util.StringRepresentable;

public enum CurtainSide implements StringRepresentable {
    LEFT("left"),
    MIDDLE("middle"),
    RIGHT("right"),
    SINGLE("single");

    private final String name;

    CurtainSide(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
