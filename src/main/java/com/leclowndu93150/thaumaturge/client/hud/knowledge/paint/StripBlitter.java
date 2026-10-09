package com.leclowndu93150.thaumaturge.client.hud.knowledge.paint;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class StripBlitter {
    private static final int FRAME_SIZE = 16;
    private static final int STRIP_WIDTH = FRAME_SIZE * ParticleTextures.STAR_GLINT_FRAMES;
    private static final int FRAME_ORIGIN = -FRAME_SIZE / 2;

    private StripBlitter() {}

    public static void draw(GuiGraphicsExtractor graphics, Identifier strip, float x, float y, float size, float radians, int frame, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().rotate(radians);
        graphics.pose().scale(size / FRAME_SIZE, size / FRAME_SIZE);
        graphics.blit(TTRenderPipelines.GUI_TEXTURED_ADDITIVE, strip, FRAME_ORIGIN, FRAME_ORIGIN, frame * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, FRAME_SIZE, STRIP_WIDTH, FRAME_SIZE,
                color);
        graphics.pose().popMatrix();
    }
}
