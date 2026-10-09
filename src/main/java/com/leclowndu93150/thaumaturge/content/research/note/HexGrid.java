package com.leclowndu93150.thaumaturge.content.research.note;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class HexGrid {
    public static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

    private static final int DIRECTIONS = 6;
    private static final float COLUMN_SPACING = 1.5F;
    private static final double SQRT3 = Math.sqrt(3.0);
    private static final double HALF = 0.5;
    private static final int AXIS_Q = 0;
    private static final int AXIS_R = 1;
    private static final int AXIS_S = 2;

    private HexGrid() {}

    public record Hex(int q, int r) {
        public static final Codec<Hex> CODEC = RecordCodecBuilder
                .create(instance -> instance.group(Codec.INT.fieldOf("q").forGetter(Hex::q), Codec.INT.fieldOf("r").forGetter(Hex::r)).apply(instance, Hex::new));

        public Hex neighbour(int direction) {
            int[] offset = NEIGHBOURS[Math.floorMod(direction, DIRECTIONS)];
            return new Hex(q + offset[0], r + offset[1]);
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
        return IntStream.range(0, DIRECTIONS * radius).mapToObj(index -> ringCell(radius, index / radius, index % radius)).collect(Collectors.toCollection(ArrayList::new));
    }

    private static Hex ringCell(int radius, int side, int offset) {
        int q = -radius;
        int r = radius;
        for (int past = 0; past < side; past++) {
            q += NEIGHBOURS[past][0] * radius;
            r += NEIGHBOURS[past][1] * radius;
        }
        return new Hex(q + NEIGHBOURS[side][0] * offset, r + NEIGHBOURS[side][1] * offset);
    }

    public static List<Hex> disk(int radius) {
        Stream<Hex> rings = IntStream.rangeClosed(1, radius).boxed().flatMap(layer -> ring(layer).stream());
        return Stream.concat(Stream.of(new Hex(0, 0)), rings).collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Hex> distributeRing(int radius, int count, RandomSource random) {
        List<Hex> border = ring(radius);
        int length = border.size();
        if (length == 0 || count <= 0) {
            return new ArrayList<>();
        }
        long shift = random.nextInt(length);
        return IntStream.range(0, count).mapToObj(slot -> border.get(spreadIndex(shift, slot, count, length))).collect(Collectors.toCollection(ArrayList::new));
    }

    private static int spreadIndex(long shift, int slot, int count, int length) {
        long doubled = 2L * (shift * count + (long) slot * length) + count;
        return (int) Math.floorMod(Math.floorDiv(doubled, 2L * count), (long) length);
    }

    public static Hex pixelToHex(float x, float y, float size) {
        double[] exact = {x / (COLUMN_SPACING * size), 0.0, 0.0};
        exact[AXIS_R] = y / (size * SQRT3) - exact[AXIS_Q] * HALF;
        exact[AXIS_S] = -exact[AXIS_Q] - exact[AXIS_R];
        long[] snapped = Arrays.stream(exact).mapToLong(Math::round).toArray();
        int worst = IntStream.of(AXIS_S, AXIS_R, AXIS_Q).reduce((best, axis) -> Math.abs(snapped[axis] - exact[axis]) > Math.abs(snapped[best] - exact[best]) ? axis : best).getAsInt();
        snapped[worst] = -(LongStream.of(snapped).sum() - snapped[worst]);
        return new Hex((int) snapped[AXIS_Q], (int) snapped[AXIS_R]);
    }

    public static Hex clampToDisk(Hex hex, int radius) {
        if (distance(hex, new Hex(0, 0)) <= radius) {
            return hex;
        }
        return new Hex(Mth.clamp(hex.q(), -radius, radius), Mth.clamp(hex.r(), -radius, radius));
    }
}
