package com.leclowndu93150.thaumaturge.content.research.note;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.util.RandomSource;

public final class HexGrid {

    public static final int DIRECTIONS = 6;
    private static final int[] STEP_Q = {1, 0, -1, -1, 0, 1};
    private static final int[] STEP_R = {0, 1, 1, 0, -1, -1};
    private static final int RING_START_DIRECTION = 4;
    private static final float COLUMN_SPACING = 1.5F;
    private static final double SQRT3 = Math.sqrt(3.0);
    private static final double HALF = 0.5;
    private static final int AXIS_Q = 0;
    private static final int AXIS_R = 1;
    private static final int AXIS_S = 2;
    private static final Hex ORIGIN = new Hex(0, 0);

    private HexGrid() {}

    public record Hex(int q, int r) {
        public static final Codec<Hex> CODEC = RecordCodecBuilder
                .create(instance -> instance.group(Codec.INT.fieldOf("q").forGetter(Hex::q), Codec.INT.fieldOf("r").forGetter(Hex::r)).apply(instance, Hex::new));

        public Hex neighbour(int direction) {
            return offset(direction, 1);
        }

        public Hex offset(int direction, int steps) {
            int side = Math.floorMod(direction, DIRECTIONS);
            return new Hex(q + STEP_Q[side] * steps, r + STEP_R[side] * steps);
        }

        public float pixelX(float size) {
            return size * COLUMN_SPACING * q;
        }

        public float pixelY(float size) {
            return (float) (size * SQRT3 * (r + q * HALF));
        }
    }

    public static int distance(Hex first, Hex second) {
        int dq = first.q() - second.q();
        int dr = first.r() - second.r();
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    public static List<Hex> ring(int radius) {
        if (radius <= 0) {
            return List.of(ORIGIN);
        }
        List<Hex> cells = new ArrayList<>(DIRECTIONS * radius);
        Hex cursor = ORIGIN.offset(RING_START_DIRECTION, radius);
        for (int side = 0; side < DIRECTIONS; side++) {
            for (int step = 0; step < radius; step++) {
                cells.add(cursor);
                cursor = cursor.neighbour(side);
            }
        }
        return cells;
    }

    public static List<Hex> disk(int radius) {
        List<Hex> cells = new ArrayList<>();
        for (int band = 0; band <= Math.max(0, radius); band++) {
            cells.addAll(ring(band));
        }
        return cells;
    }

    public static List<Hex> distributeRing(int radius, int count, RandomSource random) {
        List<Hex> outline = ring(radius);
        int picks = Math.min(count, outline.size());
        if (picks <= 0) {
            return List.of();
        }
        int first = random.nextInt(outline.size());
        double spacing = (double) outline.size() / picks;
        return IntStream.range(0, picks).mapToObj(slot -> outline.get(Math.floorMod(first + (int) Math.round(slot * spacing), outline.size()))).toList();
    }

    public static Hex pixelToHex(float x, float y, float size) {
        double q = x / (COLUMN_SPACING * size);
        return nearest(q, y / (size * SQRT3) - q * HALF);
    }

    public static Hex clampToDisk(Hex hex, int radius) {
        int reach = distance(hex, ORIGIN);
        if (reach <= radius) {
            return hex;
        }
        double shrink = (double) Math.max(0, radius) / reach;
        Hex pulled = nearest(hex.q() * shrink, hex.r() * shrink);
        while (distance(pulled, ORIGIN) > radius) {
            pulled = nearest(pulled.q() * shrink, pulled.r() * shrink);
        }
        return pulled;
    }

    private static Hex nearest(double q, double r) {
        double[] exact = {q, r, -q - r};
        long[] snapped = Arrays.stream(exact).mapToLong(Math::round).toArray();
        int worst = IntStream.of(AXIS_S, AXIS_R, AXIS_Q).reduce((best, axis) -> Math.abs(snapped[axis] - exact[axis]) > Math.abs(snapped[best] - exact[best]) ? axis : best).getAsInt();
        snapped[worst] = -(LongStream.of(snapped).sum() - snapped[worst]);
        return new Hex((int) snapped[AXIS_Q], (int) snapped[AXIS_R]);
    }
}
