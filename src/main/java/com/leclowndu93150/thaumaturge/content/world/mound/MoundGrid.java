package com.leclowndu93150.thaumaturge.content.world.mound;

import java.util.Arrays;

final class MoundGrid {
    static final int ABSENT = -1;
    private static final int CHAR_BASE = 'A';

    private final int[][][] cells = new int[MoundLayout.SIZE_X][MoundLayout.SIZE_Y][MoundLayout.SIZE_Z];

    MoundGrid() {
        for (int[][] plane : cells) {
            for (int[] row : plane) {
                Arrays.fill(row, ABSENT);
            }
        }
    }

    int get(int x, int y, int z) {
        return cells[x][y][z];
    }

    void set(int x, int y, int z, int id) {
        cells[x][y][z] = id;
    }

    void fill(int id, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    cells[x][y][z] = id;
                }
            }
        }
    }

    String encode() {
        StringBuilder data = new StringBuilder();
        for (int x = 0; x < MoundLayout.SIZE_X; x++) {
            for (int y = 0; y < MoundLayout.SIZE_Y; y++) {
                for (int z = 0; z < MoundLayout.SIZE_Z; z++) {
                    if (cells[x][y][z] != ABSENT) {
                        data.append((char) (CHAR_BASE + x));
                        data.append((char) (CHAR_BASE + y));
                        data.append((char) (CHAR_BASE + z));
                        data.append((char) (CHAR_BASE + cells[x][y][z]));
                    }
                }
            }
        }
        return data.toString();
    }
}
