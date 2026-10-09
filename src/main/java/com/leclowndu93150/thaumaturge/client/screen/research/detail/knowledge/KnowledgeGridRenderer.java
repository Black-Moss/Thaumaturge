package com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class KnowledgeGridRenderer {
    private static final int ICON_TEXTURE_SIZE = 256;
    private static final float ICON_SCALE = 0.0625F;
    private static final int GRID_LEFT_OFFSET = -10;
    private static final int GRID_TOP_OFFSET = -18;
    private static final int BAR_FILLED_V = 232;
    private static final int BAR_EMPTY_V = 234;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_WIDTH = 16;
    private static final int BAR_Y_OFFSET = 17;
    private static final int AMOUNT_X_OFFSET = 16;
    private static final int AMOUNT_Y_OFFSET = 8;
    private static final int CATEGORY_BADGE_OFFSET = 66;
    private static final float CATEGORY_BADGE_SCALE = 0.66F;
    private static final int CATEGORY_BADGE_TINT = 0xBFFFFFFF;
    private static final int CLOSING_DIVIDER_INSET = 4;
    private static final int CLOSING_DIVIDER_OFFSET = 12;
    private static final int INSERT_GRID_LEFT = 60;
    private static final int INSERT_GRID_TOP = 75;
    private static final int IN_PAGE_GRID_TOP = 210 - 16;

    private KnowledgeGridRenderer() {}

    public static Optional<List<Holder.Reference<IResearchCategory>>> categories(Player player) {
        return player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY).map(lookup -> lookup.listElements().toList());
    }

    private static List<ResourceKey<IResearchCategory>> keysOf(List<Holder.Reference<IResearchCategory>> categories) {
        return categories.stream().flatMap(category -> category.unwrapKey().stream()).toList();
    }

    public static int activeTypeCount(@Nullable Player player) {
        if (player == null) {
            return 0;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        return categories(player).map(KnowledgeGridRenderer::keysOf)
                .map(keys -> (int) Arrays.stream(KnowledgeType.values()).filter(type -> keys.stream().anyMatch(key -> knowledge.rawKnowledge(type, key) > 0)).count()).orElse(0);
    }

    public static boolean hasAnyKnowledge(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        return categories(player).map(KnowledgeGridRenderer::keysOf)
                .map(keys -> keys.stream().anyMatch(key -> Arrays.stream(KnowledgeType.values()).anyMatch(type -> knowledge.rawKnowledge(type, key) > 0))).orElse(false);
    }

    public static void drawInPage(GuiGraphicsExtractor graphics, DetailFrame frame, int mouseX, int mouseY) {
        draw(graphics, frame, frame.left(), frame.top() + IN_PAGE_GRID_TOP, mouseX, mouseY, KnowledgeGridLayout.IN_PAGE);
    }

    public static void drawInsert(GuiGraphicsExtractor graphics, DetailFrame frame, int mouseX, int mouseY) {
        BookBlit.paper(graphics, frame.paperLeft(), frame.paperTop());
        draw(graphics, frame, frame.paperLeft() + INSERT_GRID_LEFT, frame.top() + INSERT_GRID_TOP, mouseX, mouseY, KnowledgeGridLayout.INSERT);
        if (!hasAnyKnowledge(frame.player())) {
            Component hint = Component.translatable("gui.thaumaturge.thaumonomicon.knowledge_none");
            graphics.text(frame.font(), hint, (frame.screenWidth() - frame.font().width(hint)) / 2, frame.top() + INSERT_GRID_TOP, BookSprites.WHITE, false);
        }
    }

    private static void draw(GuiGraphicsExtractor graphics, DetailFrame frame, int originX, int originY, int mouseX, int mouseY, KnowledgeGridLayout layout) {
        Player player = frame.player();
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        List<Holder.Reference<IResearchCategory>> categories = categories(player).orElse(null);
        if (categories == null || categories.isEmpty()) {
            return;
        }
        int columnStride = layout.columnStride(categories.size());
        int firstRowY = originY + GRID_TOP_OFFSET;
        int rowIndex = 0;
        boolean anyDrawn = false;
        for (KnowledgeType type : KnowledgeType.values()) {
            int columnIndex = 0;
            boolean rowHasCells = false;
            for (Holder.Reference<IResearchCategory> category : categories) {
                ResourceKey<IResearchCategory> categoryKey = category.unwrapKey().orElse(null);
                if (categoryKey == null) {
                    continue;
                }
                int amount = knowledge.knowledge(type, categoryKey);
                int partial = type.progression() > 0 ? knowledge.rawKnowledge(type, categoryKey) % type.progression() : 0;
                if (amount <= 0 && partial <= 0) {
                    continue;
                }
                anyDrawn = true;
                int cellX = originX + GRID_LEFT_OFFSET + columnStride * columnIndex;
                int cellY = firstRowY - rowIndex * layout.rowStride();
                drawCell(graphics, frame.font(), cellX, cellY, type, category, amount, partial);
                if (Rect.inside(cellX, cellY, BookSprites.SLOT_SIZE, BookSprites.SLOT_SIZE, mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(frame.font(), TTTooltips.knowledgeLabel(type, categoryKey), mouseX, mouseY);
                }
                columnIndex++;
                rowHasCells = true;
            }
            if (rowHasCells) {
                rowIndex++;
            }
        }
        if (layout == KnowledgeGridLayout.IN_PAGE && anyDrawn) {
            BookBlit.sprite(graphics, originX + CLOSING_DIVIDER_INSET, firstRowY - rowIndex * layout.rowStride() + CLOSING_DIVIDER_OFFSET, BookSprites.DIVIDER_U, BookSprites.DIVIDER_V,
                    BookSprites.DIVIDER_WIDTH, BookSprites.DIVIDER_TALL_HEIGHT);
        }
    }

    private static void drawCell(GuiGraphicsExtractor graphics, Font font, int cellX, int cellY, KnowledgeType type, Holder.Reference<IResearchCategory> category, int amount, int partial) {
        drawIcon(graphics, cellX, cellY, type, category);
        String amountText = Integer.toString(amount);
        graphics.text(font, Component.literal(amountText), cellX + AMOUNT_X_OFFSET - font.width(amountText), cellY + AMOUNT_Y_OFFSET, BookSprites.WHITE, true);
        if (partial > 0 && type.progression() > 0) {
            int filled = (int) ((float) partial / type.progression() * BAR_WIDTH);
            int barY = cellY + BAR_Y_OFFSET;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, cellX, barY, 0.0F, (float) BAR_FILLED_V, filled, BAR_HEIGHT, filled, BAR_HEIGHT, TTScreenTextures.TEX_SIZE,
                    TTScreenTextures.TEX_SIZE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BOOK, cellX + filled, barY, (float) filled, (float) BAR_EMPTY_V, BAR_WIDTH - filled, BAR_HEIGHT, BAR_WIDTH - filled,
                    BAR_HEIGHT, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
        }
    }

    private static void drawIcon(GuiGraphicsExtractor graphics, int x, int y, KnowledgeType type, Holder.Reference<IResearchCategory> category) {
        Identifier typeIcon = TTIds.rl("textures/research/knowledge_" + type.getSerializedName() + ".png");
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(ICON_SCALE, ICON_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, typeIcon, 0, 0, 0.0F, 0.0F, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
        graphics.pose().translate((float) CATEGORY_BADGE_OFFSET, (float) CATEGORY_BADGE_OFFSET);
        graphics.pose().scale(CATEGORY_BADGE_SCALE, CATEGORY_BADGE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, category.value().icon(), 0, 0, 0.0F, 0.0F, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE, CATEGORY_BADGE_TINT);
        graphics.pose().popMatrix();
    }
}
