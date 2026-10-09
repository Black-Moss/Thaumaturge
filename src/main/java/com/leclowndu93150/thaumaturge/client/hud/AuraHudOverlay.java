package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.aura.ClientAuraCache;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestAuraChunkPayload;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AuraHudOverlay implements LeftHudStack.Gauge {
    private static final int GAUGE_HEIGHT = 83;
    private static final float AURA_FULL = 525.0F;
    private static final int TEX = TTScreenTextures.TEX_SIZE;

    private static final int FRAME_X = 3;
    private static final int FRAME_Y = 1;
    private static final int FRAME_U = 72;
    private static final int FRAME_V = 48;
    private static final int FRAME_W = 16;
    private static final int FRAME_H = 80;

    private static final int TUBE_TOP = 10;
    private static final int TUBE_HEIGHT = 64;
    private static final int TUBE_BOTTOM = 74;
    private static final int COLUMN_X = 7;
    private static final int COLUMN_W = 8;
    private static final int FILL_U = 88;
    private static final int FILL_V = 56;
    private static final int VIS_RIPPLE_U = 96;
    private static final int FLUX_RIPPLE_U = 104;
    private static final float VIS_RIPPLE_BASE_V = 56.0F;
    private static final float FLUX_RIPPLE_BASE_V = 120.0F;
    private static final float FLUX_CLOCK_DIVISOR = 3.0F;
    private static final int VIS_FILL_TINT = 0xFFB266E5;
    private static final int VIS_RIPPLE_TINT = 0x80FFFFFF;
    private static final int FLUX_FILL_TINT = 0xFF3F194C;
    private static final int FLUX_RIPPLE_TINT = 0x80B266FF;

    private static final int POINTER_X = 4;
    private static final int POINTER_U = 117;
    private static final int POINTER_V = 61;
    private static final int POINTER_W = 14;
    private static final int POINTER_H = 5;
    private static final int POINTER_TOP = 8;

    private static final int TEXT_X = 18;
    private static final float TEXT_SCALE = 0.5F;
    private static final int FLUX_TEXT_RISE = 4;
    private static final int VIS_TEXT_COLOR = 0xFFEEAAFF;
    private static final int FLUX_TEXT_COLOR = 0xFFAA11BB;

    private static final int AMOUNT_MAX_INTEGER_DIGITS = 7;
    private static final int AMOUNT_MAX_FRACTION_DIGITS = 1;
    private static final RoundingMode AMOUNT_ROUNDING = RoundingMode.HALF_UP;

    private final NumberFormat amountFormat = createAmountFormat();

    public AuraHudOverlay() {}

    @Override
    public boolean visible(Minecraft mc, LocalPlayer player) {
        return GogglesAccess.wearsRevealingGear(player) || holdsScanner(player);
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
        if (player == null) {
            return;
        }
        ChunkPos chunk = player.chunkPosition();
        if (ClientAuraCache.shouldRequest(chunk)) {
            ClientPacketDistributor.sendToServer(new ServerboundRequestAuraChunkPayload(chunk.x(), chunk.z()));
        }
        ClientAuraCache.Snapshot snapshot = ClientAuraCache.get(chunk);
        if (snapshot == null) {
            snapshot = ClientAuraCache.latest();
        }
        if (snapshot == null) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        float clockA = player.tickCount + partial;
        float clockB = player.tickCount / FLUX_CLOCK_DIVISOR + partial;
        float base = fraction(snapshot.base());
        float vis = fraction(snapshot.vis());
        float flux = fraction(snapshot.flux());
        float combined = flux + vis;
        if (combined > 1.0F) {
            float squeeze = 1.0F / combined;
            base *= squeeze;
            vis *= squeeze;
            flux *= squeeze;
        }
        int visHeight = Math.round(vis * TUBE_HEIGHT);
        int visTop = TUBE_BOTTOM - visHeight;
        float fluxTopExact = TUBE_TOP + (1.0F - flux - vis) * TUBE_HEIGHT;
        int fluxTop = Math.round(fluxTopExact);
        int fluxHeight = Math.round(flux * TUBE_HEIGHT);
        if (vis > 0.0F && visHeight > 0) {
            drawColumn(graphics, visTop, visHeight, VIS_FILL_TINT, VIS_RIPPLE_U, VIS_RIPPLE_BASE_V + clockA % TUBE_HEIGHT, VIS_RIPPLE_TINT);
        }
        if (flux > 0.0F && fluxHeight > 0) {
            drawColumn(graphics, fluxTop, fluxHeight, FLUX_FILL_TINT, FLUX_RIPPLE_U, FLUX_RIPPLE_BASE_V - clockB % TUBE_HEIGHT, FLUX_RIPPLE_TINT);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, FRAME_X, FRAME_Y, FRAME_U, FRAME_V, FRAME_W, FRAME_H, FRAME_W, FRAME_H, TEX, TEX);
        if (player.isShiftKeyDown()) {
            if (vis > 0.0F) {
                drawAmount(graphics, mc, snapshot.vis(), TUBE_TOP + (1.0F - vis) * TUBE_HEIGHT, VIS_TEXT_COLOR);
            }
            if (flux > 0.0F) {
                drawAmount(graphics, mc, snapshot.flux(), fluxTopExact - FLUX_TEXT_RISE, FLUX_TEXT_COLOR);
            }
        }
        int pointerTop = Math.round(POINTER_TOP + (1.0F - base) * TUBE_HEIGHT);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.HUD, POINTER_X, pointerTop, POINTER_U, POINTER_V, POINTER_W, POINTER_H, POINTER_W, POINTER_H, TEX, TEX);
    }

    private void drawColumn(GuiGraphicsExtractor graphics, int top, int height, int fillTint, int rippleU, float rippleV, int rippleTint) {
        RenderPipeline textured = RenderPipelines.GUI_TEXTURED;
        graphics.blit(textured, TTScreenTextures.HUD, COLUMN_X, top, FILL_U, FILL_V, COLUMN_W, height, COLUMN_W, TUBE_HEIGHT, TEX, TEX, fillTint);
        graphics.blit(TTRenderPipelines.GUI_TEXTURED_ADDITIVE, TTScreenTextures.HUD, COLUMN_X, top, rippleU, rippleV, COLUMN_W, height, COLUMN_W, height, TEX, TEX, rippleTint);
    }

    private void drawAmount(GuiGraphicsExtractor graphics, Minecraft mc, float amount, float y, int color) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(TEXT_X, y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(mc.font, amountFormat.format(amount), 0, 0, color, false);
        graphics.pose().popMatrix();
    }

    private static float fraction(float value) {
        return Mth.clamp(value / AURA_FULL, 0.0F, 1.0F);
    }

    private static boolean holdsScanner(LocalPlayer player) {
        return player.getMainHandItem().is(TTItems.THAUMOMETER) || player.getOffhandItem().is(TTItems.THAUMOMETER);
    }

    private static NumberFormat createAmountFormat() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumIntegerDigits(AMOUNT_MAX_INTEGER_DIGITS);
        format.setMaximumFractionDigits(AMOUNT_MAX_FRACTION_DIGITS);
        format.setGroupingUsed(false);
        format.setRoundingMode(AMOUNT_ROUNDING);
        return format;
    }
}
