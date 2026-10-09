package com.leclowndu93150.thaumaturge.client.screen.research.detail.draw;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BookBlit {
    private static final float SWELL_BASE = 1.0F;
    private static final float HALF = 2.0F;

    private BookBlit() {}

    public static void sprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, x, y, (float) u, (float) v, width, height, width, height, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
    }

    public static void tintedSprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, x, y, (float) u, (float) v, width, height, width, height, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE,
                tint);
    }

    public static void checkmark(GuiGraphicsExtractor graphics, int x, int y) {
        sprite(graphics, x + BookSprites.CHECKMARK_OFFSET_X, y, BookSprites.CHECKMARK_U, BookSprites.CHECKMARK_V, BookSprites.CHECKMARK_SIZE, BookSprites.CHECKMARK_SIZE);
    }

    public static void paper(GuiGraphicsExtractor graphics, int paperLeft, int paperTop) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.PAPER, paperLeft, paperTop, 0.0F, 0.0F, BookSprites.PAPER_SIZE, BookSprites.PAPER_SIZE, BookSprites.PAPER_SIZE,
                BookSprites.PAPER_SIZE, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
    }

    public static void iconTile(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int tint) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, BookSprites.ICON_TEXTURE_SIZE, BookSprites.ICON_TEXTURE_SIZE,
                BookSprites.ICON_TEXTURE_SIZE, BookSprites.ICON_TEXTURE_SIZE, tint);
    }

    public static void swellingSprite(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height, float swell) {
        float centerX = x + width / HALF;
        float centerY = y + height / HALF;
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(SWELL_BASE + swell, SWELL_BASE + swell);
        graphics.pose().translate(-width / HALF, -height / HALF);
        sprite(graphics, 0, 0, u, v, width, height);
        graphics.pose().popMatrix();
    }

    public static int alphaTint(float alpha) {
        return ((int) (alpha * BookSprites.ALPHA_RANGE)) << BookSprites.ALPHA_SHIFT | BookSprites.RGB_MASK;
    }

    public static int opaque(int color) {
        return BookSprites.OPAQUE_ALPHA | (color & BookSprites.RGB_MASK);
    }
}
