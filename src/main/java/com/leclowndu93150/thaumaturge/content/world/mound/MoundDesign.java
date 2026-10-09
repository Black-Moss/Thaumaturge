package com.leclowndu93150.thaumaturge.content.world.mound;

final class MoundDesign {
    private static final int CENTER = 9;
    private static final double DOME_RADIUS = 10.0;
    private static final double PROFILE_RADIUS = 10.5;
    private static final int DOME_BASE_TOP = 10;
    private static final int DOME_RISE = 5;
    private static final int NO_DOME = -1;
    private static final int BODY_FLOOR_Y = 1;

    private static final int HALL_MIN = 7;
    private static final int HALL_MAX = 11;
    private static final int HALL_FLOOR_Y = 1;
    private static final int HALL_AIR_MIN_Y = 2;
    private static final int HALL_AIR_MAX_Y = 9;
    private static final int HALL_ROOF_Y = 10;
    private static final int HALL_WALL = 1;
    private static final int PILLAR_BASE_Y = 2;
    private static final int PILLAR_CAP_Y = 3;
    private static final int COVE_Y = 9;

    private static final int ROOM_FAR_X = 2;
    private static final int ROOM_NEAR_X = 6;
    private static final int ROOM_FAR_Z = 2;
    private static final int ROOM_NEAR_Z = 6;
    private static final int ROOM_AIR_MIN_X = 3;
    private static final int ROOM_AIR_MAX_X = 5;
    private static final int ROOM_AIR_MIN_Z = 3;
    private static final int ROOM_AIR_MAX_Z = 5;
    private static final int ROOM_FLOOR_Y = 5;
    private static final int ROOM_AIR_MIN_Y = 6;
    private static final int ROOM_AIR_MAX_Y = 7;
    private static final int ROOM_ROOF_Y = 8;

    private static final int WING_SHELL_MIN_X = 7;
    private static final int WING_SHELL_MAX_X = 9;
    private static final int WING_SHELL_MAX_Y = 8;
    private static final int DOOR_X = 6;
    private static final int PASSAGE_Z = 4;
    private static final int PASSAGE_END_X = 8;
    private static final int NICHE_X = 7;
    private static final int NICHE_Z = 3;
    private static final int RUN_X = 8;
    private static final int RUN_FIRST_Z = 5;
    private static final int RUN_AIR_LOW_Y = 5;
    private static final int RUN_FLIGHT_STEPS = 4;
    private static final int RUN_FIRST_STAIR_Y = 5;
    private static final int RUN_FIRST_STAIR_Z = 5;

    private static final int SHAFT_X = 7;
    private static final int SHAFT_Z = 3;
    private static final int SHAFT_WALL_X = 6;
    private static final int SHAFT_BOTTOM_Y = 6;

    private static final int CAGE_X = 9;
    private static final int CAGE_Y = 8;
    private static final int CAGE_Z = 9;

    private static final int MOSS_WEIGHT_X = 3;
    private static final int MOSS_WEIGHT_Y = 5;
    private static final int MOSS_WEIGHT_Z = 7;
    private static final int MOSS_MODULUS = 4;
    private static final int TUFT_WEIGHT_X = 5;
    private static final int TUFT_WEIGHT_Z = 11;
    private static final int TUFT_MODULUS = 6;

    private MoundDesign() {}

    static MoundGrid build() {
        MoundGrid grid = new MoundGrid();
        raiseDome(grid);
        buildShells(grid);
        carveInteriors(grid);
        fitFlights(grid);
        dressHall(grid);
        cageNode(grid);
        sinkShaft(grid);
        scatterTufts(grid);
        weather(grid);
        return grid;
    }

    private static int domeTop(int x, int z) {
        double dx = x - CENTER;
        double dz = z - CENTER;
        double squared = dx * dx + dz * dz;
        if (squared > DOME_RADIUS * DOME_RADIUS) {
            return NO_DOME;
        }
        double ratio = squared / (PROFILE_RADIUS * PROFILE_RADIUS);
        return DOME_BASE_TOP + (int) Math.round(DOME_RISE * Math.sqrt(1.0 - ratio));
    }

