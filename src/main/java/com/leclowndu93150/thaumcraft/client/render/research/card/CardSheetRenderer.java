package com.leclowndu93150.thaumcraft.client.render.research.card;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.TheorycraftCard;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumcraft.content.research.theorycraft.ResearchTableData;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CardSheetRenderer {
    public static final float DEFAULT_SCALE = 6.0F;
    public static final int CARD_HIT_WIDTH = 100;
    public static final int CARD_HIT_HEIGHT = 120;

    private static final float INNER_TEXT_SCALE = 0.0625F;
    private static final float INNER_ITEM_SCALE = 0.125F;

    private static final int PAPER_HALF = 8;
    private static final int PAPER_QUAD_SIZE = 16;
    private static final int PAPER_TEXTURE_SIZE = 256;

    private static final int CARD_NAME_Y_PRESCALE = -65;
    private static final int CARD_TEXT_Y_PRESCALE = -48;
    private static final int CARD_TEXT_WIDTH_PRESCALE = 140;
    private static final int CARD_TEXT_X_PRESCALE = -70;

    private static final int INK_BOTTLE_FILLED_U = 32;
    private static final int INK_BOTTLE_FILLED_V = 96;
    private static final int INK_BOTTLE_EXTRA_U = 48;
    private static final int INK_BOTTLE_EXTRA_V = 0;
    private static final int INK_BOTTLE_SIZE = 16;
    private static final int INK_BOTTLE_STRIDE_PRESCALE = 20;
    private static final int INK_BOTTLE_Y_PRESCALE = -95;

    private static final int CONSUMED_INDICATOR_U = 64;
    private static final int CONSUMED_INDICATOR_V = 120;
    private static final int CONSUMED_INDICATOR_SIZE = 16;
    private static final int CONSUMED_INDICATOR_BASE_Y = 45;
    private static final int CONSUMED_INDICATOR_X_OFFSET = -2;
    private static final float CONSUMED_INDICATOR_BASE_SCALE = 0.5F;
    private static final float CONSUMED_BOB_TRANSLATE_GAIN = 10.0F;
    private static final float CONSUMED_BOB_AMPLITUDE = 0.03F;
    private static final float CONSUMED_BOB_DIVISOR = 2.0F;
    private static final int CONSUMED_BOB_INDEX_FACTOR = 2;

    private static final int ITEM_Y_PRESCALE = 35;
    private static final int ITEM_STRIDE_PRESCALE = 18;
    private static final int ITEM_HALF_STRIDE_PRESCALE = 9;

    private static final Identifier UNKNOWN_TEXTURE =
            Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/aspects/_unknown.png");
    private static final int UNKNOWN_QUAD_SIZE = 16;
    private static final int UNKNOWN_TEX_SIZE = 32;
    private static final int UNKNOWN_TINT_RGB = 0xBFBFBF;

    private static final float CATEGORY_WATERMARK_SCALE = 0.5F;
    private static final int CATEGORY_WATERMARK_TEXTURE_SIZE = 256;
    private static final float CATEGORY_WATERMARK_ALPHA_DIVISOR = 6.0F;

    private static final float FULL_ALPHA = 1.0F;

    private CardSheetRenderer() {}

    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            HolderLookup.Provider registries,
            double centerX,
            double centerY,
            float scale,
            float tilt,
            float alpha,
            ResearchTableData.CardChoice choice
    ) {
        TheorycraftCard card = choice.hydrate(registries);
        renderInternal(graphics, font, registries, centerX, centerY, scale, tilt, alpha,
                choice.fromAid(), card, choice.seed());
    }

    public static void renderBlank(
            GuiGraphicsExtractor graphics,
            double centerX,
            double centerY,
            float scale,
            float tilt,
            float alpha,
            long randomSeed,
            boolean gilded
    ) {
        renderInternal(graphics, null, null, centerX, centerY, scale, tilt, alpha,
                gilded, null, randomSeed);
    }

    private static void renderInternal(
            GuiGraphicsExtractor graphics,
            @Nullable Font font,
            HolderLookup.@Nullable Provider registries,
            double centerX,
            double centerY,
            float scale,
            float tilt,
            float alpha,
            boolean gilded,
            @Nullable TheorycraftCard card,
            long randomSeed
    ) {
        if (alpha <= 0.0F) return;
        Random r = new Random(randomSeed);
        float jitterX = (float) r.nextGaussian();
        float jitterY = (float) r.nextGaussian();
        float tiltRotation = (float) r.nextGaussian() * tilt;
        boolean flipRotated = r.nextBoolean();
        boolean flipMirrored = r.nextBoolean();
        int colorMask = packAlpha(alpha);

        graphics.pose().pushMatrix();
        graphics.pose().translate((float) centerX + jitterX, (float) centerY + jitterY);
        graphics.pose().scale(scale, scale);
        graphics.pose().rotate(tiltRotation * (float) (Math.PI / 180.0));

        graphics.pose().pushMatrix();
        if (flipRotated) {
            graphics.pose().rotate((float) Math.PI);
        }
        drawPaper(
                graphics,
                gilded ? TCScreenTextures.PAPER_GILDED : TCScreenTextures.PAPER,
                alpha,
                colorMask,
                flipMirrored
        );
        graphics.pose().popMatrix();

        if (card == null || font == null) {
            graphics.pose().popMatrix();
            return;
        }

        if (alpha == FULL_ALPHA) {
            if (registries != null) {
                renderCategoryWatermark(graphics, registries, card, alpha);
            }
            renderCardContent(graphics, font, card, alpha, colorMask);
        }

        graphics.pose().popMatrix();
    }

    private static void drawPaper(GuiGraphicsExtractor graphics, Identifier texture, float alpha, int colorMask, boolean flipMirrored) {
        if (alpha >= 0.999F) {
            float u0 = flipMirrored ? 1.0F : 0.0F;
            float u1 = flipMirrored ? 0.0F : 1.0F;
            graphics.blit(texture, -PAPER_HALF, -PAPER_HALF, PAPER_HALF, PAPER_HALF, u0, u1, 0.0F, 1.0F);
            return;
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                -PAPER_HALF, -PAPER_HALF,
                0.0F, 0.0F,
                PAPER_QUAD_SIZE, PAPER_QUAD_SIZE,
                PAPER_TEXTURE_SIZE, PAPER_TEXTURE_SIZE,
                PAPER_TEXTURE_SIZE, PAPER_TEXTURE_SIZE,
                colorMask
        );
    }

    private static void renderCategoryWatermark(
            GuiGraphicsExtractor graphics,
            HolderLookup.Provider registries,
            TheorycraftCard card,
            float alpha
    ) {
        ResourceKey<IResearchCategory> categoryKey = card.category();
        if (categoryKey == null) return;
        registries.lookup(IResearchCategory.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(categoryKey))
                .ifPresent(holder -> drawWatermark(graphics, holder, alpha));
    }

    private static void drawWatermark(GuiGraphicsExtractor graphics, Holder.Reference<IResearchCategory> holder, float alpha) {
        int watermarkColor = packAlpha(alpha / CATEGORY_WATERMARK_ALPHA_DIVISOR);
        graphics.pose().pushMatrix();
        graphics.pose().scale(CATEGORY_WATERMARK_SCALE, CATEGORY_WATERMARK_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                holder.value().icon(),
                -PAPER_HALF, -PAPER_HALF,
                0.0F, 0.0F,
                PAPER_QUAD_SIZE, PAPER_QUAD_SIZE,
                CATEGORY_WATERMARK_TEXTURE_SIZE, CATEGORY_WATERMARK_TEXTURE_SIZE,
                CATEGORY_WATERMARK_TEXTURE_SIZE, CATEGORY_WATERMARK_TEXTURE_SIZE,
                watermarkColor
        );
        graphics.pose().popMatrix();
    }

    private static void renderCardContent(GuiGraphicsExtractor graphics, Font font, TheorycraftCard card, float alpha, int colorMask) {
        int textColor = packBlackWithAlpha(alpha);

        graphics.pose().pushMatrix();
        graphics.pose().scale(INNER_TEXT_SCALE, INNER_TEXT_SCALE);

        MutableComponent name = Component.empty().append(card.name()).withStyle(ChatFormatting.BOLD);
        int nameWidth = font.width(name);
        graphics.text(font, name, -nameWidth / 2, CARD_NAME_Y_PRESCALE, textColor, false);

        List<FormattedText> lines = font.getSplitter().splitLines(card.description(), CARD_TEXT_WIDTH_PRESCALE, Style.EMPTY);
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, Component.literal(lines.get(i).getString()), CARD_TEXT_X_PRESCALE, CARD_TEXT_Y_PRESCALE + i * 9, textColor, false);
        }
        graphics.pose().popMatrix();

        renderInkBottles(graphics, card.inspirationCost(), colorMask);
        renderRequiredItems(graphics, card, alpha, colorMask);
    }

    private static void renderInkBottles(GuiGraphicsExtractor graphics, int cost, int colorMask) {
        graphics.pose().pushMatrix();
        graphics.pose().scale(INNER_TEXT_SCALE, INNER_TEXT_SCALE);
        boolean add = cost < 0;
        int absCost = Math.abs(cost);
        if (add) absCost++;
        for (int i = 0; i < absCost; i++) {
            int u = (i == 0 && add) ? INK_BOTTLE_EXTRA_U : INK_BOTTLE_FILLED_U;
            int v = (i == 0 && add) ? INK_BOTTLE_EXTRA_V : INK_BOTTLE_FILLED_V;
            int x = -10 * absCost + INK_BOTTLE_STRIDE_PRESCALE * i;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.GUI_BASE,
                    x, INK_BOTTLE_Y_PRESCALE,
                    (float) u, (float) v,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    INK_BOTTLE_SIZE, INK_BOTTLE_SIZE,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                    colorMask
            );
        }
        graphics.pose().popMatrix();
    }

    private static void renderRequiredItems(GuiGraphicsExtractor graphics, TheorycraftCard card, float alpha, int colorMask) {
        List<TheorycraftCard.CardItemRequirement> items = card.requiredItems();
        if (items.isEmpty()) return;
        int count = items.size();
        int ticksExisted = currentTicksExisted();
        float partialTick = currentPartialTick();
        int unknownColor = packTintWithAlpha(UNKNOWN_TINT_RGB, alpha);

        graphics.pose().pushMatrix();
        graphics.pose().scale(INNER_ITEM_SCALE, INNER_ITEM_SCALE);
        for (int i = 0; i < count; i++) {
            ItemStack stack = items.get(i).stack();
            int x = -ITEM_HALF_STRIDE_PRESCALE * count + ITEM_STRIDE_PRESCALE * i;
            if (stack.isEmpty()) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(x, ITEM_Y_PRESCALE);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        UNKNOWN_TEXTURE,
                        0, 0,
                        0.0F, 0.0F,
                        UNKNOWN_QUAD_SIZE, UNKNOWN_QUAD_SIZE,
                        UNKNOWN_TEX_SIZE, UNKNOWN_TEX_SIZE,
                        UNKNOWN_TEX_SIZE, UNKNOWN_TEX_SIZE,
                        unknownColor
                );
                graphics.pose().popMatrix();
                continue;
            }
            graphics.item(stack, x, ITEM_Y_PRESCALE);
            if (items.get(i).consumed()) {
                float bob = (float) Math.sin((ticksExisted + i * CONSUMED_BOB_INDEX_FACTOR + partialTick) / CONSUMED_BOB_DIVISOR) * CONSUMED_BOB_AMPLITUDE;
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + CONSUMED_INDICATOR_X_OFFSET, CONSUMED_INDICATOR_BASE_Y + bob * CONSUMED_BOB_TRANSLATE_GAIN);
                graphics.pose().scale(CONSUMED_INDICATOR_BASE_SCALE, CONSUMED_INDICATOR_BASE_SCALE + bob);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TCScreenTextures.GUI_BASE,
                        0, 0,
                        (float) CONSUMED_INDICATOR_U, (float) CONSUMED_INDICATOR_V,
                        CONSUMED_INDICATOR_SIZE, CONSUMED_INDICATOR_SIZE,
                        CONSUMED_INDICATOR_SIZE, CONSUMED_INDICATOR_SIZE,
                        TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                        colorMask
                );
                graphics.pose().popMatrix();
            }
        }
        graphics.pose().popMatrix();
    }

    private static int currentTicksExisted() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? 0 : player.tickCount;
    }

    private static float currentPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    private static int packAlpha(float alpha) {
        int a = clampAlpha(alpha);
        return (a << 24) | 0xFFFFFF;
    }

    private static int packBlackWithAlpha(float alpha) {
        return clampAlpha(alpha) << 24;
    }

    private static int packTintWithAlpha(int rgb, float alpha) {
        return (clampAlpha(alpha) << 24) | (rgb & 0xFFFFFF);
    }

    private static int clampAlpha(float alpha) {
        return Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
    }
}
