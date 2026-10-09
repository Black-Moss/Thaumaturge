package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public final class PageTextLayout {
    private static final int PAGE_WIDTH = PageParser.PAGE_WIDTH;
    private static final int PAGE_LEFT_OFFSET = -15;
    private static final int PAGE_SIDE_OFFSET = 152;
    private static final int CONTENT_Y_OFFSET = -10;
    private static final int TITLE_Y_ADVANCE = 28;
    private static final int LINE_HEIGHT = 9;
    private static final float PARAGRAPH_GAP_FACTOR = 0.66F;
    private static final int TEXT_BASELINE_SHIFT = 6;
    private static final int IMAGE_BASELINE_SHIFT = 5;
    private static final int IMAGE_GAP = 2;
    private static final int TITLE_COLOR = 0xFF202020;
    private static final int DIVIDER_INSET_X = 4;
    private static final int DIVIDER_ABOVE_OFFSET = -7;
    private static final int DIVIDER_BELOW_OFFSET = 10;
    private static final int LEFT_SIDE = 0;
    private static final int RIGHT_SIDE = 1;

    private PageTextLayout() {}

    public static List<PageParser.Page> compose(Font font, Identifier entryId, IResearchStage stage, List<ResearchAddendum> addenda, int knowledgeRows, int stageCount, int progressStage) {
        List<String> addendumKeys = addenda.stream().map(ResearchAddendum::textKey).toList();
        boolean reserveHistoryGap = stageCount > 1 && progressStage > 0 && !EntryDetailModel.hasRequirements(stage);
        return PageParser.parse(font, entryId, stage.textKey(), addendumKeys, knowledgeRows, false, !stage.requiredResearch().isEmpty(), !stage.obtain().isEmpty(), !stage.craft().isEmpty(),
                !stage.requiredKnowledge().isEmpty(), reserveHistoryGap);
    }

    public static int contentTop(DetailFrame frame) {
        return frame.top() + CONTENT_Y_OFFSET;
    }

    public static int titleBottom(DetailFrame frame) {
        return contentTop(frame) + TITLE_Y_ADVANCE;
    }

    public static void drawSpread(GuiGraphicsExtractor graphics, DetailFrame frame, Component title, List<PageParser.Page> pages, int leftIndex) {
        if (pages.isEmpty()) {
            return;
        }
        int top = contentTop(frame);
        if (leftIndex < pages.size()) {
            drawPage(graphics, frame, title, pages.get(leftIndex), LEFT_SIDE, top, leftIndex == 0);
        }
        if (leftIndex + 1 < pages.size()) {
            drawPage(graphics, frame, title, pages.get(leftIndex + 1), RIGHT_SIDE, top, false);
        }
    }

    private static void drawPage(GuiGraphicsExtractor graphics, DetailFrame frame, Component title, PageParser.Page page, int side, int top, boolean withTitle) {
        int cursorY = top;
        if (withTitle) {
            drawDivider(graphics, frame.left() + DIVIDER_INSET_X, cursorY + DIVIDER_ABOVE_OFFSET);
            drawTitle(graphics, frame, title, cursorY);
            drawDivider(graphics, frame.left() + DIVIDER_INSET_X, cursorY + DIVIDER_BELOW_OFFSET);
            cursorY += TITLE_Y_ADVANCE;
        }
        int columnX = frame.left() + PAGE_LEFT_OFFSET + side * PAGE_SIDE_OFFSET;
        for (PageParser.PageElement element : page.elements()) {
            switch (element) {
                case PageParser.PageElement.Text text -> cursorY += drawTextLine(graphics, frame.font(), text, columnX, cursorY);
                case PageParser.PageElement.Image image -> cursorY += drawImage(graphics, image.image(), columnX, cursorY);
            }
        }
    }

    private static int drawTextLine(GuiGraphicsExtractor graphics, Font font, PageParser.PageElement.Text text, int columnX, int lineY) {
        FormattedCharSequence sequence = sink -> StringDecomposer.iterateFormatted(text.content(), text.style(), sink);
        graphics.text(font, sequence, columnX, lineY - TEXT_BASELINE_SHIFT, BookSprites.INK_COLOR, false);
        return LINE_HEIGHT + (text.paragraphBreak() ? (int) (LINE_HEIGHT * PARAGRAPH_GAP_FACTOR) : 0);
    }

    private static int drawImage(GuiGraphicsExtractor graphics, PageParser.PageImage image, int columnX, int imageY) {
        int centering = (PAGE_WIDTH - image.renderedWidth()) / 2;
        graphics.pose().pushMatrix();
        graphics.pose().translate(columnX + centering, imageY - IMAGE_BASELINE_SHIFT);
        graphics.pose().scale(image.scale, image.scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, image.texture, 0, 0, (float) image.u, (float) image.v, image.w, image.h, image.w, image.h, TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
        return image.renderedHeight() + IMAGE_GAP;
    }

    private static void drawTitle(GuiGraphicsExtractor graphics, DetailFrame frame, Component title, int titleY) {
        Font font = frame.font();
        int titleWidth = font.width(title);
        int columnCenter = frame.left() + PAGE_LEFT_OFFSET + PAGE_WIDTH / 2;
        if (titleWidth <= PAGE_WIDTH) {
            graphics.text(font, title, columnCenter - titleWidth / 2, titleY, TITLE_COLOR, false);
            return;
        }
        float fitScale = (float) PAGE_WIDTH / titleWidth;
        graphics.pose().pushMatrix();
        graphics.pose().translate(frame.left() + PAGE_LEFT_OFFSET + PAGE_WIDTH / 2.0F - titleWidth / 2.0F * fitScale, titleY + fitScale);
        graphics.pose().scale(fitScale, fitScale);
        graphics.text(font, title, 0, 0, TITLE_COLOR, false);
        graphics.pose().popMatrix();
    }

    private static void drawDivider(GuiGraphicsExtractor graphics, int x, int y) {
        BookBlit.sprite(graphics, x, y, BookSprites.DIVIDER_U, BookSprites.DIVIDER_V, BookSprites.DIVIDER_WIDTH, BookSprites.DIVIDER_THIN_HEIGHT);
    }
}
