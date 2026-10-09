package com.leclowndu93150.thaumaturge.content.warp.roll;

public record WarpOutcomeBand(int min, int max, WarpOutcome outcome) {
    private static final int UNBOUNDED = Integer.MAX_VALUE;

    public WarpOutcomeBand {
        if (max < min) {
            throw new IllegalArgumentException("Warp outcome band " + min + ".." + max + " is empty");
        }
    }

    public static WarpOutcomeBand between(int min, int max, WarpOutcome outcome) {
        return new WarpOutcomeBand(min, max, outcome);
    }

    public static WarpOutcomeBand exactly(int strength, WarpOutcome outcome) {
        return new WarpOutcomeBand(strength, strength, outcome);
    }

    public static WarpOutcomeBand atLeast(int min, WarpOutcome outcome) {
        return new WarpOutcomeBand(min, UNBOUNDED, outcome);
    }

    public boolean contains(int strength) {
        return strength >= min && strength <= max;
    }

    public boolean overlaps(WarpOutcomeBand other) {
        return min <= other.max && other.min <= max;
    }
}
