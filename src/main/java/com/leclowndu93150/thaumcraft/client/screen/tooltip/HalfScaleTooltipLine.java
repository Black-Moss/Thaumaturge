package com.leclowndu93150.thaumcraft.client.screen.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;

public final class HalfScaleTooltipLine implements ClientTooltipComponent {
    public static final String PREFIX = "@@";
    private static final float SCALE = 0.5F;
    private static final int LINE_HEIGHT = 7;

    private final FormattedCharSequence text;

    public HalfScaleTooltipLine(FormattedCharSequence text) {
        this.text = text;
    }

    @Override
    public int getWidth(Font font) {
        return Math.round(font.width(text) * SCALE);
    }

    @Override
    public int getHeight(Font font) {
        return LINE_HEIGHT;
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(SCALE, SCALE);
        graphics.text(font, text, 0, 0, -1, true);
        graphics.pose().popMatrix();
    }
}
