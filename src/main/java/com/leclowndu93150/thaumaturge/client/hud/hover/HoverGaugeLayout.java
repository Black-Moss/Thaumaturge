package com.leclowndu93150.thaumaturge.client.hud.hover;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import net.minecraft.util.ARGB;

public record HoverGaugeLayout(GaugePart fill, GaugePart frame, GaugePart spark, Anchor icon, int sparkFrames) {
    private static final int SHEET = TTScreenTextures.TEX_SIZE;
    private static final int NO_TINT = ARGB.white(1.0F);

    public static final HoverGaugeLayout STANDARD = new HoverGaugeLayout(Fill.part(), Frame.part(), Spark.part(), Icon.ANCHOR, Spark.FRAMES);

    private static final class Fill {
        static final int MAX_HEIGHT = 48;
        static final int X = 6;
        static final int BOTTOM_OFFSET = 24;
        static final int U = 0;
        static final int V = 72;
        static final int WIDTH = 8;
        static final int TINT = ARGB.colorFromFloat(1.0F, 0.0F, 1.0F, 0.75F);

        static GaugePart part() {
            return new GaugePart(TTScreenTextures.HUD, new SourceRect(U, V, WIDTH, MAX_HEIGHT), new Anchor(X, BOTTOM_OFFSET), SHEET, SHEET, TINT);
        }
    }

    private static final class Frame {
        static final int X = 4;
        static final int TOP_OFFSET = -28;
        static final int U = 14;
        static final int V = 72;
        static final int WIDTH = 12;
        static final int HEIGHT = 56;

        static GaugePart part() {
            return new GaugePart(TTScreenTextures.HUD, new SourceRect(U, V, WIDTH, HEIGHT), new Anchor(X, TOP_OFFSET), SHEET, SHEET, NO_TINT);
        }
    }

    private static final class Spark {
        static final int FRAMES = 14;
        static final int FRAME_SIZE = 16;
        static final int STRIP_WIDTH = FRAME_SIZE * FRAMES;
        static final float ALPHA = 0.66F;

        static GaugePart part() {
            return new GaugePart(TTScreenTextures.HUD_HOVER_SPARK, new SourceRect(0, 0, FRAME_SIZE, FRAME_SIZE), Icon.ANCHOR, STRIP_WIDTH, FRAME_SIZE, ARGB.white(ALPHA));
        }
    }

    private static final class Icon {
        static final int X = 2;
        static final int TOP_OFFSET = -43;
        static final Anchor ANCHOR = new Anchor(X, TOP_OFFSET);
    }

    public int fillMaxHeight() {
        return fill.source().height();
    }
}
