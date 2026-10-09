package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class ConnectorRenderer {
    public static final int CELL_SIZE = 24;
    public static final int COLOR_PARENT_KNOWN = 0xFF999999;
    public static final int COLOR_PARENT_UNKNOWN = 0xFF333333;
    public static final int COLOR_SIBLING_KNOWN = 0xFF4C4C66;
    public static final int COLOR_SIBLING_UNKNOWN = 0xFF2F2F3F;

    private static final int PIECE_INSET = 4;
    private static final int ARROW_INSET = 8;
    private static final int BIG_CORNER_SHIFT = CELL_SIZE;
    private static final int BIG_CORNER_SPAN = 2;
    private static final int ARROW_SIZE = 32;
    private static final int BIG_SIZE = 48;
    private static final int STRAIGHT_V = 228;
    private static final int BIG_CORNER_V = 180;
    private static final int ARROW_V = 112;

    private static final ThreadLocal<List<Piece>> PENDING = ThreadLocal.withInitial(ArrayList::new);
    private static final Comparator<Piece> BY_Z = Comparator.comparingDouble(Piece::z);

    private enum Sprite {
        STRAIGHT_VERTICAL(0, STRAIGHT_V, CELL_SIZE), STRAIGHT_HORIZONTAL(24, STRAIGHT_V, CELL_SIZE), CORNER_BOTTOM_LEFT(48, STRAIGHT_V, CELL_SIZE), CORNER_BOTTOM_RIGHT(72, STRAIGHT_V,
                CELL_SIZE), CORNER_TOP_LEFT(96, STRAIGHT_V, CELL_SIZE), CORNER_TOP_RIGHT(120, STRAIGHT_V, CELL_SIZE), BIG_CORNER_BOTTOM_LEFT(0, BIG_CORNER_V, BIG_SIZE), BIG_CORNER_BOTTOM_RIGHT(48,
                        BIG_CORNER_V, BIG_SIZE), BIG_CORNER_TOP_LEFT(96, BIG_CORNER_V, BIG_SIZE), BIG_CORNER_TOP_RIGHT(144, BIG_CORNER_V,
                                BIG_SIZE), ARROW_DOWN(64, ARROW_V, ARROW_SIZE), ARROW_UP(96, ARROW_V, ARROW_SIZE), ARROW_RIGHT(128, ARROW_V, ARROW_SIZE), ARROW_LEFT(160, ARROW_V, ARROW_SIZE);

        private final int u;
        private final int v;
        private final int size;

        Sprite(int u, int v, int size) {
            this.u = u;
            this.v = v;
            this.size = size;
        }
    }

    private record Piece(float z, int x, int y, Sprite sprite, int color) {
    }

    private ConnectorRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics, int sourceCol, int sourceRow, int parentCol, int parentRow, int originX, int originY, int color, float zModifier, boolean arrow, boolean flip) {
        int anchorCol = flip ? parentCol : sourceCol;
        int anchorRow = flip ? parentRow : sourceRow;
        int farCol = flip ? sourceCol : parentCol;
        int farRow = flip ? sourceRow : parentRow;
        int dirX = Integer.signum(farCol - anchorCol);
        int dirY = Integer.signum(farRow - anchorRow);
        int dx = Math.abs(farCol - anchorCol);
        int dy = Math.abs(farRow - anchorRow);
        List<Piece> pending = PENDING.get();
        Layout layout = new Layout(pending, originX, originY, color, zModifier);
        if (dx == 0 && dy > 0) {
            layout.verticals(anchorCol, anchorRow + dirY, farRow - dirY, dirY);
        } else if (dy == 0 && dx > 0) {
            layout.horizontals(anchorRow, anchorCol + dirX, farCol - dirX, dirX);
        } else if (dx > 0) {
            if (dx == 1 || dy == 1) {
                layout.verticals(anchorCol, anchorRow + dirY, farRow - dirY, dirY);
                layout.piece(anchorCol, farRow, 0, 0, smallCorner(dirX, dirY));
                layout.horizontals(farRow, anchorCol + dirX, farCol - dirX, dirX);
            } else {
                layout.verticals(anchorCol, anchorRow + dirY, farRow - BIG_CORNER_SPAN * dirY, dirY);
                int shiftX = dirX < 0 ? -BIG_CORNER_SHIFT : 0;
                int shiftY = dirY < 0 ? -BIG_CORNER_SHIFT : 0;
                layout.piece(anchorCol, farRow - dirY, shiftX, shiftY, bigCorner(dirX, dirY));
                layout.horizontals(farRow, anchorCol + BIG_CORNER_SPAN * dirX, farCol - dirX, dirX);
            }
        }
        if (arrow) {
            queueArrow(layout, flip, anchorCol, anchorRow, farCol, farRow);
        }
    }

    public static void flush(GuiGraphicsExtractor graphics) {
        List<Piece> pending = PENDING.get();
        try {
            pending.sort(BY_Z);
            for (Piece piece : pending) {
                Sprite sprite = piece.sprite;
                graphics.blit(RenderPipelines.GUI_TEXTURED, TTScreenTextures.RESEARCH_BROWSER, piece.x, piece.y, (float) sprite.u, (float) sprite.v, sprite.size, sprite.size, sprite.size, sprite.size,
                        TTScreenTextures.TEX_SIZE, TTScreenTextures.TEX_SIZE, piece.color);
            }
        } finally {
            pending.clear();
        }
    }

    private static void queueArrow(Layout layout, boolean flip, int anchorCol, int anchorRow, int farCol, int farRow) {
        int dCol = farCol - anchorCol;
        int dRow = farRow - anchorRow;
        Sprite sprite = null;
        int col = flip ? farCol : anchorCol;
        int row = flip ? farRow : anchorRow;
        if (!flip) {
            if (dRow < 0) {
                sprite = Sprite.ARROW_DOWN;
            } else if (dRow > 0) {
                sprite = Sprite.ARROW_UP;
            } else if (dCol > 0) {
                sprite = Sprite.ARROW_LEFT;
            } else if (dCol < 0) {
                sprite = Sprite.ARROW_RIGHT;
            }
        } else {
            if (dCol < 0) {
                sprite = Sprite.ARROW_LEFT;
            } else if (dCol > 0) {
                sprite = Sprite.ARROW_RIGHT;
            } else if (dRow > 0) {
                sprite = Sprite.ARROW_DOWN;
            } else if (dRow < 0) {
                sprite = Sprite.ARROW_UP;
            }
        }
        if (sprite != null) {
            layout.arrow(col, row, sprite);
        }
    }

    private static Sprite smallCorner(int dirX, int dirY) {
        if (dirY > 0) {
            return dirX < 0 ? Sprite.CORNER_BOTTOM_LEFT : Sprite.CORNER_BOTTOM_RIGHT;
        }
        return dirX < 0 ? Sprite.CORNER_TOP_LEFT : Sprite.CORNER_TOP_RIGHT;
    }

    private static Sprite bigCorner(int dirX, int dirY) {
        if (dirY > 0) {
            return dirX < 0 ? Sprite.BIG_CORNER_BOTTOM_LEFT : Sprite.BIG_CORNER_BOTTOM_RIGHT;
        }
        return dirX < 0 ? Sprite.BIG_CORNER_TOP_LEFT : Sprite.BIG_CORNER_TOP_RIGHT;
    }

    private record Layout(List<Piece> pending, int originX, int originY, int color, float z) {
        void piece(int col, int row, int shiftX, int shiftY, Sprite sprite) {
            pending.add(new Piece(z, col * CELL_SIZE - PIECE_INSET + originX + shiftX, row * CELL_SIZE - PIECE_INSET + originY + shiftY, sprite, color));
        }

        void arrow(int col, int row, Sprite sprite) {
            pending.add(new Piece(z, col * CELL_SIZE - ARROW_INSET + originX, row * CELL_SIZE - ARROW_INSET + originY, sprite, color));
        }

        void verticals(int col, int fromRow, int toRow, int step) {
            for (int row = fromRow; step > 0 ? row <= toRow : row >= toRow; row += step) {
                piece(col, row, 0, 0, Sprite.STRAIGHT_VERTICAL);
            }
        }

        void horizontals(int row, int fromCol, int toCol, int step) {
            for (int col = fromCol; step > 0 ? col <= toCol : col >= toCol; col += step) {
                piece(col, row, 0, 0, Sprite.STRAIGHT_HORIZONTAL);
            }
        }
    }
}
