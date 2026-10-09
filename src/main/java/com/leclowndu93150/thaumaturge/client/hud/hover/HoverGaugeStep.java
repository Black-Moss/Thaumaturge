package com.leclowndu93150.thaumaturge.client.hud.hover;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface HoverGaugeStep {
    void draw(GuiGraphicsExtractor graphics, HoverGaugeState state);
}