    private static int flip(int z, boolean mirrored) {
        return mirrored ? MoundLayout.SIZE_Z - 1 - z : z;
    }

    private static void raiseDome(MoundGrid grid) {
        for (int x = 0; x < MoundLayout.SIZE_X; x++) {
            for (int z = 0; z < MoundLayout.SIZE_Z; z++) {
                int top = domeTop(x, z);
                if (top != NO_DOME) {
                    grid.fill(MoundLayout.DIRT, x, BODY_FLOOR_Y, z, x, top - 1, z);
                    grid.set(x, top, z, MoundLayout.GRASS_BLOCK);
                }
            }
        }
    }

    private static void buildShells(MoundGrid grid) {
        grid.fill(MoundLayout.COBBLESTONE, HALL_MIN - HALL_WALL, HALL_FLOOR_Y, HALL_MIN - HALL_WALL, HALL_MAX + HALL_WALL, HALL_ROOF_Y, HALL_MAX + HALL_WALL);
        for (boolean mirrored : new boolean[]{false, true}) {
            grid.fill(MoundLayout.COBBLESTONE, ROOM_FAR_X, ROOM_FLOOR_Y, flip(ROOM_FAR_Z, mirrored), ROOM_NEAR_X, ROOM_ROOF_Y, flip(ROOM_NEAR_Z, mirrored));
            grid.fill(MoundLayout.COBBLESTONE, WING_SHELL_MIN_X, BODY_FLOOR_Y, flip(ROOM_FAR_Z, mirrored), WING_SHELL_MAX_X, WING_SHELL_MAX_Y, flip(ROOM_NEAR_Z, mirrored));
        }
    }

    private static void carveInteriors(MoundGrid grid) {
        grid.fill(MoundLayout.AIR, HALL_MIN, HALL_AIR_MIN_Y, HALL_MIN, HALL_MAX, HALL_AIR_MAX_Y, HALL_MAX);
        for (boolean mirrored : new boolean[]{false, true}) {
            grid.fill(MoundLayout.AIR, ROOM_AIR_MIN_X, ROOM_AIR_MIN_Y, flip(ROOM_AIR_MIN_Z, mirrored), ROOM_AIR_MAX_X, ROOM_AIR_MAX_Y, flip(ROOM_AIR_MAX_Z, mirrored));
            grid.fill(MoundLayout.AIR, DOOR_X, ROOM_AIR_MIN_Y, flip(PASSAGE_Z, mirrored), PASSAGE_END_X, ROOM_AIR_MAX_Y, flip(PASSAGE_Z, mirrored));
            grid.fill(MoundLayout.AIR, NICHE_X, ROOM_AIR_MIN_Y, flip(NICHE_Z, mirrored), NICHE_X, ROOM_AIR_MAX_Y, flip(NICHE_Z, mirrored));
            grid.fill(MoundLayout.AIR, RUN_X, ROOM_AIR_MIN_Y, flip(RUN_FIRST_Z, mirrored), RUN_X, ROOM_AIR_MAX_Y, flip(RUN_FIRST_Z, mirrored));
            grid.fill(MoundLayout.AIR, RUN_X, RUN_AIR_LOW_Y, flip(RUN_FIRST_Z + 1, mirrored), RUN_X, ROOM_AIR_MAX_Y, flip(RUN_FIRST_Z + 1, mirrored));
        }
    }

    private static void fitFlights(MoundGrid grid) {
        fitFlight(grid, false, MoundLayout.STAIRS_NORTH);
        fitFlight(grid, true, MoundLayout.STAIRS_SOUTH);
    }

    private static void fitFlight(MoundGrid grid, boolean mirrored, int stairId) {
        for (int step = 0; step < RUN_FLIGHT_STEPS; step++) {
            int y = RUN_FIRST_STAIR_Y - step;
            int z = flip(RUN_FIRST_STAIR_Z + step, mirrored);
            grid.set(RUN_X, y - 1, z, MoundLayout.COBBLESTONE);
            grid.set(RUN_X, y, z, stairId);
        }
    }

