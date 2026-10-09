package com.leclowndu93150.thaumaturge.content.essentia.spill;

import net.minecraft.util.RandomSource;

public record SpillRange(double start, double width) {
    public static SpillRange startingAt(double start, double width) {
        return new SpillRange(start, width);
    }

    public double sample(RandomSource random) {
        return start + random.nextDouble() * width;
    }
}
