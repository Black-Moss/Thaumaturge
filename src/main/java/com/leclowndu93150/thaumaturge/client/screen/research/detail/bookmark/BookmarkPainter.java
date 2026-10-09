package com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.pip.BlockPreviews;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.HitRecorder;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.insert.ConstructInsertRenderer;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class BookmarkPainter {
    private static final int SIDE_BODY_U_ASPECTS = 76;
    private static final int SIDE_BODY_U_KNOWLEDGE = 44;
    private static final int SIDE_BODY_OFFSET_ASPECTS = 0;
    private static final int SIDE_BODY_OFFSET_KNOWLEDGE = -1;
    private static final int SIDE_TIP_OFFSET_ASPECTS = 20;
    private static final int SIDE_TIP_OFFSET_KNOWLEDGE = 19;
    private static final int SIDE_V = 232;
    private static final int SIDE_BODY_WIDTH = 24;
    private static final int SIDE_IDLE_INSET = 3;
    private static final int SIDE_TIP_U = 100;
    private static final int SIDE_TIP_WIDTH = 4;

    private static final int RECIPE_BODY_U = 120;
    private static final int RECIPE_V = 232;
    private static final int RECIPE_WIDTH = 28;
    private static final int RECIPE_HEIGHT = 16;
    private static final int RECIPE_TIP_U = 116;
    private static final int RECIPE_TIP_WIDTH = 4;
    private static final int RECIPE_ICON_OFFSET = 7;
    private static final int RECIPE_IDLE_LEAN = 3;
    private static final int RECIPE_SELECTED_TINT = 0xFFFF8080;
    private static final int RECIPE_IDLE_TINT = 0xFFFFFFFF;
    private static final int RECIPE_CYCLE_TICKS = 20;
    private static final int CONSTRUCT_ICON_CENTER_X = 8;
    private static final int CONSTRUCT_ICON_CENTER_Y = 8;
    private static final int CONSTRUCT_ICON_SIZE = 16;
    private static final float CONSTRUCT_ICON_ROTATION = -35.0F;
    private static final int CONSTRUCT_ICON_ALL_LAYERS = -1;

    private final HitRecorder hits;

    public BookmarkPainter(HitRecorder hits) {
        this.hits = hits;
    }

    public void paint(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, IResearchStage stage, List<BookmarkSlot> slots, BookmarkJitter jitter, int mouseX, int mouseY) {
        for (BookmarkSlot slot : slots) {
            switch (slot.kind()) {
                case ASPECTS -> paintSide(graphics, frame, slot, SIDE_BODY_U_ASPECTS, SIDE_BODY_OFFSET_ASPECTS, SIDE_TIP_OFFSET_ASPECTS, "gui.thaumaturge.thaumonomicon.aspects_title", mouseX, mouseY);
                case KNOWLEDGE ->
                    paintSide(graphics, frame, slot, SIDE_BODY_U_KNOWLEDGE, SIDE_BODY_OFFSET_KNOWLEDGE, SIDE_TIP_OFFSET_KNOWLEDGE, "gui.thaumaturge.thaumonomicon.knowledge_title", mouseX, mouseY);
                case RECIPE -> paintRecipe(graphics, frame, model, slot, jitter, mouseX, mouseY);
                case CONSTRUCT -> paintConstruct(graphics, frame, model, stage, slot, jitter, mouseX, mouseY);
            }
        }
    }

    private void paintSide(GuiGraphicsExtractor graphics, DetailFrame frame, BookmarkSlot slot, int bodyU, int bodyOffset, int tipOffset, String tooltipKey, int mouseX, int mouseY) {
        Rect area = slot.hoverArea();
        boolean hovered = area.contains(mouseX, mouseY);
        int inset = hovered ? 0 : SIDE_IDLE_INSET;
        BookBlit.sprite(graphics, area.x() + bodyOffset + inset, area.y(), bodyU, SIDE_V, SIDE_BODY_WIDTH - inset, BookmarkLayout.SIDE_HEIGHT);
        BookBlit.sprite(graphics, area.x() + tipOffset, area.y(), SIDE_TIP_U, SIDE_V, SIDE_TIP_WIDTH, BookmarkLayout.SIDE_HEIGHT);
        if (hovered) {
            graphics.setTooltipForNextFrame(frame.font(), Component.translatable(tooltipKey), mouseX, mouseY);
        }
    }

    private void paintRecipe(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, BookmarkSlot slot, BookmarkJitter jitter, int mouseX, int mouseY) {
        List<RecipeDisplay> variants = RecipeDisplayCache.get(slot.recipeId());
        if (variants == null || variants.isEmpty()) {
            return;
        }
        RecipeDisplay shown = variants.get(frame.tick() / RECIPE_CYCLE_TICKS % variants.size());
        ItemStack result = resultOf(shown, frame.level());
        Rect area = slot.hoverArea();
        boolean hovered = area.contains(mouseX, mouseY);
        int shift = jitter.shift(slot.ordinal());
        int lean = jitter.lean(slot.ordinal()) + (hovered ? 0 : RECIPE_IDLE_LEAN);
        int tint = slot.recipeId().equals(model.openRecipe()) ? RECIPE_SELECTED_TINT : RECIPE_IDLE_TINT;
        drawTab(graphics, area, shift, lean, tint);
        int iconX = area.x() + shift + RECIPE_ICON_OFFSET - lean;
        if (!result.isEmpty()) {
            hits.recordItem(result, iconX, area.y());
        }
        RecipeDisplayWidget.renderBookmarkIcon(graphics, iconX, area.y(), shown);
        if (hovered && !result.isEmpty()) {
            graphics.setTooltipForNextFrame(frame.font(), result, mouseX, mouseY);
        }
    }

    private void paintConstruct(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, IResearchStage stage, BookmarkSlot slot, BookmarkJitter jitter, int mouseX, int mouseY) {
        Rect area = slot.hoverArea();
        boolean hovered = area.contains(mouseX, mouseY);
        int shift = jitter.shift(slot.ordinal());
        int lean = jitter.lean(slot.ordinal()) + (hovered ? 0 : RECIPE_IDLE_LEAN);
        drawTab(graphics, area, shift, lean, model.constructOpen() ? RECIPE_SELECTED_TINT : RECIPE_IDLE_TINT);
        BlockPreviews.render(graphics, area.x() + shift + RECIPE_ICON_OFFSET - lean + CONSTRUCT_ICON_CENTER_X, area.y() + CONSTRUCT_ICON_CENTER_Y,
                ConstructInsertRenderer.constructBlocks(stage.construct().orElseThrow(), frame.gameTime()), CONSTRUCT_ICON_SIZE, CONSTRUCT_ICON_SIZE, CONSTRUCT_ICON_SIZE, CONSTRUCT_ICON_ROTATION,
                CONSTRUCT_ICON_ALL_LAYERS);
        if (hovered) {
            graphics.setTooltipForNextFrame(frame.font(), Component.translatable("gui.thaumaturge.recipe_type.construct"), mouseX, mouseY);
        }
    }

    private static void drawTab(GuiGraphicsExtractor graphics, Rect area, int shift, int lean, int tint) {
        BookBlit.tintedSprite(graphics, area.x() + shift, area.y(), RECIPE_BODY_U + lean, RECIPE_V, RECIPE_WIDTH, RECIPE_HEIGHT, tint);
        BookBlit.sprite(graphics, area.x() + shift, area.y(), RECIPE_TIP_U, RECIPE_V, RECIPE_TIP_WIDTH, RECIPE_HEIGHT);
    }

    private static ItemStack resultOf(RecipeDisplay display, @Nullable Level level) {
        try {
            return display.result().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
        } catch (RuntimeException unresolved) {
            return ItemStack.EMPTY;
        }
    }
}