    private static void dressHall(MoundGrid grid) {
        for (int x : new int[]{HALL_MIN, HALL_MAX}) {
            for (int z : new int[]{HALL_MIN, HALL_MAX}) {
                grid.set(x, PILLAR_BASE_Y, z, MoundLayout.COBBLESTONE);
                grid.set(x, PILLAR_CAP_Y, z, MoundLayout.CHISELED_STONE_BRICKS);
            }
        }
        for (int edge = HALL_MIN; edge <= HALL_MAX; edge++) {
            grid.set(HALL_MIN, COVE_Y, edge, MoundLayout.STAIRS_WEST_TOP);
            grid.set(HALL_MAX, COVE_Y, edge, MoundLayout.STAIRS_EAST_TOP);
        }
        for (int edge = HALL_MIN + 1; edge < HALL_MAX; edge++) {
            grid.set(edge, COVE_Y, HALL_MIN, MoundLayout.STAIRS_NORTH_TOP);
            grid.set(edge, COVE_Y, HALL_MAX, MoundLayout.STAIRS_SOUTH_TOP);
        }
    }

    private static void cageNode(MoundGrid grid) {
        grid.set(CAGE_X - 1, CAGE_Y, CAGE_Z, MoundLayout.IRON_BARS);
        grid.set(CAGE_X + 1, CAGE_Y, CAGE_Z, MoundLayout.IRON_BARS);
        grid.set(CAGE_X, CAGE_Y - 1, CAGE_Z, MoundLayout.IRON_BARS);
        grid.set(CAGE_X, CAGE_Y + 1, CAGE_Z, MoundLayout.IRON_BARS);
        grid.set(CAGE_X, CAGE_Y, CAGE_Z - 1, MoundLayout.IRON_BARS);
        grid.set(CAGE_X, CAGE_Y, CAGE_Z + 1, MoundLayout.IRON_BARS);
    }

    private static void sinkShaft(MoundGrid grid) {
        int shaftTop = domeTop(SHAFT_X, SHAFT_Z);
        int ladderTop = Math.min(shaftTop, domeTop(SHAFT_WALL_X, SHAFT_Z));
        for (int y = SHAFT_BOTTOM_Y; y <= ladderTop; y++) {
            grid.set(SHAFT_X, y, SHAFT_Z, MoundLayout.LADDER_EAST);
        }
        for (int y = ladderTop + 1; y <= shaftTop; y++) {
            grid.set(SHAFT_X, y, SHAFT_Z, MoundLayout.AIR);
        }
    }

    private static void scatterTufts(MoundGrid grid) {
        for (int x = 0; x < MoundLayout.SIZE_X; x++) {
            for (int z = 0; z < MoundLayout.SIZE_Z; z++) {
                int top = domeTop(x, z);
                boolean shaftColumn = x == SHAFT_X && z == SHAFT_Z;
                boolean chosen = (x * TUFT_WEIGHT_X + z * TUFT_WEIGHT_Z) % TUFT_MODULUS == 0;
                if (top != NO_DOME && top + 1 < MoundLayout.SIZE_Y && !shaftColumn && chosen) {
                    grid.set(x, top + 1, z, MoundLayout.SHORT_GRASS);
                }
            }
        }
    }

    private static void weather(MoundGrid grid) {
        for (int x = 0; x < MoundLayout.SIZE_X; x++) {
            for (int y = 0; y < MoundLayout.SIZE_Y; y++) {
                for (int z = 0; z < MoundLayout.SIZE_Z; z++) {
                    boolean mossy = (x * MOSS_WEIGHT_X + y * MOSS_WEIGHT_Y + z * MOSS_WEIGHT_Z) % MOSS_MODULUS == 0;
                    if (grid.get(x, y, z) == MoundLayout.COBBLESTONE && mossy) {
                        grid.set(x, y, z, MoundLayout.MOSSY_COBBLESTONE);
                    }
                }
            }
        }
    }
}
