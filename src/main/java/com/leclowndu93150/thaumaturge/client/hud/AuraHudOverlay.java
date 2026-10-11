package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestAuraChunkPayload;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public final class AuraHudOverlay implements LeftHudStack.Gauge {
    private static final int GAUGE_HEIGHT = 83;
    private static final int TEX = TTScreenTextures.TEX_SIZE;

    private static final int FRAME_X = 3;
    private static final int FRAME_Y = 1;
    private static final int FRAME_U = 72;
    private static final int FRAME_V = 48;
    private static final int FRAME_W = 16;
    private static final int FRAME_H = 80;

    private static final int TUBE_X = 7;
    private static final int TUBE_BOTTOM = 74;
    private static final int TUBE_W = 8;
    private static final int TUBE_ROWS = 64;

    private static final int FILL_U = 88;
    private static final int FILL_V = 56;
    private static final int FILL_PERIOD = 64;
    private static final int VIS_SHIMMER_U = 96;
    private static final int FLUX_SHIMMER_U = 104;
    private static final int SHIMMER_V = 56;
    private static final int SHIMMER_PERIOD = 128;

    private static final int POINTER_X = 4;
    private static final int POINTER_U = 117;
    private static final int POINTER_V = 61;
    private static final int POINTER_W = 14;
    private static final int POINTER_H = 5;
    private static final int POINTER_TIP_ROW = 2;

    private static final float FULL_TUBE_AURA = 520.0F;

    private static final int VIS_TINT = 0xFFC7A6EE;
    private static final int FLUX_TINT = 0xFF4B2559;
    private static final int VIS_SHIMMER_TINT = 0x5CFFF4FF;
    private static final int FLUX_SHIMMER_TINT = 0x70B37BFF;
    private static final float VIS_SHIMMER_SPEED = 0.55F;
    private static final float FLUX_SHIMMER_SPEED = -0.3F;
    private static final long SCROLL_WRAP_TICKS = 64000L;

    private static final int TEXT_X = 18;
    private static final float TEXT_SCALE = 0.5F;
    private static final int TEXT_UPSCALE = 2;
    private static final int VIS_TEXT_COLOR = 0xFFF2CDEB;
    private static final int FLUX_TEXT_COLOR = 0xFFC01C82;
    private static final int TENTHS = 10;

    public AuraHudOverlay() {}

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        if (mc.screen != null || !mc.mouseHandler.isMouseGrabbed() || !readsAura(player)) {
            return false;
        }
        ChunkPos chunk = player.chunkPosition();
        if (ClientAuraCache.shouldRequest(chunk)) {
            ClientPacketDistributor.sendToServer(new ServerboundRequestAuraChunkPayload(chunk.x(), chunk.z()));
        }
        return snapshotFor(player) != null;
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
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            return;
        }
        ClientAuraCache.Snapshot snapshot = snapshotFor(player);
        if (snapshot == null) {
            return;
        }
        float visShare = share(snapshot.vis());
        float fluxShare = share(snapshot.flux());
        float baseShare = share(snapshot.base());
        float filled = visShare + fluxShare;
        if (filled > 1.0F) {
            visShare /= filled;
            fluxShare /= filled;
            baseShare /= filled;
        }
        int visRows = Math.round(visShare * TUBE_ROWS);
        int fluxRows = Math.min(TUBE_ROWS - visRows, Math.round(fluxShare * TUBE_ROWS));
        int visTop = TUBE_BOTTOM - visRows;
        int fluxTop = visTop - fluxRows;
        float clock = (level.getGameTime() % SCROLL_WRAP_TICKS) + deltaTracker.getGameTimeDeltaPartialTick(false);
        drawBand(graphics, visTop, visRows, VIS_TINT, VIS_SHIMMER_U, VIS_SHIMMER_TINT, clock * VIS_SHIMMER_SPEED);
        drawBand(graphics, fluxTop, fluxRows, FLUX_TINT, FLUX_SHIMMER_U, FLUX_SHIMMER_TINT, clock * FLUX_SHIMMER_SPEED);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, FRAME_X, FRAME_Y, FRAME_U, FRAME_V, FRAME_W, FRAME_H, TEX, TEX);
        int pointerTip = TUBE_BOTTOM - Math.round(baseShare * TUBE_ROWS);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, POINTER_X, pointerTip - POINTER_TIP_ROW, POINTER_U, POINTER_V, POINTER_W, POINTER_H, TEX, TEX);
        if (player.isShiftKeyDown()) {
            drawAmounts(graphics, mc.font, snapshot, visTop, fluxTop);
        }
    }

    private static boolean readsAura(LocalPlayer player) {
        return GogglesAccess.wearsRevealingGear(player) || player.getMainHandItem().is(TTItems.THAUMOMETER) || player.getOffhandItem().is(TTItems.THAUMOMETER);
    }

    private static ClientAuraCache.@Nullable Snapshot snapshotFor(LocalPlayer player) {
        ClientAuraCache.Snapshot own = ClientAuraCache.get(player.chunkPosition());
        return own != null ? own : ClientAuraCache.latest();
    }

    private static float share(float amount) {
        return Math.clamp(amount / FULL_TUBE_AURA, 0.0F, 1.0F);
    }

    private static void drawBand(GuiGraphicsExtractor graphics, int top, int rows, int tint, int shimmerU, int shimmerTint, float scroll) {
        if (rows <= 0) {
            return;
        }
        int tubeRow = top - (TUBE_BOTTOM - TUBE_ROWS);
        scrolledStrip(graphics, RenderPipelines.GUI_TEXTURED, top, rows, FILL_U, FILL_V, FILL_PERIOD, tubeRow, tint);
        int shimmerRow = Math.floorMod(tubeRow + (int) Math.floor(scroll), SHIMMER_PERIOD);
        scrolledStrip(graphics, TTRenderPipelines.GUI_TEXTURED_ADDITIVE, top, rows, shimmerU, SHIMMER_V, SHIMMER_PERIOD, shimmerRow, shimmerTint);
    }

    private static void scrolledStrip(GuiGraphicsExtractor graphics, RenderPipeline pipeline, int top, int rows, int u, int v, int period, int startRow, int tint) {
        int first = Math.min(rows, period - startRow);
        graphics.blit(pipeline, TTScreenTextures.HUD, TUBE_X, top, u, v + startRow, TUBE_W, first, TEX, TEX, tint);
        if (first < rows) {
            graphics.blit(pipeline, TTScreenTextures.HUD, TUBE_X, top + first, u, v, TUBE_W, rows - first, TEX, TEX, tint);
        }
    }

    private static void drawAmounts(GuiGraphicsExtractor graphics, Font font, ClientAuraCache.Snapshot snapshot, int visTop, int fluxTop) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(TEXT_SCALE, TEXT_SCALE);
        int textX = TEXT_X * TEXT_UPSCALE;
        graphics.text(font, amount(snapshot.vis()), textX, visTop * TEXT_UPSCALE - font.lineHeight / TEXT_UPSCALE, VIS_TEXT_COLOR);
        graphics.text(font, amount(snapshot.flux()), textX, fluxTop * TEXT_UPSCALE - font.lineHeight, FLUX_TEXT_COLOR);
        pose.popMatrix();
    }

    private static String amount(float value) {
        long tenths = Math.round(value * TENTHS);
        long whole = tenths / TENTHS;
        long fraction = Math.abs(tenths % TENTHS);
        return fraction == 0 ? Long.toString(whole) : whole + "." + fraction;
    }
}
