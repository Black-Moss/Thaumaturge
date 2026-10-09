package com.leclowndu93150.thaumaturge.client.toast;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ResearchToast implements Toast {
    private static final Identifier BACKGROUND = TTIds.rl("textures/gui/hud.png");
    private static final int SHEET_SIZE = 256;
    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 32;
    private static final float BACKGROUND_U = 0.0F;
    private static final float BACKGROUND_V = 224.0F;
    private static final int ICON_X = 6;
    private static final int ICON_Y = 8;
    private static final int TEXT_X = 30;
    private static final int HEADING_Y = 7;
    private static final int NAME_Y = 18;
    private static final int HEADING_COLOR = 0xFFA23BF1;
    private static final int NAME_COLOR = 0xFFFFAB09;
    private static final int NAME_MAX_WIDTH = 124;
    private static final long DISPLAY_TIME_MS = 5000L;

    private final Identifier research;
    private final Component heading;
    private final Component name;
    private final Object icon;
    private Visibility wantedVisibility = Visibility.SHOW;

    public ResearchToast(Identifier research, Component heading, Component name, Object icon) {
        this.research = research;
        this.heading = heading;
        this.name = name;
        this.icon = icon;
    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        wantedVisibility = fullyVisibleForMs > DISPLAY_TIME_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, BACKGROUND_U, BACKGROUND_V, CARD_WIDTH, CARD_HEIGHT, CARD_WIDTH, CARD_HEIGHT, SHEET_SIZE, SHEET_SIZE);
        EntryIconRenderer.drawResearchIcon(graphics, ICON_X, ICON_Y, icon, false);
        graphics.text(font, heading, TEXT_X, HEADING_Y, HEADING_COLOR, false);
        drawName(graphics, font);
    }

    private void drawName(GuiGraphicsExtractor graphics, Font font) {
        int width = font.width(name);
        if (width <= NAME_MAX_WIDTH) {
            graphics.text(font, name, TEXT_X, NAME_Y, NAME_COLOR, false);
            return;
        }
        float scale = (float) NAME_MAX_WIDTH / (float) width;
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) TEXT_X, (float) NAME_Y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, name, 0, 0, NAME_COLOR, false);
        graphics.pose().popMatrix();
    }

    @Override
    public Object getToken() {
        return research;
    }
}
