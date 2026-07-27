package com.leclowndu93150.thaumaturge.client.render.research;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class StageNavigatorWidget {
    private static final int TEXT_COLOR = 0xFF402010;

    private StageNavigatorWidget() {}

    public static int height() {
        return 12;
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int x,
            int y,
            int width,
            int currentStage,
            int totalStages
    ) {
        String label = (currentStage + 1) + " / " + totalStages;
        int labelWidth = font.width(label);
        graphics.text(font, Component.literal(label),
                x + width / 2 - labelWidth / 2,
                y + 2,
                TEXT_COLOR,
                false);
    }
}
