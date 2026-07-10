package com.leclowndu93150.thaumcraft.client.tooltip;

import com.leclowndu93150.thaumcraft.api.aspect.AspectChipsTooltip;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public final class AspectChipsClientTooltip implements ClientTooltipComponent {
    private static final int CHIP_SIZE = 16;
    private static final int TEXTURE_SIZE = 32;
    private static final int CHIP_STRIDE = 18;
    private static final int ROW_HEIGHT = 18;
    private static final int AMOUNT_OFFSET_X = 16;
    private static final int AMOUNT_OFFSET_Y = 9;

    private final AspectList aspects;

    public AspectChipsClientTooltip(AspectChipsTooltip carrier) {
        this.aspects = carrier.aspects();
    }

    @Override
    public int getHeight(Font font) {
        return aspects.isEmpty() ? 0 : ROW_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        if (aspects.isEmpty()) {
            return 0;
        }
        return aspects.size() * CHIP_STRIDE;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        int chipX = x;
        for (AspectInstance entry : aspects.sortedByAmount()) {
            AspectTagRenderer.render(graphics, font, chipX, y, entry.aspect(), entry.amount());
            chipX += CHIP_STRIDE;
        }
    }
}
