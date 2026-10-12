package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class ConnectorRenderer {
    private static final int CELL = 24;
    private static final int HALF_CELL = CELL / 2;
    private static final int TILE_INSET = 4;
    private static final int ICON_CENTER = 8;

    private static final int VERTICAL_U = 0;
    private static final int HORIZONTAL_U = 24;
    private static final int STRAIGHT_V = 228;

    private static final int SMALL_V = 228;
    private static final int SMALL_WEST_NORTH_U = 48;
    private static final int SMALL_NORTH_EAST_U = 72;
    private static final int SMALL_WEST_SOUTH_U = 96;
    private static final int SMALL_EAST_SOUTH_U = 120;

    private static final int LARGE_SIZE = 2 * CELL;
    private static final int LARGE_V = 180;
    private static final int LARGE_WEST_NORTH_U = 0;
    private static final int LARGE_NORTH_EAST_U = 48;
    private static final int LARGE_WEST_SOUTH_U = 96;
    private static final int LARGE_EAST_SOUTH_U = 144;

    private static final int ARROW_LONG = 7;
    private static final int ARROW_SHORT = 6;
    private static final int ARROW_DOWN_U = 77;
    private static final int ARROW_DOWN_V = 112;
    private static final int ARROW_UP_U = 109;
    private static final int ARROW_UP_V = 138;
    private static final int ARROW_RIGHT_U = 128;
    private static final int ARROW_RIGHT_V = 125;
    private static final int ARROW_LEFT_U = 186;
    private static final int ARROW_LEFT_V = 125;
    private static final int ARROW_TIP_GAP = 10;
    private static final int ARROW_SIDE = 3;

    private final List<Piece> pending = new ArrayList<>();

    public ConnectorRenderer() {}

    public void draw(GuiGraphicsExtractor graphics, int sourceCol, int sourceRow, int parentCol, int parentRow, int originX, int originY, int color, float zModifier, boolean arrow, boolean flip) {
        if (sourceCol == parentCol && sourceRow == parentRow) {
            return;
        }
        Grid grid = new Grid(originX, originY, color, zModifier, pending);
        int verticalCol = flip ? parentCol : sourceCol;
        int verticalRow = flip ? parentRow : sourceRow;
        int horizontalCol = flip ? sourceCol : parentCol;
        int horizontalRow = flip ? sourceRow : parentRow;
        route(grid, verticalCol, verticalRow, horizontalCol, horizontalRow);
        if (arrow) {
            Heading heading = arrival(sourceCol, sourceRow, parentCol, parentRow, flip);
            grid.arrow(sourceCol, sourceRow, heading);
        }
    }

    public void flush(GuiGraphicsExtractor graphics) {
        pending.sort(Comparator.comparingDouble(Piece::layer));
        for (Piece piece : pending) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, piece.x(), piece.y(), (float) piece.u(), (float) piece.v(), piece.width(), piece.height(),
                    TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE, piece.color());
        }
        pending.clear();
    }

    private static void route(Grid grid, int verticalCol, int verticalRow, int horizontalCol, int horizontalRow) {
        int rowsToTurn = horizontalRow - verticalRow;
        int colsAfterTurn = horizontalCol - verticalCol;
        if (colsAfterTurn == 0 || rowsToTurn == 0) {
            straight(grid, verticalCol, verticalRow, horizontalCol, horizontalRow);
            return;
        }
        int stepRow = Integer.signum(rowsToTurn);
        int stepCol = Integer.signum(colsAfterTurn);
        Side towardVertical = stepRow > 0 ? Side.NORTH : Side.SOUTH;
        Side towardHorizontal = stepCol > 0 ? Side.EAST : Side.WEST;
        boolean wide = Math.abs(rowsToTurn) > 1 && Math.abs(colsAfterTurn) > 1;
        int legEndRow = horizontalRow - (wide ? stepRow : 0);
        int legStartCol = verticalCol + (wide ? stepCol : 0);
        grid.half(verticalCol, verticalRow, towardVertical.opposite());
        for (int row = verticalRow + stepRow; row != legEndRow; row += stepRow) {
            grid.vertical(verticalCol, row);
        }
        if (wide) {
            grid.largeCorner(verticalCol, horizontalRow, stepCol, -stepRow, Turn.of(towardVertical, towardHorizontal));
        } else {
            grid.smallCorner(verticalCol, horizontalRow, Turn.of(towardVertical, towardHorizontal));
        }
        for (int col = legStartCol + stepCol; col != horizontalCol; col += stepCol) {
            grid.horizontal(col, horizontalRow);
        }
        grid.half(horizontalCol, horizontalRow, towardHorizontal.opposite());
    }

    private static void straight(Grid grid, int fromCol, int fromRow, int toCol, int toRow) {
        int stepCol = Integer.signum(toCol - fromCol);
        int stepRow = Integer.signum(toRow - fromRow);
        boolean upright = stepCol == 0;
        Side leaving = upright ? (stepRow > 0 ? Side.SOUTH : Side.NORTH) : (stepCol > 0 ? Side.EAST : Side.WEST);
        grid.half(fromCol, fromRow, leaving);
        for (int col = fromCol + stepCol, row = fromRow + stepRow; col != toCol || row != toRow; col += stepCol, row += stepRow) {
            if (upright) {
                grid.vertical(col, row);
            } else {
                grid.horizontal(col, row);
            }
        }
        grid.half(toCol, toRow, leaving.opposite());
    }

    private static Heading arrival(int sourceCol, int sourceRow, int parentCol, int parentRow, boolean flip) {
        boolean entersSideways = flip ? sourceCol != parentCol : sourceRow == parentRow;
        if (entersSideways) {
            return sourceCol > parentCol ? Heading.EAST : Heading.WEST;
        }
        return sourceRow > parentRow ? Heading.SOUTH : Heading.NORTH;
    }

    private enum Side {
        NORTH, SOUTH, EAST, WEST;

        Side opposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case EAST -> WEST;
                case WEST -> EAST;
            };
        }
    }

    private enum Turn {
        WEST_NORTH(SMALL_WEST_NORTH_U, LARGE_WEST_NORTH_U), NORTH_EAST(SMALL_NORTH_EAST_U, LARGE_NORTH_EAST_U), WEST_SOUTH(SMALL_WEST_SOUTH_U, LARGE_WEST_SOUTH_U), EAST_SOUTH(SMALL_EAST_SOUTH_U,
                LARGE_EAST_SOUTH_U);

        private final int smallU;
        private final int largeU;

        Turn(int smallU, int largeU) {
            this.smallU = smallU;
            this.largeU = largeU;
        }

        static Turn of(Side vertical, Side horizontal) {
            boolean north = vertical == Side.NORTH;
            boolean east = horizontal == Side.EAST;
            if (north) {
                return east ? NORTH_EAST : WEST_NORTH;
            }
            return east ? EAST_SOUTH : WEST_SOUTH;
        }
    }

    private enum Heading {
        NORTH(ARROW_UP_U, ARROW_UP_V, ARROW_LONG, ARROW_SHORT, -ARROW_SIDE, ARROW_TIP_GAP), SOUTH(ARROW_DOWN_U, ARROW_DOWN_V, ARROW_LONG, ARROW_SHORT, -ARROW_SIDE, -ARROW_TIP_GAP - ARROW_SHORT), EAST(
                ARROW_RIGHT_U, ARROW_RIGHT_V, ARROW_SHORT, ARROW_LONG, -ARROW_TIP_GAP - ARROW_SHORT,
                -ARROW_SIDE), WEST(ARROW_LEFT_U, ARROW_LEFT_V, ARROW_SHORT, ARROW_LONG, ARROW_TIP_GAP, -ARROW_SIDE);

        private final int u;
        private final int v;
        private final int width;
        private final int height;
        private final int offsetX;
        private final int offsetY;

        Heading(int u, int v, int width, int height, int offsetX, int offsetY) {
            this.u = u;
            this.v = v;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private record Grid(int originX, int originY, int color, float layer, List<Piece> sink) {
        int tileX(int col) {
            return originX + col * CELL - TILE_INSET;
        }

        int tileY(int row) {
            return originY + row * CELL - TILE_INSET;
        }

        void vertical(int col, int row) {
            add(tileX(col), tileY(row), VERTICAL_U, STRAIGHT_V, CELL, CELL);
        }

        void horizontal(int col, int row) {
            add(tileX(col), tileY(row), HORIZONTAL_U, STRAIGHT_V, CELL, CELL);
        }

        void half(int col, int row, Side toward) {
            int x = tileX(col);
            int y = tileY(row);
            switch (toward) {
                case NORTH -> add(x, y, VERTICAL_U, STRAIGHT_V, CELL, HALF_CELL);
                case SOUTH -> add(x, y + HALF_CELL, VERTICAL_U, STRAIGHT_V + HALF_CELL, CELL, HALF_CELL);
                case WEST -> add(x, y, HORIZONTAL_U, STRAIGHT_V, HALF_CELL, CELL);
                case EAST -> add(x + HALF_CELL, y, HORIZONTAL_U + HALF_CELL, STRAIGHT_V, HALF_CELL, CELL);
            }
        }

        void smallCorner(int col, int row, Turn turn) {
            add(tileX(col), tileY(row), turn.smallU, SMALL_V, CELL, CELL);
        }

        void largeCorner(int col, int row, int stepCol, int stepRow, Turn turn) {
            int blockCol = Math.min(col, col + stepCol);
            int blockRow = Math.min(row, row + stepRow);
            add(tileX(blockCol), tileY(blockRow), turn.largeU, LARGE_V, LARGE_SIZE, LARGE_SIZE);
        }

        void arrow(int col, int row, Heading heading) {
            int centerX = originX + col * CELL + ICON_CENTER;
            int centerY = originY + row * CELL + ICON_CENTER;
            add(centerX + heading.offsetX, centerY + heading.offsetY, heading.u, heading.v, heading.width, heading.height);
        }

        private void add(int x, int y, int u, int v, int width, int height) {
            sink.add(new Piece(x, y, u, v, width, height, color, layer));
        }
    }

    private record Piece(int x, int y, int u, int v, int width, int height, int color, float layer) {
    }
}
