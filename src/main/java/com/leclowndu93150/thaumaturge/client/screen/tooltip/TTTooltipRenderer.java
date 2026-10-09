package com.leclowndu93150.thaumaturge.client.screen.tooltip;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;

public final class TTTooltipRenderer {
    private static final int PLAIN_LINE_MAX_WIDTH = 200;
    private static final int HALF_LINE_MAX_WIDTH = 400;

    private TTTooltipRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, Font font, List<TooltipLine> lines, int x, int y) {
        if (lines.isEmpty())
            return;
        List<ClientTooltipComponent> built = new ArrayList<>();
        for (TooltipLine line : lines) {
            if (line.scale() == TooltipLineScale.HALF) {
                for (FormattedCharSequence part : font.split(line.text(), HALF_LINE_MAX_WIDTH)) {
                    built.add(new HalfScaleTooltipLine(part));
                }
            } else {
                for (FormattedCharSequence part : font.split(line.text(), PLAIN_LINE_MAX_WIDTH)) {
                    built.add(new ClientTextTooltip(part));
                }
            }
        }
        graphics.tooltip(font, built, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }
}
