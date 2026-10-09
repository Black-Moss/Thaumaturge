package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.ArrowBob;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class NavigationPainter {
    private static final int STAGE_LABEL_CENTER_X = 52;
    private static final int STAGE_LABEL_Y_SHIFT = 2;

    private NavigationPainter() {}

    public static void paint(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, int mouseX, int mouseY) {
        Player player = frame.player();
        float swell = ArrowBob.swell(player);
        int arrowRow = NavigationLayout.arrowRow(frame);
        boolean insertOpen = model.insertOpen();
        if (model.leftPage() > 0 && !insertOpen) {
            drawArrow(graphics, NavigationLayout.pageLeftDrawX(frame), arrowRow, BookSprites.ARROW_LEFT_U, swell);
        }
        if (model.hasNextSpread() && !insertOpen) {
            drawArrow(graphics, NavigationLayout.pageRightDrawX(frame), arrowRow, BookSprites.ARROW_RIGHT_U, swell);
        }
        paintBackButton(graphics, frame, model, swell, mouseX, mouseY);
        if (model.canNavigateStageHistory(player)) {
            paintStageHistory(graphics, frame, model, swell, mouseX, mouseY);
        }
    }

    private static void drawArrow(GuiGraphicsExtractor graphics, int x, int y, int u, float swell) {
        BookBlit.swellingSprite(graphics, x, y, u, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT, swell);
    }

    private static void paintBackButton(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, float swell, int mouseX, int mouseY) {
        Rect back = NavigationLayout.backButton(frame);
        boolean returnsToMap = !model.hasRecipeTrail() && !model.insertOpen();
        BookBlit.swellingSprite(graphics, back.x(), back.y(), NavigationLayout.BACK_U, NavigationLayout.BACK_V, back.width(), back.height(), returnsToMap ? 0.0F : swell);
        if (back.contains(mouseX, mouseY)) {
            String key = returnsToMap ? "gui.thaumaturge.thaumonomicon.return_to_map" : "gui.thaumaturge.thaumonomicon.back";
            graphics.text(frame.font(), Component.translatable(key), mouseX, mouseY, BookSprites.WHITE, true);
        }
    }

    private static void paintStageHistory(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, float swell, int mouseX, int mouseY) {
        Player player = frame.player();
        int displayedStage = model.displayedStage(player);
        int progressStage = model.progressStage(player);
        int drawRow = NavigationLayout.stageDrawRow(frame);
        if (displayedStage > 0) {
            drawArrow(graphics, NavigationLayout.stageLeftDrawX(frame), drawRow, BookSprites.ARROW_LEFT_U, swell);
            if (NavigationLayout.stageLeftHit(frame).contains(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(Component.translatable("gui.thaumaturge.thaumonomicon.previous_stage"), mouseX, mouseY);
            }
        }
        if (displayedStage < progressStage) {
            drawArrow(graphics, NavigationLayout.stageRightDrawX(frame), drawRow, BookSprites.ARROW_RIGHT_U, swell);
            if (NavigationLayout.stageRightHit(frame).contains(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(Component.translatable("gui.thaumaturge.thaumonomicon.next_stage"), mouseX, mouseY);
            }
        }
        Component label = Component.translatable("gui.thaumaturge.thaumonomicon.stage_history", displayedStage + 1, model.stages().size());
        graphics.text(frame.font(), label, frame.left() + STAGE_LABEL_CENTER_X - frame.font().width(label) / 2, drawRow + STAGE_LABEL_Y_SHIFT, BookSprites.INK_COLOR, false);
    }
}
