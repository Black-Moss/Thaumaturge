package com.leclowndu93150.thaumcraft.client.render.research;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumcraft.api.capability.KnowledgeType;
import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import com.leclowndu93150.thaumcraft.api.research.KnowledgeReward;
import com.leclowndu93150.thaumcraft.client.screen.TCScreenTextures;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class KnowledgeRequirementWidget {
    public static final int ICON_SIZE = 16;
    private static final int ROW_STRIDE = 18;

    private static final int SPRITE_SIZE = 255;
    private static final int ATLAS_REF = 256;
    private static final float ICON_MATRIX_SCALE = 0.0625F;

    private static final int CATEGORY_OVERLAY_TRANSLATE = 2;
    private static final float CATEGORY_OVERLAY_SCALE = 0.046875F;
    private static final int CATEGORY_OVERLAY_COLOR = 0xFFFFFFFF;

    private static final int COUNT_TEXT_COLOR = 0xFFFFFFFF;
    private static final int COUNT_BASELINE_X = 16;
    private static final int COUNT_BASELINE_Y = 12;
    private static final float COUNT_TEXT_SCALE = 0.5F;

    private static final int CHECKMARK_OFFSET_X = 8;
    private static final int CHECKMARK_U = 159;
    private static final int CHECKMARK_V = 207;
    private static final int CHECKMARK_SIZE = 10;

    private static final int PROGRESS_BAR_HEIGHT = 2;
    private static final int PROGRESS_BAR_FILLED_V = 232;
    private static final int PROGRESS_BAR_EMPTY_V = 234;

    private static final Map<KnowledgeType, Identifier> ICONS = new EnumMap<>(KnowledgeType.class);
    static {
        ICONS.put(KnowledgeType.THEORY, Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/research/knowledge_theory.png"));
        ICONS.put(KnowledgeType.OBSERVATION, Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/research/knowledge_observation.png"));
    }

    private KnowledgeRequirementWidget() {}

    public static int height() {
        return ROW_STRIDE;
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            int x,
            int y,
            KnowledgeReward requirement,
            IPlayerKnowledge knowledge
    ) {
        ResourceKey<IResearchCategory> categoryKey = requirement.category().unwrapKey().orElse(null);
        int current = categoryKey == null ? 0 : knowledge.knowledge(requirement.type(), categoryKey);
        int rawCounter = categoryKey == null ? 0 : knowledge.rawKnowledge(requirement.type(), categoryKey);
        int progression = requirement.type().progression();
        int partial = progression > 0 ? rawCounter % progression : 0;
        int required = requirement.amount();
        boolean hasKnow = current >= required;

        drawIcon(graphics, x, y, requirement.type());
        if (categoryKey != null) {
            drawCategoryOverlay(graphics, x, y, categoryKey);
        }
        if (partial > 0 && progression > 0) {
            drawProgressBar(graphics, x, y + ICON_SIZE + 1, partial, progression);
        }
        if (hasKnow) {
            drawCheckmark(graphics, x, y);
        }
        drawCount(graphics, font, x, y, required, hasKnow);
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, int x, int y, KnowledgeType type) {
        Identifier icon = ICONS.get(type);
        if (icon == null) return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(ICON_MATRIX_SCALE, ICON_MATRIX_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                icon,
                0, 0,
                0.0F, 0.0F,
                SPRITE_SIZE, SPRITE_SIZE,
                ATLAS_REF, ATLAS_REF
        );
        graphics.pose().popMatrix();
    }

    private static void drawCategoryOverlay(GuiGraphicsExtractor graphics, int x, int y, ResourceKey<IResearchCategory> categoryKey) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        HolderLookup.Provider registries = mc.player.registryAccess();
        registries.lookup(IResearchCategory.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(categoryKey))
                .ifPresent(holder -> drawCategoryOverlayInternal(graphics, x, y, holder));
    }

    private static void drawCategoryOverlayInternal(GuiGraphicsExtractor graphics, int x, int y, Holder.Reference<IResearchCategory> holder) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + CATEGORY_OVERLAY_TRANSLATE, y + CATEGORY_OVERLAY_TRANSLATE);
        graphics.pose().scale(CATEGORY_OVERLAY_SCALE, CATEGORY_OVERLAY_SCALE);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                holder.value().icon(),
                0, 0,
                0.0F, 0.0F,
                SPRITE_SIZE, SPRITE_SIZE,
                SPRITE_SIZE, SPRITE_SIZE,
                ATLAS_REF, ATLAS_REF,
                CATEGORY_OVERLAY_COLOR
        );
        graphics.pose().popMatrix();
    }

    private static void drawCheckmark(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TCScreenTextures.RESEARCH_BOOK,
                x + CHECKMARK_OFFSET_X, y,
                (float) CHECKMARK_U, (float) CHECKMARK_V,
                CHECKMARK_SIZE, CHECKMARK_SIZE,
                CHECKMARK_SIZE, CHECKMARK_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
        );
    }

    private static void drawCount(GuiGraphicsExtractor graphics, Font font, int x, int y, int required, boolean hasKnow) {
        Component amount = hasKnow
                ? Component.literal(Integer.toString(required))
                : Component.literal(Integer.toString(required)).withStyle(ChatFormatting.RED);
        int width = font.width(amount);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + COUNT_BASELINE_X - width / 2.0F, y + COUNT_BASELINE_Y);
        graphics.pose().scale(COUNT_TEXT_SCALE, COUNT_TEXT_SCALE);
        graphics.text(font, amount, 0, 0, COUNT_TEXT_COLOR, true);
        graphics.pose().popMatrix();
    }

    private static void drawProgressBar(GuiGraphicsExtractor graphics, int x, int y, int partial, int progression) {
        int filledWidth = Math.max(0, Math.min(ICON_SIZE, partial * ICON_SIZE / progression));
        int emptyWidth = ICON_SIZE - filledWidth;
        if (filledWidth > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.RESEARCH_BOOK,
                    x, y,
                    0.0F, (float) PROGRESS_BAR_FILLED_V,
                    filledWidth, PROGRESS_BAR_HEIGHT,
                    filledWidth, PROGRESS_BAR_HEIGHT,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
            );
        }
        if (emptyWidth > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TCScreenTextures.RESEARCH_BOOK,
                    x + filledWidth, y,
                    (float) filledWidth, (float) PROGRESS_BAR_EMPTY_V,
                    emptyWidth, PROGRESS_BAR_HEIGHT,
                    emptyWidth, PROGRESS_BAR_HEIGHT,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE
            );
        }
    }
}
