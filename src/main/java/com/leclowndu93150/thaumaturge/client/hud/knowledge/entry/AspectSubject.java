package com.leclowndu93150.thaumaturge.client.hud.knowledge.entry;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;

public record AspectSubject(Holder<IAspect> aspect) implements GainSubject {
    private static final int ICON_SIZE = 16;
    private static final int ICON_ORIGIN = -ICON_SIZE / 2;
    private static final int ASPECT_ICON_TEX = 32;

    @Override
    public void drawIcon(GuiGraphicsExtractor graphics, ClientLevel level) {
        IAspect value = aspect.value();
        graphics.blit(RenderPipelines.GUI_TEXTURED, value.texture(), ICON_ORIGIN, ICON_ORIGIN, 0, 0, ICON_SIZE, ICON_SIZE, ASPECT_ICON_TEX, ASPECT_ICON_TEX, ASPECT_ICON_TEX, ASPECT_ICON_TEX,
                ARGB.opaque(value.color()));
    }
}
