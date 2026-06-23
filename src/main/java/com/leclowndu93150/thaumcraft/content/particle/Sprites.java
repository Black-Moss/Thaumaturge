package com.leclowndu93150.thaumcraft.content.particle;

public enum Sprites {
    PUFF(16, 123, 5, 1),
    FLASH(16, 77, 1, 1),
    WISPY_LOOP(64, 512, 16, 1),
    CURLY_WISP(16, 60, 4, 1),
    SPARKLE_LOOP(64, 320, 16, 1),
    SPARKLE_LARGE(64, 512, 16, 1),
    TAINT(16, 57, 3, 1),
    LIGHTNING_FLASH(16, 108, 4, 1),
    SMOKE_MIST(16, 56, 1, 1),
    SMOKE_MIST_4(16, 56, 4, 1),
    STABILIZER(16, 72, 4, 1),
    MIST_FLAT(8, 24, 1, 1),
    WISP_LOOP(64, 264, 8, 1),
    NITOR_CORE(64, 457, 1, 1),
    CRUCIBLE_BUBBLE(64, 64, 1, 1),
    CRUCIBLE_FROTH_DOWN(64, 73, 1, 1),
    SPARK_LOOP(16, 8, 8, 1),
    BURST(16, 208, 31, 1),
    ESSENTIA_DROP(64, 25, 1, 1),
    JAR_SPLASH(64, 73, 1, 1);

    private final int grid;
    private final int start;
    private final int num;
    private final int inc;

    Sprites(int grid, int start, int num, int inc) {
        this.grid = grid;
        this.start = start;
        this.num = num;
        this.inc = inc;
    }

    public int grid() { return grid; }
    public int start() { return start; }
    public int num() { return num; }
    public int inc() { return inc; }
}
