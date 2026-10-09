package com.leclowndu93150.thaumaturge.client.screen.research.detail.page;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class WarpIndicatorRenderer {
    private static final Identifier NODE_TEXTURE = TTIds.rl("textures/misc/auranodes.png");
    private static final int MAX_WARP_LEVEL = 5;
    private static final int NODE_OFFSET_X = -57;
    private static final int NODE_OFFSET_Y = -40;
    private static final int LABEL_OFFSET_X = -56;
    private static final int LABEL_OFFSET_Y = -43;
    private static final int LABEL_COLOR = 0xFFAA8FFF;
    private static final int HOVER_OFFSET_X = -67;
    private static final int HOVER_OFFSET_Y = -50;
    private static final int HOVER_SIZE = 20;
    private static final int NODE_CELL_PIXELS = 64;
    private static final int NODE_FRAME_COUNT = 32;
    private static final int NODE_SHEET_ROW = 5;
    private static final int NODE_DRAW_SIZE = 90;
    private static final int NODE_SHEET_SIZE = 2048;
    private static final int NODE_TINT = 0xE5540070;

    private WarpIndicatorRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, IResearchStage stage, int mouseX, int mouseY) {
        Player player = frame.player();
        if (model.isComplete() && model.displayedStage(player) == model.progressStage(player)) {
            return;
        }
        int warp = Math.min(stage.warp(), MAX_WARP_LEVEL);
        if (warp <= 0) {
            return;
        }
        int anchorX = frame.left();
        int anchorY = PageTextLayout.titleBottom(frame);
        drawNode(graphics, frame, anchorX + NODE_OFFSET_X, anchorY + NODE_OFFSET_Y);
        Component level = Component.translatable("gui.thaumaturge.thaumonomicon.warp_level." + warp);
        graphics.text(frame.font(), level, anchorX + LABEL_OFFSET_X - frame.font().width(level) / 2, anchorY + LABEL_OFFSET_Y, LABEL_COLOR, false);
        if (Rect.inside(anchorX + HOVER_OFFSET_X, anchorY + HOVER_OFFSET_Y, HOVER_SIZE, HOVER_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(frame.font(), Component.translatable("gui.thaumaturge.thaumonomicon.warp_warning", level), mouseX, mouseY);
        }
    }

    private static void drawNode(GuiGraphicsExtractor graphics, DetailFrame frame, int centerX, int centerY) {
        int cellU = frame.tick() % NODE_FRAME_COUNT * NODE_CELL_PIXELS;
        int cellV = NODE_SHEET_ROW * NODE_CELL_PIXELS;
        int half = NODE_DRAW_SIZE / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, NODE_TEXTURE, centerX - half, centerY - half, (float) cellU, (float) cellV, NODE_DRAW_SIZE, NODE_DRAW_SIZE, NODE_CELL_PIXELS, NODE_CELL_PIXELS,
                NODE_SHEET_SIZE, NODE_SHEET_SIZE, NODE_TINT);
    }
}
