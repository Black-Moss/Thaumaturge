package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AspectListWidget {
    private static final int CHIP_SIZE = 16;
    private static final int CHIP_STRIDE = 18;

    private AspectListWidget() {}

    public static int height() {
        return CHIP_SIZE + 2;
    }

    public static int width(AspectList aspects) {
        return Math.max(0, aspects.size() * CHIP_STRIDE - (CHIP_STRIDE - CHIP_SIZE));
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int x, int y, AspectList aspects) {
        int chipX = x;
        for (AspectInstance entry : aspects.sortedByAmount()) {
            AspectTagRenderer.render(graphics, font, chipX, y, entry.aspect(), entry.amount());
            chipX += CHIP_STRIDE;
        }
    }
}
