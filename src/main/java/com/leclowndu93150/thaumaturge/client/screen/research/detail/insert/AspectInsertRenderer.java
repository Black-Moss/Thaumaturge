package com.leclowndu93150.thaumaturge.client.screen.research.detail.insert;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.EntryDetailModel;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.ArrowBob;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.AspectTileDrawer;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class AspectInsertRenderer {
    private static final int ROWS_PER_PAGE = 5;
    private static final int ROW_PITCH = 40;
    private static final int LIST_X = 60;
    private static final int LIST_Y = 24;
    private static final int HALF = 2;

    private static final int MAIN_X = 2;
    private static final int MAIN_Y = 2;
    private static final float MAIN_SCALE = 1.5F;
    private static final int MAIN_NAME_CENTER_X = 16;
    private static final int EQUALS_X = 41;
    private static final int SYMBOL_Y = 12;
    private static final int FIRST_PART_X = 60;
    private static final int SECOND_PART_X = 102;
    private static final int PART_Y = 4;
    private static final float PART_SCALE = 1.25F;
    private static final int FIRST_NAME_CENTER_X = 72;
    private static final int SECOND_NAME_CENTER_X = 114;
    private static final int PLUS_X = 89;
    private static final int PRIMAL_X = 54;
    private static final int NAME_Y = 29;
    private static final float NAME_SCALE = 0.5F;
    private static final int HOVER_SIZE = 40;
    private static final int COMPOUND_PARTS = 2;

    private static final int PREVIOUS_X = -20;
    private static final int NEXT_X = 144;
    private static final int PAGER_Y = 208;

    private static final int NAME_COLOR = 0xFF6B6560;
    private static final int SYMBOL_COLOR = 0xFF2E2A27;
    private static final int PRIMAL_COLOR = 0xFF7D7772;
    private static final float HOVER_BACKDROP_ALPHA = 0.35F;

    private static final String EQUALS = "=";
    private static final String PLUS = "+";
    private static final String PRIMAL_KEY = "tooltip.thaumaturge.aspect.primal";
    private static final int LISTED_AMOUNT = 1;

    private AspectInsertRenderer() {}

    public static AspectList listedAspects(@Nullable Player player) {
        if (player == null) {
            return AspectList.EMPTY;
        }
        List<AspectInstance> known = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(aspect -> AspectPools.isDiscovered(player, aspect))
                .sorted(Comparator.comparing(aspect -> AspectComponents.name(aspect).getString())).map(aspect -> new AspectInstance(aspect, LISTED_AMOUNT)).toList();
        return AspectList.ofEntries(known);
    }

    public static int pageCount(@Nullable Player player) {
        int listed = listedAspects(player).size();
        return Math.max(1, (listed + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    public static void render(GuiGraphicsExtractor graphics, DetailFrame frame, EntryDetailModel model, int mouseX, int mouseY) {
        int paperLeft = frame.paperLeft();
        int paperTop = frame.paperTop();
        BookBlit.paper(graphics, paperLeft, paperTop);
        Player player = frame.player();
        List<AspectInstance> listed = listedAspects(player).entries();
        int pages = Math.max(1, (listed.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        int page = Mth.clamp(model.aspectsPageIndex(), 0, pages - 1);
        int originX = paperLeft + LIST_X;
        int originY = paperTop + LIST_Y;
        int first = page * ROWS_PER_PAGE;
        for (int slot = 0; slot < ROWS_PER_PAGE && first + slot < listed.size(); slot++) {
            drawRow(graphics, frame.font(), player, listed.get(first + slot).aspect(), originX, originY + slot * ROW_PITCH, mouseX, mouseY);
        }
        float swell = ArrowBob.swell(player);
        if (page > 0) {
            BookBlit.swellingSprite(graphics, originX + PREVIOUS_X, originY + PAGER_Y, BookSprites.ARROW_LEFT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT, swell);
        }
        if (page < pages - 1) {
            BookBlit.swellingSprite(graphics, originX + NEXT_X, originY + PAGER_Y, BookSprites.ARROW_RIGHT_U, BookSprites.ARROW_V, BookSprites.ARROW_WIDTH, BookSprites.ARROW_HEIGHT, swell);
        }
    }

    private static void drawRow(GuiGraphicsExtractor graphics, Font font, Player player, Holder<IAspect> aspect, int x, int y, int mouseX, int mouseY) {
        if (Rect.inside(x, y, HOVER_SIZE, HOVER_SIZE, mouseX, mouseY)) {
            drawScaled(graphics, x + MAIN_X, y + MAIN_Y, MAIN_SCALE, () -> BookBlit.iconTile(graphics, AspectTileDrawer.BACKDROP_TILE, 0, 0, BookBlit.alphaTint(HOVER_BACKDROP_ALPHA)));
        }
        drawScaled(graphics, x + MAIN_X, y + MAIN_Y, MAIN_SCALE, () -> AspectTileDrawer.known(graphics, 0, 0, aspect));
        drawSmallName(graphics, font, AspectComponents.name(aspect), x + MAIN_NAME_CENTER_X, y + NAME_Y);
        List<Holder<IAspect>> parts = aspect.value().components();
        if (aspect.value().isPrimal() || parts.size() != COMPOUND_PARTS) {
            graphics.text(font, Component.translatable(PRIMAL_KEY), x + PRIMAL_X, y + SYMBOL_Y, PRIMAL_COLOR, false);
            return;
        }
        graphics.text(font, EQUALS, x + EQUALS_X, y + SYMBOL_Y, SYMBOL_COLOR, false);
        graphics.text(font, PLUS, x + PLUS_X, y + SYMBOL_Y, SYMBOL_COLOR, false);
        drawPart(graphics, font, player, parts.get(0), x + FIRST_PART_X, y, x + FIRST_NAME_CENTER_X);
        drawPart(graphics, font, player, parts.get(1), x + SECOND_PART_X, y, x + SECOND_NAME_CENTER_X);
    }

    private static void drawPart(GuiGraphicsExtractor graphics, Font font, Player player, Holder<IAspect> part, int x, int rowY, int nameCenterX) {
        if (!AspectPools.isDiscovered(player, part)) {
            drawScaled(graphics, x, rowY + PART_Y, PART_SCALE, () -> AspectTileDrawer.unrevealed(graphics));
            return;
        }
        drawScaled(graphics, x, rowY + PART_Y, PART_SCALE, () -> AspectTileDrawer.known(graphics, 0, 0, part));
        drawSmallName(graphics, font, AspectComponents.name(part), nameCenterX, rowY + NAME_Y);
    }

    private static void drawScaled(GuiGraphicsExtractor graphics, int x, int y, float scale, Runnable drawing) {
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) x, (float) y);
        graphics.pose().scale(scale, scale);
        drawing.run();
        graphics.pose().popMatrix();
    }

    private static void drawSmallName(GuiGraphicsExtractor graphics, Font font, Component name, int centerX, int top) {
        int width = font.width(name);
        drawScaled(graphics, centerX, top, NAME_SCALE, () -> graphics.text(font, name, -width / HALF, 0, NAME_COLOR, false));
    }
}
