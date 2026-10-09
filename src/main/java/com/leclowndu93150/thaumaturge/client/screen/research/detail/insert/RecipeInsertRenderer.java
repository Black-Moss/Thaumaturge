package com.leclowndu93150.thaumaturge.client.screen.research.detail.insert;

import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget.ItemHit;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.PreviewController;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.ArrowBob;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jspecify.annotations.Nullable;

public final class RecipeInsertRenderer {
    private static final int PAPER_CENTER = BookSprites.PAPER_LAYOUT_SPAN / 2;
    private static final int PAGER_LEFT_X = 40;
    private static final int PAGER_RIGHT_X = 204;
    private static final int PAGER_Y = 232;

    private RecipeInsertRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, PreviewController preview, int mouseX, int mouseY) {
        Identifier recipeId = model.openRecipe();
        if (recipeId == null) {
            return;
        }
        int paperLeft = frame.paperLeft();
        int paperTop = frame.paperTop();
        BookBlit.paper(graphics, paperLeft, paperTop);
        List<RecipeDisplay> variants = RecipeDisplayCache.get(recipeId);
        if (variants == null || variants.isEmpty()) {
            return;
        }
        long gameTime = frame.gameTime();
        model.setRecipeVariant(Mth.clamp(model.recipeVariant(), 0, variants.size() - 1));
        RecipeDisplay current = variants.get(model.recipeVariant());
        int centerX = paperLeft + PAPER_CENTER;
        int centerY = paperTop + PAPER_CENTER;
        int panelLeft = centerX - RecipeDisplayWidget.width() / 2;
        int panelTop = centerY - RecipeDisplayWidget.height() / 2;
        RecipeDisplayWidget.renderCrafting(graphics, panelLeft, panelTop, current, gameTime, preview.currentRotation(), preview.visibleLayer());
        preview.configure(graphics, frame.font(), centerX, centerY, RecipeDisplayWidget.multiblockLayerCount(current));
        ItemStack hovered = RecipeDisplayWidget.hoverStackForDisplay(panelLeft, panelTop, current, gameTime, mouseX, mouseY);
        if (hovered != null && !hovered.isEmpty()) {
            graphics.setTooltipForNextFrame(frame.font(), hovered, mouseX, mouseY);
        }
        List<Component> popup = RecipeDisplayWidget.hoverPopupForDisplay(panelLeft, panelTop, current, mouseX, mouseY);
        if (popup != null) {
            graphics.setTooltipForNextFrame(frame.font(), popup, Optional.empty(), mouseX, mouseY);
        }
        if (variants.size() > 1) {
            float swell = ArrowBob.swell(frame.player());
            if (model.recipeVariant() > 0) {
                BookBlit.swellingSprite(graphics, paperLeft + PAGER_LEFT_X, paperTop + PAGER_Y, BookSprites.ARROW_LEFT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT,
                        swell);
            }
            if (model.recipeVariant() < variants.size() - 1) {
                BookBlit.swellingSprite(graphics, paperLeft + PAGER_RIGHT_X, paperTop + PAGER_Y, BookSprites.ARROW_RIGHT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT,
                        swell);
            }
        }
    }

    public static @Nullable ItemHit hoverItem(EntryDetailModel model, int screenWidth, int screenHeight, double mouseX, double mouseY) {
        Identifier recipeId = model.openRecipe();
        if (!model.onlyRecipeOpen() || recipeId == null) {
            return null;
        }
        List<RecipeDisplay> variants = RecipeDisplayCache.get(recipeId);
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        RecipeDisplay current = variants.get(Mth.clamp(model.recipeVariant(), 0, variants.size() - 1));
        int paperLeft = (screenWidth - BookSprites.PAPER_SIZE) / 2;
        int paperTop = (screenHeight - BookSprites.PAPER_SIZE) / 2;
        return RecipeDisplayWidget.hoverItemForDisplay(paperLeft + BookSprites.PAPER_SIZE / 2 - RecipeDisplayWidget.width() / 2,
                paperTop + BookSprites.PAPER_SIZE / 2 - RecipeDisplayWidget.height() / 2, current, mouseX, mouseY);
    }
}
