package com.crispytwig.bbb.client.renderer;

import net.minecraft.client.Minecraft;

public final class ViewDistance {
    private ViewDistance() {
    }

    public static int blocks() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
