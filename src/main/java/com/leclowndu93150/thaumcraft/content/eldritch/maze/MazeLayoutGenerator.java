package com.leclowndu93150.thaumcraft.content.eldritch.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class MazeLayoutGenerator {
    private static final int N = 1;
    private static final int S = 2;
    private static final int E = 4;
    private static final int W = 8;
    private static final int TEMP_MARK = 25344;
    private static final int FEATURE_TEMP = 99;

    private final int width;
    private final int height;
    private final Random rand;
    public final int[][] grid;

    private record Loc(int x, int y) {}

    public MazeLayoutGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.grid = new int[height][width];
    }

    private static int opposite(int dir) {
        return switch (dir) {
            case N -> S;
            case S -> N;
            case E -> W;
            case W -> E;
            default -> -99;
        };
    }

    private static int dx(int dir) {
        return switch (dir) {
            case E -> 1;
            case W -> -1;
            case N, S -> 0;
            default -> -99;
        };
    }

    private static int dy(int dir) {
        return switch (dir) {
            case N -> -1;
            case S -> 1;
            case E, W -> 0;
            default -> -99;
        };
    }

    public boolean generate() {
        int bx = 0;
        int by = 0;
        switch (rand.nextInt(4)) {
            case 0 -> {
                bx = 0;
                by = 0;
            }
            case 1 -> {
                bx = width - 2;
                by = height - 2;
            }
            case 2 -> {
                bx = width - 2;
                by = 0;
            }
            case 3 -> {
                bx = 0;
                by = height - 2;
            }
        }
        grid[by][bx] = MazeCell.FEATURE_BOSS_NW << 8;
        grid[by][bx + 1] = MazeCell.FEATURE_BOSS_NE << 8;
        grid[by + 1][bx] = MazeCell.FEATURE_BOSS_SW << 8;
        grid[by + 1][bx + 1] = MazeCell.FEATURE_BOSS_SE << 8;
        int px = 1 + width / 2;
        int py = 1 + height / 2;
        grid[py][px] = MazeCell.FEATURE_PORTAL << 8;
        List<Loc> cells = new ArrayList<>();
        int blobs = (width + height) / 4;
        for (int z = 0; z < blobs; z++) {
            int w = 1 + rand.nextInt(3);
            if (w > 2) {
                blobs--;
            }
            int qq = rand.nextInt(width - w);
            int ww = rand.nextInt(height - w);
            for (int a = qq; a < qq + w; a++) {
                for (int b = ww; b < ww + w; b++) {
                    if (grid[b][a] == 0) {
                        grid[b][a] = -1;
                    }
                }
            }
        }
        List<Integer> directions = new ArrayList<>(Arrays.asList(N, S, E, W));
        Collections.shuffle(directions, rand);
        int xx = px + dx(directions.get(0));
        int yy = py + dy(directions.get(0));
        grid[py][px] |= directions.get(0);
        if (grid[yy][xx] < 0) {
            grid[yy][xx] = 0;
        }
        grid[yy][xx] |= opposite(directions.get(0));
        cells.add(new Loc(xx, yy));
        boolean success = false;
        while (!cells.isEmpty()) {
            int index = getNextIndex(cells.size());
            int x = cells.get(index).x();
            int y = cells.get(index).y();
            Collections.shuffle(directions, rand);
            boolean carved = false;
            for (int dir : directions) {
                int nx = x + dx(dir);
                int ny = y + dy(dir);
                if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1) {
                    if (grid[ny][nx] == 0) {
                        grid[y][x] |= dir;
                        grid[ny][nx] |= opposite(dir);
                        cells.add(new Loc(nx, ny));
                        carved = true;
                    }
                    if (carved) {
                        success = true;
                        break;
                    }
                }
            }
            if (!carved) {
                cells.remove(index);
            }
        }
        if (!success) {
            return false;
        }
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                if (grid[aa][bb] < 0) {
                    grid[aa][bb] = 0;
                }
            }
        }
        Collections.shuffle(directions, rand);
        for (int dir : directions) {
            int nx = px + dx(dir);
            int ny = py + dy(dir);
            if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1 && grid[ny][nx] > 0 && rand.nextBoolean()) {
                grid[ny][nx] |= opposite(dir);
                grid[py][px] |= dir;
            }
        }
        Collections.shuffle(directions, rand);
        boolean connected = false;
        connect:
        for (int ax = 0; ax < 2; ax++) {
            for (int ay = 0; ay < 2; ay++) {
                for (int dir : directions) {
                    int nx = bx + ax + dx(dir);
                    int ny = by + ay + dy(dir);
                    if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1 && grid[ny][nx] > 0
                            && new MazeCell((short) grid[ny][nx]).feature == 0) {
                        grid[ny][nx] |= opposite(dir);
                        grid[by + ay][bx + ax] |= dir;
                        connected = true;
                        break connect;
                    }
                }
            }
        }
        if (!connected) {
            List<Integer> directions2 = new ArrayList<>(Arrays.asList(N, S, E, W));
            Collections.shuffle(directions2, rand);
            success = false;
            carveOut:
            for (int ax = 0; ax < 2; ax++) {
                for (int ay = 0; ay < 2; ay++) {
                    for (int dir2 : directions2) {
                        int qx = bx + ax + dx(dir2);
                        int qy = by + ay + dy(dir2);
                        if (0 < qx && qx < width - 1 && 0 < qy && qy < height - 1 && grid[qy][qx] == 0) {
                            cells.add(new Loc(qx, qy));
                            while (!cells.isEmpty()) {
                                int index = getNextIndex(cells.size());
                                int x = cells.get(index).x();
                                int y = cells.get(index).y();
                                Collections.shuffle(directions, rand);
                                boolean carved = false;
                                for (int dir : directions) {
                                    int nx = x + dx(dir);
                                    int ny = y + dy(dir);
                                    if (0 < nx && nx < width - 1 && 0 < ny && ny < height - 1) {
                                        if (grid[ny][nx] == 0) {
                                            grid[y][x] |= dir;
                                            grid[y][x] |= TEMP_MARK;
                                            grid[ny][nx] |= opposite(dir);
                                            grid[ny][nx] |= TEMP_MARK;
                                            cells.add(new Loc(nx, ny));
                                            carved = true;
                                        } else if (new MazeCell((short) grid[ny][nx]).feature == 0) {
                                            grid[y][x] |= dir;
                                            grid[ny][nx] |= opposite(dir);
                                            grid[qy][qx] |= opposite(dir2);
                                            grid[by + ay][bx + ax] |= dir2;
                                            success = true;
                                            break carveOut;
                                        }
                                        if (carved) {
                                            break;
                                        }
                                    }
                                }
                                if (!carved) {
                                    cells.remove(index);
                                }
                            }
                        }
                    }
                }
            }
            if (!success) {
                return false;
            }
        }
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                MazeCell c = new MazeCell((short) grid[aa][bb]);
                if (c.feature == FEATURE_TEMP) {
                    c.feature = 0;
                    grid[aa][bb] = c.pack();
                }
            }
        }
        List<Loc> deadEnds = new ArrayList<>();
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                MazeCell c = new MazeCell((short) grid[aa][bb]);
                if (c.openingCount() == 1 && c.feature == 0) {
                    deadEnds.add(new Loc(aa, bb));
                }
            }
        }
        if (deadEnds.isEmpty()) {
            return false;
        }
        int r = rand.nextInt(deadEnds.size());
        Loc keyLoc = deadEnds.get(r);
        MazeCell keyCell = new MazeCell((short) grid[keyLoc.x()][keyLoc.y()]);
        keyCell.feature = MazeCell.FEATURE_KEY;
        grid[keyLoc.x()][keyLoc.y()] = keyCell.pack();
        deadEnds.remove(r);
        if (!deadEnds.isEmpty()) {
            int filled = 0;
            while (filled < deadEnds.size() / 2) {
                int rx = rand.nextInt(deadEnds.size());
                Loc loc = deadEnds.get(rx);
                MazeCell cell = new MazeCell((short) grid[loc.x()][loc.y()]);
                if (cell.feature == 0) {
                    cell.feature = (byte) (7 + rand.nextInt(3));
                    grid[loc.x()][loc.y()] = cell.pack();
                    deadEnds.remove(rx);
                    filled++;
                }
            }
        }
        for (int aa = 0; aa < height; aa++) {
            for (int bb = 0; bb < width; bb++) {
                MazeCell c = new MazeCell((short) grid[aa][bb]);
                if (c.feature == 0 && c.hasAnyOpening() && rand.nextInt(25) == 0) {
                    switch (rand.nextInt(8)) {
                        case 0 -> c.feature = 8;
                        case 1 -> c.feature = 10;
                        case 2, 3 -> c.feature = 11;
                        case 4, 5 -> c.feature = 12;
                        case 6 -> c.feature = 13;
                        case 7 -> c.feature = 14;
                    }
                    grid[aa][bb] = c.pack();
                }
            }
        }
        return true;
    }

    private int getNextIndex(int ceil) {
        float r = rand.nextFloat();
        if (r <= 0.45F) {
            return ceil - 1;
        }
        return r <= 0.9F ? rand.nextInt(ceil) : 0;
    }
}
