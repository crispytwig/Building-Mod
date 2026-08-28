package com.crispytwig.bbb.client.screen;

import com.crispytwig.bbb.common.BuildingButBetter;
import com.crispytwig.bbb.common.menu.SofaMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SofaScreen extends AbstractContainerScreen<SofaMenu> {
    private static final ResourceLocation TEXTURE = BuildingButBetter.location("textures/gui/sofa_crevice.png");

    public SofaScreen(SofaMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 138;
        inventoryLabelY = imageHeight - 94;
        titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
