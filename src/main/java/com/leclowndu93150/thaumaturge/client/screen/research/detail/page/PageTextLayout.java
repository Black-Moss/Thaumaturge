package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge.KnowledgeGridRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement.RequirementLayout;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class PageTextLayout {
    private static final int COLUMN_LEFT_OFFSET = -15;
    private static final int CONTENT_TOP_OFFSET = -16;
    private static final int RIGHT_COLUMN_SHIFT = 152;
    private static final int TITLE_HEIGHT = 28;
    private static final int FIRST_PAGE_BUDGET = PageParser.PAGE_HEIGHT - TITLE_HEIGHT;
    private static final int REQUIREMENT_ROW_RESERVE = 18;
    private static final int REQUIREMENT_DIVIDER_RESERVE = 24;
    private static final int STAGE_HISTORY_GAP = 5;
    private static final int LAYOUT_ORIGIN = 0;
    private static final int HALF = 2;

    private static final int TITLE_COLOR = 0xFF202020;
    private static final int TITLE_CENTER_OFFSET = 55;
    private static final int TITLE_TEXT_Y = -10;
    private static final int TITLE_RULE_LEFT_OFFSET = 4;
    private static final int TITLE_RULE_TOP_Y = -17;
    private static final int TITLE_RULE_BOTTOM_Y = 0;

    private static final String ADDENDUM_KEY = "gui.thaumaturge.thaumonomicon.addendum";
    private static final int ADDENDUM_NUMBER_BASE = 1;

    private PageTextLayout() {}

    public static List<PageParser.Page> compose(Font font, IResearchStage stage, List<ResearchAddendum> addenda, int knowledgeRows, int stageCount, int progressStage) {
        List<String> texts = new ArrayList<>();
        texts.add(Component.translatable(stage.textKey()).getString());
        for (int index = 0; index < addenda.size(); index++) {
            String heading = Component.translatable(ADDENDUM_KEY, index + ADDENDUM_NUMBER_BASE).getString();
            texts.add(PageParser.headed(heading, Component.translatable(addenda.get(index).textKey()).getString()));
        }
        int requirementRows = requirementRows(stage);
        int reserved = reserve(requirementRows, knowledgeRows);
        if (requirementRows == 0 && stageCount > 1 && progressStage > 0) {
            reserved += STAGE_HISTORY_GAP;
        }
        return PageParser.paginate(font, texts, TITLE_HEIGHT, FIRST_PAGE_BUDGET - reserved);
    }

    private static int reserve(int requirementRows, int knowledgeRows) {
        boolean withRequirements = requirementRows > 0;
        if (knowledgeRows > 0) {
            return KnowledgeGridRenderer.inlineReserve(knowledgeRows, withRequirements) + requirementRows * REQUIREMENT_ROW_RESERVE;
        }
        return withRequirements ? requirementRows * REQUIREMENT_ROW_RESERVE + REQUIREMENT_DIVIDER_RESERVE : 0;
    }

    private static int requirementRows(IResearchStage stage) {
        RequirementLayout layout = RequirementLayout.of(stage, LAYOUT_ORIGIN);
        int rows = 0;
        rows += layout.hasResearch() ? 1 : 0;
        rows += layout.hasObtain() ? 1 : 0;
        rows += layout.hasCraft() ? 1 : 0;
        rows += layout.hasKnowledge() ? 1 : 0;
        return rows;
    }

    public static int contentTop(DetailFrame frame) {
        return frame.top() + CONTENT_TOP_OFFSET;
    }

    public static int columnLeft(DetailFrame frame) {
        return frame.left() + COLUMN_LEFT_OFFSET;
    }

    public static void drawSpread(GuiGraphicsExtractor graphics, DetailFrame frame, Component title, List<PageParser.Page> pages, int leftIndex) {
        int top = contentTop(frame);
        int left = columnLeft(frame);
        if (leftIndex == 0) {
            drawTitle(graphics, frame, title);
        }
        if (leftIndex >= 0 && leftIndex < pages.size()) {
            pages.get(leftIndex).draw(graphics, frame.font(), left, top);
        }
        int rightIndex = leftIndex + 1;
        if (rightIndex >= 0 && rightIndex < pages.size()) {
            pages.get(rightIndex).draw(graphics, frame.font(), left + RIGHT_COLUMN_SHIFT, top);
        }
    }

    private static void drawTitle(GuiGraphicsExtractor graphics, DetailFrame frame, Component title) {
        Font font = frame.font();
        int ruleLeft = frame.left() + TITLE_RULE_LEFT_OFFSET;
        drawRule(graphics, ruleLeft, frame.top() + TITLE_RULE_TOP_Y);
        drawRule(graphics, ruleLeft, frame.top() + TITLE_RULE_BOTTOM_Y);
        int center = frame.left() + TITLE_CENTER_OFFSET;
        int width = font.width(title);
        float scale = width > PageParser.PAGE_WIDTH ? (float) PageParser.PAGE_WIDTH / width : 1.0F;
        float lift = (font.lineHeight - font.lineHeight * scale) / HALF;
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) center, frame.top() + TITLE_TEXT_Y + lift);
        graphics.pose().scale(scale, scale);
        graphics.text(font, title, -width / HALF, 0, TITLE_COLOR, false);
        graphics.pose().popMatrix();
    }

    private static void drawRule(GuiGraphicsExtractor graphics, int x, int y) {
        BookBlit.sprite(graphics, x, y, BookSprites.DIVIDER_U, BookSprites.DIVIDER_V, BookSprites.DIVIDER_WIDTH, BookSprites.DIVIDER_THIN_HEIGHT);
    }
}
