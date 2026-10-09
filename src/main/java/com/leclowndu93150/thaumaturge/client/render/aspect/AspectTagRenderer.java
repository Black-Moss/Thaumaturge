package com.leclowndu93150.thaumaturge.client.render.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public final class AspectTagRenderer {
    public static final int TAG_SIZE = 16;
    public static final int TEXTURE_SIZE = 32;

    private static final String AMOUNT_PATTERN = "#######.##";
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat(AMOUNT_PATTERN);
    private static final int MONOCHROME_RGB = 0x1A1A1A;
    private static final float MONOCHROME_ALPHA_FACTOR = 0.8F;
    private static final float CHANNEL_MAX = 255.0F;
    private static final int CHANNEL_MAX_INT = 255;
    private static final float MASKED_ALPHA = 0.45F;
    private static final float SMALL_TEXT_SCALE = 0.5F;
    private static final float LARGE_TEXT_FACTOR = 0.5F;
    private static final int OUTLINE_COLOR = 0xFF000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int BADGE_SIZE = 16;
    private static final int BADGE_OFFSET = 4;
    private static final int BADGE_FRAMES = 16;
    private static final int BADGE_STRIP_WIDTH = 256;
    private static final int BONUS_TEXT_X = 8;
    private static final int BONUS_TEXT_Y = 15;
    private static final int SCALED_PIXEL = 2;
    private static final int HALF_DIVISOR = 2;

    private AspectTagRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        paint(TagSpec.at(x, y), aspect, graphics, defaultFont());
    }

    public static void renderUnknown(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        paint(TagSpec.at(x, y).grayed(true), aspect, graphics, defaultFont());
    }

    public static void renderMaskedChip(GuiGraphicsExtractor graphics, int x, int y, @Nullable Holder<IAspect> aspect, AspectKnowledge knowledge) {
        float alpha = knowledge == AspectKnowledge.DEDUCIBLE ? 1.0F : MASKED_ALPHA;
        drawChip(graphics, x, y, aspect, alpha);
    }

    public static void renderUnknownChip(GuiGraphicsExtractor graphics, int x, int y, @Nullable Holder<IAspect> aspect) {
        drawChip(graphics, x, y, aspect, MASKED_ALPHA);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount) {
        paint(TagSpec.at(x, y).amount(amount), aspect, graphics, font);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, int x, int y, Holder<IAspect> aspect, float amount, int bonus, float alpha, boolean monochrome) {
        paint(TagSpec.at(x, y).amount(amount).bonus(bonus).opacity(alpha).grayed(monochrome), aspect, graphics, font);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, float alpha, boolean monochrome) {
        render(graphics, defaultFont(), x, y, aspect, amount, bonus, alpha, monochrome);
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, double depth) {
        paint(TagSpec.at(x, y).amount(amount).bonus(bonus), aspect, graphics, defaultFont());
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect, float amount, int bonus, double depth, BlendMode mode, float opacity) {
        paint(TagSpec.at(x, y).amount(amount).bonus(bonus).blend(mode).opacity(opacity), aspect, graphics, defaultFont());
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, double x, double y, @Nullable Holder<IAspect> aspect, float amount, int bonus, double depth, BlendMode mode, float opacity, boolean monochrome) {
        paint(TagSpec.at(x, y).amount(amount).bonus(bonus).blend(mode).opacity(opacity).grayed(monochrome), aspect, graphics, font);
    }

    private static void paint(TagSpec spec, @Nullable Holder<IAspect> aspect, GuiGraphicsExtractor graphics, Font font) {
        if (aspect == null || !aspect.isBound()) {
            return;
        }
        drawIcon(graphics, spec.x(), spec.y(), aspect.value(), spec.mode(), spec.opacity(), spec.monochrome());
        overlayLabels(graphics, font, spec);
    }

    public static int colorOf(IAspect aspect, float alpha, boolean monochrome) {
        int rgb = monochrome ? MONOCHROME_RGB : aspect.color() & 0xFFFFFF;
        return ARGB.color(alphaByte(monochrome ? alpha * MONOCHROME_ALPHA_FACTOR : alpha), rgb);
    }

    private static int alphaByte(float factor) {
        return Mth.clamp((int) (factor * CHANNEL_MAX), 0, CHANNEL_MAX_INT);
    }

    private static void overlayLabels(GuiGraphicsExtractor graphics, Font font, TagSpec spec) {
        if (spec.amount() > 0.0F) {
            drawAmount(graphics, font, spec.x(), spec.y(), spec.amount());
        }
        if (spec.bonus() > 0) {
            drawBonus(graphics, font, spec.x(), spec.y(), spec.bonus());
        }
    }

    private static Font defaultFont() {
        return Minecraft.getInstance().font;
    }

    private static void blitTag(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier texture, int x, int y, int color) {
        graphics.blit(pipeline, texture, x, y, 0.0F, 0.0F, TAG_SIZE, TAG_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, color);
    }

    private static void drawChip(GuiGraphicsExtractor graphics, int x, int y, @Nullable Holder<IAspect> aspect, float alpha) {
        boolean bound = aspect != null && aspect.isBound();
        int color = bound ? colorOf(aspect.value(), alpha, false) : ARGB.colorFromFloat(alpha, 1.0F, 1.0F, 1.0F);
        blitTag(graphics, RenderPipelines.GUI_TEXTURED, AspectTagWorldRenderer.UNKNOWN_TEXTURE, x, y, color);
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, double x, double y, IAspect aspect, BlendMode mode, float opacity, boolean monochrome) {
        RenderPipeline pipeline = mode == BlendMode.ADDITIVE ? TTRenderPipelines.GUI_TEXTURED_ADDITIVE : RenderPipelines.GUI_TEXTURED;
        int color = colorOf(aspect, opacity, monochrome);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) x, (float) y);
        blitTag(graphics, pipeline, aspect.texture(), 0, 0, color);
        pose.popMatrix();
    }

    private static void drawAmount(GuiGraphicsExtractor graphics, Font font, double x, double y, float amount) {
        String label = AMOUNT_FORMAT.format(amount);
        int rightEdge = doubledCell(x);
        int baseline = doubledCell(y);
        drawScaledText(graphics, font, label, rightEdge - font.width(label), baseline - font.lineHeight, true);
    }

    private static int doubledCell(double coordinate) {
        return TEXTURE_SIZE + SCALED_PIXEL * (int) coordinate;
    }

    private static void drawBonus(GuiGraphicsExtractor graphics, Font font, double x, double y, int bonus) {
        int cellX = (int) x;
        int cellY = (int) y;
        float glintU = badgeFrame() * BADGE_SIZE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ParticleTextures.BONUS_GLINT, cellX - BADGE_OFFSET, cellY - BADGE_OFFSET, glintU, 0.0F, BADGE_SIZE, BADGE_SIZE, BADGE_STRIP_WIDTH, BADGE_SIZE);
        if (bonus <= 1) {
            return;
        }
        String text = Integer.toString(bonus);
        int centerShift = font.width(text) / HALF_DIVISOR;
        drawScaledText(graphics, font, text, BONUS_TEXT_X + SCALED_PIXEL * cellX - centerShift, BONUS_TEXT_Y + SCALED_PIXEL * cellY - font.lineHeight, false);
    }

    private static void drawScaledText(GuiGraphicsExtractor graphics, Font font, String text, int originX, int originY, boolean outlined) {
        float factor = ThaumaturgeClientConfig.largeTagText() ? LARGE_TEXT_FACTOR : 1.0F;
        boolean shrink = factor == 1.0F;
        if (shrink) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(SMALL_TEXT_SCALE, SMALL_TEXT_SCALE);
        }
        putText(graphics, font, text, originX * factor, originY * factor, outlined);
        if (shrink) {
            graphics.pose().popMatrix();
        }
    }

    private static void putText(GuiGraphicsExtractor graphics, Font font, String text, float drawX, float drawY, boolean outlined) {
        if (outlined) {
            for (int axis = 0; axis < 2; axis++) {
                for (int sign = -1; sign <= 1; sign += 2) {
                    int dx = axis == 0 ? sign : 0;
                    int dy = axis == 0 ? 0 : sign;
                    graphics.text(font, text, (int) (drawX + dx), (int) (drawY + dy), OUTLINE_COLOR, false);
                }
            }
        }
        graphics.text(font, text, (int) drawX, (int) drawY, TEXT_COLOR, !outlined);
    }

    private static int badgeFrame() {
        Minecraft mc = Minecraft.getInstance();
        int ticks = mc.player != null ? mc.player.tickCount : mc.gui.getGuiTicks();
        return ticks & (BADGE_FRAMES - 1);
    }

    public enum BlendMode {
        ALPHA, ADDITIVE
    }

    private record TagSpec(double x, double y, float amount, int bonus, BlendMode mode, float opacity, boolean monochrome) {
        static TagSpec at(double x, double y) {
            return new TagSpec(x, y, 0.0F, 0, BlendMode.ALPHA, 1.0F, false);
        }

        TagSpec amount(float value) {
            return new TagSpec(x, y, value, bonus, mode, opacity, monochrome);
        }

        TagSpec bonus(int value) {
            return new TagSpec(x, y, amount, value, mode, opacity, monochrome);
        }

        TagSpec blend(BlendMode value) {
            return new TagSpec(x, y, amount, bonus, value, opacity, monochrome);
        }

        TagSpec opacity(float value) {
            return new TagSpec(x, y, amount, bonus, mode, value, monochrome);
        }

        TagSpec grayed(boolean value) {
            return new TagSpec(x, y, amount, bonus, mode, opacity, value);
        }
    }
}
