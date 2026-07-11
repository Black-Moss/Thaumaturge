package com.leclowndu93150.thaumcraft.client.tooltip;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectChipsTooltip;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.content.research.pool.AspectPools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;

public final class AspectChipsClientTooltip implements ClientTooltipComponent {
    private static final Identifier UNKNOWN_TEXTURE = TCIds.rl("textures/aspects/_unknown.png");
    private static final int CHIP_SIZE = 16;
    private static final int TEXTURE_SIZE = 32;
    private static final int CHIP_STRIDE = 18;
    private static final int ROW_HEIGHT = 18;
    private static final int AMOUNT_OFFSET_X = 16;
    private static final int AMOUNT_OFFSET_Y = 9;
    private static final float UNKNOWN_ALPHA = 0.75F;

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
                int tint = ARGB.colorFromFloat(UNKNOWN_ALPHA, 1.0F, 1.0F, 1.0F);
                int color = entry.aspect().value() != null
                        ? (tint & 0xFF000000) | (entry.aspect().value().color() & 0x00FFFFFF)
                        : tint;
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_TEXTURE, chipX, y,
                        0.0F, 0.0F, CHIP_SIZE, CHIP_SIZE,
                        TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color);
            }
            chipX += CHIP_STRIDE;
        }
    }
}
