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
import net.minecraft.util.RandomSource;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public final class SanityHudOverlay implements LeftHudStack.Gauge {
    private static final int GAUGE_HEIGHT = 78;
    private static final int TEX = TTScreenTextures.TEX_SIZE;
    private static final float FULL_WARP = 100.0F;

    private static final int PANEL_X = 1;
    private static final int PANEL_Y = 1;
    private static final int PANEL_U = 176;
    private static final int PANEL_V = 0;
    private static final int PANEL_W = 20;
    private static final int PANEL_H = 76;

    private static final int WELL_X = 7;
    private static final int WELL_Y = 21;
    private static final int WELL_W = 8;
    private static final int WELL_H = 48;
    private static final int FILL_U = 200;
    private static final int FILL_V = 0;

    private static final int TEMPORARY_TINT = 0xFFE7B5F2;
    private static final int NORMAL_TINT = 0xFFA24BDB;
    private static final int PERMANENT_TINT = 0xFF4B1675;

    private static final int GLYPH_U = 216;
    private static final int GLYPH_V = 0;
    private static final int GLYPH_W = 20;
    private static final int GLYPH_H = 16;
    private static final float GLYPH_X = 1.0F;
    private static final float GLYPH_Y = 3.0F;
    private static final float GLYPH_SCALE = 0.75F;
    private static final float GLYPH_TREMBLE = 0.6F;

    public SanityHudOverlay() {}

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        return mc.screen == null && mc.mouseHandler.isMouseGrabbed() && (player.getMainHandItem().is(TTItems.SANITY_CHECKER) || player.getOffhandItem().is(TTItems.SANITY_CHECKER));
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
        int temporary = Math.max(0, warp.get(WarpType.TEMPORARY));
        int normal = Math.max(0, warp.get(WarpType.NORMAL));
        int permanent = Math.max(0, warp.get(WarpType.PERMANENT));
        int total = temporary + normal + permanent;
        if (total > 0) {
            drawBands(graphics, temporary, normal, permanent, total);
        }
        drawPanel(graphics);
        if (total >= FULL_WARP) {
            drawWarning(graphics, player.getRandom());
        }
    }

    private static void drawPanel(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, PANEL_X, PANEL_Y, PANEL_U, PANEL_V, PANEL_W, PANEL_H, PANEL_W, PANEL_H, TEX, TEX);
    }

    private static void drawBands(GuiGraphicsExtractor graphics, int temporary, int normal, int permanent, int total) {
        float squeeze = total > FULL_WARP ? FULL_WARP / total : 1.0F;
        int empty = total < FULL_WARP ? (int) ((FULL_WARP - total) / FULL_WARP * WELL_H) : 0;
        int temporaryRows = rows(temporary, squeeze);
        int normalRows = rows(normal, squeeze);
        int permanentRows = rows(permanent, squeeze);
        int leftover = WELL_H - empty - temporaryRows - normalRows - permanentRows;
        if (permanent > 0) {
            permanentRows += leftover;
        } else if (normal > 0) {
            normalRows += leftover;
        } else {
            temporaryRows += leftover;
        }
        int top = empty;
        top = drawBand(graphics, top, temporaryRows, TEMPORARY_TINT);
        top = drawBand(graphics, top, normalRows, NORMAL_TINT);
        drawBand(graphics, top, permanentRows, PERMANENT_TINT);
    }

    private static int rows(int amount, float squeeze) {
        return (int) (amount * squeeze / FULL_WARP * WELL_H);
    }

    private static int drawBand(GuiGraphicsExtractor graphics, int top, int rows, int tint) {
        if (rows <= 0) {
            return top;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, WELL_X, WELL_Y + top, FILL_U, FILL_V + top, WELL_W, rows, WELL_W, rows, TEX, TEX, tint);
        return top + rows;
    }

    private static void drawWarning(GuiGraphicsExtractor graphics, RandomSource random) {
        float jitterX = (random.nextFloat() - random.nextFloat()) * GLYPH_TREMBLE;
        float jitterY = (random.nextFloat() - random.nextFloat()) * GLYPH_TREMBLE;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(GLYPH_X + jitterX, GLYPH_Y + jitterY);
        pose.scale(GLYPH_SCALE, GLYPH_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, 0, 0, GLYPH_U, GLYPH_V, GLYPH_W, GLYPH_H, GLYPH_W, GLYPH_H, TEX, TEX);
        pose.popMatrix();
    }
}
