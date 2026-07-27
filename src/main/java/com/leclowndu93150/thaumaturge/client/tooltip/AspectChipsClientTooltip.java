package com.leclowndu93150.thaumaturge.client.tooltip;

import com.leclowndu93150.thaumaturge.api.aspect.AspectChipsTooltip;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.player.Player;

public final class AspectChipsClientTooltip implements ClientTooltipComponent {
    private static final int CHIP_STRIDE = 18;
    private static final int ROW_HEIGHT = 18;

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
        Player player = Minecraft.getInstance().player;
        int chipX = x;
        for (AspectInstance entry : aspects.sortedByAmount()) {
            if (player != null && AspectPools.isDiscovered(player, entry.aspect())) {
                AspectTagRenderer.render(graphics, font, chipX, y, entry.aspect(), entry.amount());
            } else {
                AspectTagRenderer.renderUnknownChip(graphics, chipX, y, entry.aspect());
            }
            chipX += CHIP_STRIDE;
        }
    }
}
