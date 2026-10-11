package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge.KnowledgeGridRenderer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.requirement.RequirementLayout;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class PageTextLayout {
    private static final int COLUMN_LEFT_OFFSET = -18;
    private static final int CONTENT_TOP_OFFSET = -18;
    private static final int RIGHT_COLUMN_SHIFT = 152;
    private static final int TITLE_HEIGHT = 28;
    private static final int FIRST_PAGE_BUDGET = PageParser.PAGE_HEIGHT - TITLE_HEIGHT;
    private static final int PAGE_BOTTOM_OFFSET = CONTENT_TOP_OFFSET + PageParser.PAGE_HEIGHT;
    private static final int REQUIREMENT_CLEARANCE = 2;
    private static final int LAYOUT_ORIGIN = 0;
    private static final int HALF = 2;

    private static final int TITLE_COLOR = 0xFF2B2724;
    private static final int TITLE_TEXT_Y = 10;
    private static final int TITLE_RULE_TOP_Y = 2;
    private static final int TITLE_RULE_BOTTOM_Y = 22;

    private static final String ADDENDUM_KEY = "gui.thaumaturge.thaumonomicon.addendum";
    private static final int ADDENDUM_NUMBER_BASE = 1;

    private PageTextLayout() {}

    public static List<PageParser.Page> compose(Font font, IResearchStage stage, List<ResearchAddendum> addenda, int knowledgeRows) {
        List<String> texts = new ArrayList<>();
        texts.add(Component.translatable(stage.textKey()).getString());
        for (int index = 0; index < addenda.size(); index++) {
            String heading = Component.translatable(ADDENDUM_KEY, index + ADDENDUM_NUMBER_BASE).getString();
            texts.add(PageParser.headed(heading, Component.translatable(addenda.get(index).textKey()).getString()));
        }
        int reserved = Math.max(requirementReserve(stage), KnowledgeGridRenderer.inlineReserve(knowledgeRows));
        return PageParser.paginate(font, texts, TITLE_HEIGHT, FIRST_PAGE_BUDGET - reserved);
    }

    private static int requirementReserve(IResearchStage stage) {
        RequirementLayout layout = RequirementLayout.of(stage, LAYOUT_ORIGIN);
        if (!layout.any()) {
            return 0;
        }
        return PAGE_BOTTOM_OFFSET - layout.completeButton(LAYOUT_ORIGIN).y() + REQUIREMENT_CLEARANCE;
    }

    public static int contentTop(DetailFrame frame) {
        return frame.top() + CONTENT_TOP_OFFSET;
    }

    public static int titleBottom(DetailFrame frame) {
        return contentTop(frame) + TITLE_HEIGHT;
    }

    public static int pageBottom(DetailFrame frame) {
        return frame.top() + PAGE_BOTTOM_OFFSET;
    }

    public static int columnLeft(DetailFrame frame) {
        return frame.left() + COLUMN_LEFT_OFFSET;
    }

    public static int columnCenter(DetailFrame frame) {
        return columnLeft(frame) + PageParser.PAGE_WIDTH / HALF;
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
        int top = contentTop(frame);
        int center = columnCenter(frame);
        PageParser.drawRule(graphics, center, top + TITLE_RULE_TOP_Y);
        PageParser.drawRule(graphics, center, top + TITLE_RULE_BOTTOM_Y);
        int width = font.width(title);
        float scale = width > PageParser.PAGE_WIDTH ? (float) PageParser.PAGE_WIDTH / width : 1.0F;
        float lift = (font.lineHeight - font.lineHeight * scale) / HALF;
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) center, top + TITLE_TEXT_Y + lift);
        graphics.pose().scale(scale, scale);
        graphics.text(font, title, -width / HALF, 0, TITLE_COLOR, false);
        graphics.pose().popMatrix();
    }
}
