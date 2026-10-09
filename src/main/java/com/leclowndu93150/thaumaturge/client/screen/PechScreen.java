package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTGauge;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class PechScreen extends AbstractTTContainerScreen<MenuPech> {
    private static final Identifier SHEET = TTIds.rl("textures/gui/gui_pech.png");
    private static final int SHEET_SIZE = 256;
    private static final int IMAGE_WIDTH = 175;
    private static final int IMAGE_HEIGHT = 232;
    private static final int LEFT_BUTTON = 0;

    private static final int TRADE_X = 67;
    private static final int TRADE_Y = 24;
    private static final int TRADE_SIZE = 25;
    private static final int TRADE_U = 176;
    private static final int TRADE_V = 0;
    private static final TTGauge TRADE_HIGHLIGHT = new TTGauge(SHEET, SHEET_SIZE, TRADE_U, TRADE_V, TRADE_SIZE, TRADE_SIZE);

    private static final float DICE_VOLUME = 0.5F;
    private static final float DICE_PITCH_BASE = 0.95F;
    private static final float DICE_PITCH_SPREAD = 0.1F;

    public PechScreen(MenuPech menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SHEET, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (menu.canTrade()) {
            TRADE_HIGHLIGHT.extractFull(graphics, leftPos + TRADE_X, topPos + TRADE_Y);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == LEFT_BUTTON && minecraft != null && minecraft.gameMode != null && minecraft.player != null && menu.canTrade() && overTradeButton(event.x(), event.y())) {
            LocalPlayer player = minecraft.player;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuPech.TRADE_BUTTON_ID);
            player.playSound(TTSounds.PECH_DICE.get(), DICE_VOLUME, DICE_PITCH_BASE + player.getRandom().nextFloat() * DICE_PITCH_SPREAD);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean overTradeButton(double mouseX, double mouseY) {
        double relX = mouseX - (leftPos + TRADE_X);
        double relY = mouseY - (topPos + TRADE_Y);
        return relX >= 0 && relY >= 0 && relX < TRADE_SIZE && relY < TRADE_SIZE;
    }
}
