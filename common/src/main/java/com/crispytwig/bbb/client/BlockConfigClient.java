package com.crispytwig.bbb.client;

import com.crispytwig.bbb.common.config.BlockConfig;
import com.crispytwig.bbb.common.mixin.client.CreativeModeTabsAccessor;
import com.crispytwig.bbb.common.network.BlockConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTabs;

public final class BlockConfigClient {
    private BlockConfigClient() {
    }

    public static void handle(BlockConfigPayload payload) {
        BlockConfig.setFromServer(payload.disabledGroups());
        rebuildTabs();
    }

    public static void clear() {
        BlockConfig.clearFromServer();
        rebuildTabs();
    }

    public static void rebuildTabs() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player == null) {
                return;
            }
            CreativeModeTabsAccessor.bbb$setParameters(null);
            CreativeModeTabs.tryRebuildTabContents(
                    minecraft.player.connection.enabledFeatures(),
                    minecraft.player.canUseGameMasterBlocks() || minecraft.options.operatorItemsTab().get(),
                    minecraft.player.level().registryAccess());

            Screen screen = minecraft.screen;
            if (screen instanceof CreativeModeInventoryScreen) {
                screen.resize(minecraft, screen.width, screen.height);
            }
        });
    }
}
