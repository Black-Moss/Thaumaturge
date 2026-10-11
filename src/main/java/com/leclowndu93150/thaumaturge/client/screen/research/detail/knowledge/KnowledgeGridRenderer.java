package com.leclowndu93150.thaumaturge.client.screen.research.detail.knowledge;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TTTooltips;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.DetailFrame;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookBlit;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.draw.BookSprites;
import com.leclowndu93150.thaumaturge.client.screen.research.detail.page.PageTextLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class KnowledgeGridRenderer {
    private static final int CELL = 16;
    private static final int HALF = 2;
    private static final int BADGE_OFFSET = 6;
    private static final float BADGE_SCALE = 0.66F;
    private static final int BADGE_TINT = 0xD2FFFFFF;
    private static final int LEVEL_RIGHT = 17;
    private static final int LEVEL_TOP = 9;
    private static final int LEVEL_COLOR = 0xFFFFFFFF;
    private static final int BAR_TOP = 17;
    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_U = 0;
    private static final int BAR_FILL_V = 232;
    private static final int BAR_TRACK_V = 234;
    private static final int CELL_FOOTPRINT = BAR_TOP + BAR_HEIGHT;

    private static final int INLINE_BOTTOM_INSET = 23;
    private static final int INLINE_DIVIDER_GAP = 11;
    private static final int INLINE_PADDING = 3;

    private static final int INSERT_TITLE_Y = 26;
    private static final int INSERT_TEXT_COLOR = 0xFF4A4440;
    private static final int INSERT_NONE_WIDTH = 168;

    private static final String TITLE_KEY = "gui.thaumaturge.thaumonomicon.knowledge_title";
    private static final String NONE_KEY = "gui.thaumaturge.thaumonomicon.knowledge_none";

    private KnowledgeGridRenderer() {}

    public static Optional<List<Holder.Reference<IResearchCategory>>> categories(Player player) {
        Comparator<Holder.Reference<IResearchCategory>> order = Comparator.comparingInt(reference -> reference.value().index());
        return player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY)
                .map(lookup -> lookup.listElements().sorted(order.thenComparing(reference -> reference.key().identifier().toString())).toList());
    }

    public static int activeTypeCount(@Nullable Player player) {
        return player == null ? 0 : rows(player).size();
    }

    public static boolean hasAnyKnowledge(@Nullable Player player) {
        return activeTypeCount(player) > 0;
    }

    public static int inlineReserve(int rowCount) {
        if (rowCount <= 0) {
            return 0;
        }
        return INLINE_BOTTOM_INSET + (rowCount - 1) * KnowledgeGridLayout.IN_PAGE.rowStride() + INLINE_DIVIDER_GAP + INLINE_PADDING;
    }

    public static void drawInPage(GuiGraphicsExtractor graphics, DetailFrame frame, int mouseX, int mouseY) {
        List<Row> rows = rows(frame.player());
        if (rows.isEmpty()) {
            return;
        }
        KnowledgeGridLayout layout = KnowledgeGridLayout.IN_PAGE;
        int center = PageTextLayout.columnCenter(frame);
        int bottomRowY = PageTextLayout.pageBottom(frame) - INLINE_BOTTOM_INSET;
        int topRowY = bottomRowY - (rows.size() - 1) * layout.rowStride();
        BookBlit.sprite(graphics, center - BookSprites.DIVIDER_WIDTH / HALF, topRowY - INLINE_DIVIDER_GAP, BookSprites.DIVIDER_U, BookSprites.DIVIDER_V, BookSprites.DIVIDER_WIDTH,
                BookSprites.DIVIDER_TALL_HEIGHT);
        int pitch = layout.columnStride(categoryCount(frame.player()));
        for (int index = 0; index < rows.size(); index++) {
            drawRow(graphics, frame.font(), rows.get(index), center, bottomRowY - index * layout.rowStride(), pitch, mouseX, mouseY);
        }
    }

    public static void drawInsert(GuiGraphicsExtractor graphics, DetailFrame frame, int mouseX, int mouseY) {
        int paperLeft = frame.paperLeft();
        int paperTop = frame.paperTop();
        BookBlit.paper(graphics, paperLeft, paperTop);
        Font font = frame.font();
        int centerX = paperLeft + BookSprites.PAPER_LAYOUT_SPAN / HALF;
        int centerY = paperTop + BookSprites.PAPER_LAYOUT_SPAN / HALF;
        Component title = Component.translatable(TITLE_KEY);
        graphics.text(font, title, centerX - font.width(title) / HALF, paperTop + INSERT_TITLE_Y, INSERT_TEXT_COLOR, false);
        List<Row> rows = rows(frame.player());
        if (rows.isEmpty()) {
            drawNothingYet(graphics, font, centerX, centerY);
            return;
        }
        KnowledgeGridLayout layout = KnowledgeGridLayout.INSERT;
        int gridHeight = (rows.size() - 1) * layout.rowStride() + CELL_FOOTPRINT;
        int bottomRowY = centerY + gridHeight / HALF - CELL_FOOTPRINT;
        int pitch = layout.columnStride(categoryCount(frame.player()));
        for (int index = 0; index < rows.size(); index++) {
            drawRow(graphics, font, rows.get(index), centerX, bottomRowY - index * layout.rowStride(), pitch, mouseX, mouseY);
        }
    }

    private static void drawNothingYet(GuiGraphicsExtractor graphics, Font font, int centerX, int centerY) {
        List<FormattedCharSequence> lines = font.split(Component.translatable(NONE_KEY), INSERT_NONE_WIDTH);
        int y = centerY - lines.size() * font.lineHeight / HALF;
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, centerX - font.width(line) / HALF, y, INSERT_TEXT_COLOR, false);
            y += font.lineHeight;
        }
    }

    private static int categoryCount(Player player) {
        return Math.max(1, categories(player).map(List::size).orElse(1));
    }

    private static List<Row> rows(Player player) {
        List<Holder.Reference<IResearchCategory>> categories = categories(player).orElse(List.of());
        PlayerResearch research = PlayerResearchAccess.of(player);
        List<Row> rows = new ArrayList<>();
        for (KnowledgeType type : KnowledgeType.values()) {
            List<Cell> cells = new ArrayList<>();
            for (Holder.Reference<IResearchCategory> category : categories) {
                int points = research.knowledgePoints(type, category.key());
                if (points > 0) {
                    cells.add(new Cell(category.key(), category.value().icon(), points / type.progression(), points % type.progression()));
                }
            }
            if (!cells.isEmpty()) {
                rows.add(new Row(type, cells));
            }
        }
        return rows;
    }

    private static void drawRow(GuiGraphicsExtractor graphics, Font font, Row row, int centerX, int y, int pitch, int mouseX, int mouseY) {
        int rowWidth = (row.cells().size() - 1) * pitch + CELL;
        int x = centerX - rowWidth / HALF;
        for (Cell cell : row.cells()) {
            drawCell(graphics, font, row.type(), cell, x, y);
            if (Rect.inside(x, y, CELL, CELL, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, TTTooltips.knowledgeLabel(row.type(), cell.category()), mouseX, mouseY);
            }
            x += pitch;
        }
    }

    private static void drawCell(GuiGraphicsExtractor graphics, Font font, KnowledgeType type, Cell cell, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, kindIcon(type), x, y, 0.0F, 0.0F, CELL, CELL, CELL, CELL);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) (x + BADGE_OFFSET), (float) (y + BADGE_OFFSET));
        graphics.pose().scale(BADGE_SCALE, BADGE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, cell.icon(), 0, 0, 0.0F, 0.0F, CELL, CELL, CELL, CELL, BADGE_TINT);
        graphics.pose().popMatrix();
        String level = Integer.toString(cell.level());
        graphics.text(font, level, x + LEVEL_RIGHT - font.width(level), y + LEVEL_TOP, LEVEL_COLOR, true);
        if (cell.partial() > 0) {
            int filled = Math.max(1, cell.partial() * BAR_WIDTH / type.progression());
            BookBlit.sprite(graphics, x, y + BAR_TOP, BAR_U, BAR_TRACK_V, BAR_WIDTH, BAR_HEIGHT);
            BookBlit.sprite(graphics, x, y + BAR_TOP, BAR_U, BAR_FILL_V, filled, BAR_HEIGHT);
        }
    }

    private static Identifier kindIcon(KnowledgeType type) {
        return switch (type) {
            case THEORY -> TTScreenTextures.RESEARCH_KNOWLEDGE_THEORY;
            case OBSERVATION -> TTScreenTextures.RESEARCH_KNOWLEDGE_OBSERVATION;
        };
    }

    private record Row(KnowledgeType type, List<Cell> cells) {
    }

    private record Cell(ResourceKey<IResearchCategory> category, Identifier icon, int level, int partial) {
    }
}
