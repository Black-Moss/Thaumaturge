package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

public final class SanityHudOverlay implements LeftHudStack.Gauge {
    private static final int GAUGE_HEIGHT = 78;
    private static final int TEX = TTScreenTextures.TEX_SIZE;
    private static final float WARP_CAP = 100.0F;

    private static final int FRAME_X = 1;
    private static final int FRAME_Y = 1;
    private static final int FRAME_U = 152;
    private static final int OVERLAY_U = 176;
    private static final int PANEL_V = 0;
    private static final int PANEL_W = 20;
    private static final int PANEL_H = 76;

    private static final int WELL_X = 7;
    private static final int WELL_Y = 21;
    private static final int WELL_HEIGHT = 48;
    private static final int WELL_ART_U = 200;
    private static final int WELL_W = 8;

    private static final int TEMPORARY_TINT = ARGB.colorFromFloat(1.0F, 1.0F, 0.5F, 1.0F);
    private static final int NORMAL_TINT = ARGB.colorFromFloat(1.0F, 0.75F, 0.0F, 0.75F);
    private static final int PERMANENT_TINT = ARGB.colorFromFloat(1.0F, 0.5F, 0.0F, 0.5F);

    public SanityHudOverlay() {}

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        return player.getMainHandItem().is(TTItems.SANITY_CHECKER) || player.getOffhandItem().is(TTItems.SANITY_CHECKER);
    }

    @Override
    public int height() {
        return GAUGE_HEIGHT;
    }

    @Override
    public @Nullable String exclusiveGroup() {
        return null;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        IPlayerWarp warp = WarpHelper.getWarp(player);
        int[] held = {warp.get(WarpType.TEMPORARY), warp.get(WarpType.NORMAL), warp.get(WarpType.PERMANENT)};
        int temporary = held[0];
        int normal = held[1];
        int permanent = held[2];
        float sum = temporary + normal + permanent;
        float shrink = sum > WARP_CAP ? WARP_CAP / sum : 1.0F;
        int cursor = (int) ((WARP_CAP - Math.min(sum, WARP_CAP)) / WARP_CAP * WELL_HEIGHT);

        drawPanel(graphics, FRAME_U);
        cursor = fillBand(graphics, temporary, cursor, scaledHeight(temporary, shrink), TEMPORARY_TINT);
        cursor = fillBand(graphics, normal, cursor, scaledHeight(normal, shrink), NORMAL_TINT);
        fillBand(graphics, permanent, cursor, WELL_HEIGHT - cursor, PERMANENT_TINT);
        drawPanel(graphics, OVERLAY_U);
    }

    private static int scaledHeight(int amount, float shrink) {
        return (int) (amount / WARP_CAP * WELL_HEIGHT * shrink);
    }

    private static void drawPanel(GuiGraphicsExtractor graphics, int u) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, FRAME_X, FRAME_Y, u, PANEL_V, PANEL_W, PANEL_H, PANEL_W, PANEL_H, TEX, TEX);
    }

    private static int fillBand(GuiGraphicsExtractor graphics, int amount, int row, int rows, int tint) {
        if (amount > 0 && rows > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, WELL_X, WELL_Y + row, WELL_ART_U, row, WELL_W, rows, WELL_W, rows, TEX, TEX, tint);
        }
        return row + rows;
    }
}
