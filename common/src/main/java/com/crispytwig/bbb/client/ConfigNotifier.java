package com.crispytwig.bbb.client;

import com.crispytwig.bbb.common.config.BlockConfig;
import com.crispytwig.bbb.common.config.BlockGroup;
import com.crispytwig.bbb.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Set;

public final class ConfigNotifier {

    private static long lastPolled;
    private static Snapshot last;

    private record Snapshot(Set<BlockGroup> disabled, boolean needsRestart) {
    }

    private ConfigNotifier() {
    }

    public static void clear() {
        last = null;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !minecraft.hasSingleplayerServer()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPolled < 1000L) {
            return;
        }
        lastPolled = now;

        Snapshot current = new Snapshot(BlockConfig.disabledInConfig(), Services.CONFIG.needsRestart());
        Snapshot previous = last;
        last = current;
        if (previous == null || current.equals(previous)) {
            return;
        }

        boolean groupsChanged = !current.disabled().equals(previous.disabled());
        if (groupsChanged) {
            BlockConfig.clearFromServer();
            BlockConfigClient.rebuildTabs();
        }

        player.sendSystemMessage(Component.translatable("bbb.config.reloaded").withStyle(ChatFormatting.YELLOW));
        if (groupsChanged) {
            player.sendSystemMessage(Component.translatable("bbb.config.reloaded.recipes").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            player.sendSystemMessage(Component.translatable("bbb.config.reloaded.rejoin").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (current.needsRestart()) {
            player.sendSystemMessage(Component.translatable("bbb.config.reloaded.restart").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
