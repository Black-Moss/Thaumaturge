package com.leclowndu93150.thaumaturge.client.screen.research.detail.draw;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

public final class AspectTileDrawer {
    public static final Identifier UNKNOWN_TILE = TTIds.rl("textures/aspects/_unknown.png");
    public static final Identifier BACKDROP_TILE = TTIds.rl("textures/aspects/_back.png");

    private static final int UNCERTAIN_TINT = 0xFFCCCCCC;

    private AspectTileDrawer() {}

    public static void known(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        IAspect value = aspect.value();
        BookBlit.iconTile(graphics, value.texture(), x, y, BookBlit.opaque(value.color()));
    }

    public static void silhouette(GuiGraphicsExtractor graphics, Holder<IAspect> aspect) {
        BookBlit.iconTile(graphics, UNKNOWN_TILE, 0, 0, BookBlit.opaque(aspect.value().color()));
    }

    public static void unrevealed(GuiGraphicsExtractor graphics) {
        BookBlit.iconTile(graphics, UNKNOWN_TILE, 0, 0, UNCERTAIN_TINT);
    }
}
