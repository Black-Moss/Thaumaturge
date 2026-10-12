package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BlockEffects {
    private static final AABB FULL_CUBE = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final int AXIS_COUNT = 3;

    private static final double SHELL_GROWTH = 0.1;
    private static final double SHELL_SPARKLES_PER_EDGE = 20.0;
    private static final int SHELL_SPARKLE_MULTIPLIER = 2;
    private static final double SHELL_CENTRE = 0.5;
    private static final double SHELL_FACE_OFFSET = 0.51;
    private static final double SHELL_SPREAD = 0.6;
    private static final int SHELL_EXTRA_DELAY_BOUND = 5;
    private static final double SHELL_RISE = 0.0025;
    private static final double SHELL_SCALE_BASE = 0.4;
    private static final double SHELL_SCALE_SPREAD = 0.1;
    private static final float SHELL_DECAY = 1.0F;
    private static final float SHELL_GRAVITY = 0.01F;
    private static final int SHELL_BASE_AGE = 16;

    private static final int DUST_RED = 255;
    private static final int DUST_GREEN_MIN = 189;
    private static final int DUST_BLUE_MIN = 64;
    private static final int DUST_CHANNEL_BOUND = 256;
    private static final int DUST_RED_SHIFT = 16;
    private static final int DUST_GREEN_SHIFT = 8;

    private static final int MIST_COUNT = 8;
    private static final double MIST_SIDE_DRIFT = 0.01;
    private static final double MIST_RISE_BASE = 0.0;
    private static final double MIST_RISE_SPREAD = 0.075;

    private static final int FLAT_MIST_COUNT = 6;
    private static final double FLAT_MIST_LAYER = 0.125;
    private static final double FLAT_MIST_SIDE_DRIFT = 0.005;
    private static final double FLAT_MIST_RISE = 0.005;

    private BlockEffects() {}

    static void sparkles(ServerLevel level, BlockPos pos, Vec3 source) {
        AABB shell = bounds(level, pos).inflate(SHELL_GROWTH);
        RandomSource random = level.getRandom();
        for (Direction face : Direction.values()) {
            if (faceVisible(level, pos, face)) {
                sparkleFace(level, pos, shell, face, source, random);
            }
        }
    }

    static void mist(ServerLevel level, BlockPos pos, int color) {
        AABB box = outline(level, pos);
        RandomSource random = level.getRandom();
        ColorParticleOption tint = TTParticles.colorOf(TTParticles.BLOCK_MIST, EffectColor.opaque(color));
        for (int i = 0; i < MIST_COUNT; i++) {
            double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
            double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
            double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
            double rise = MIST_RISE_BASE + random.nextDouble() * MIST_RISE_SPREAD;
            Effects.spawn(level, tint, x, y, z, random.nextGaussian() * MIST_SIDE_DRIFT, rise, random.nextGaussian() * MIST_SIDE_DRIFT);
        }
    }

    static void flatMist(ServerLevel level, BlockPos pos, int color) {
        RandomSource random = level.getRandom();
        ColorParticleOption tint = TTParticles.colorOf(TTParticles.MIST_FLAT, EffectColor.opaque(color));
        for (int i = 0; i < FLAT_MIST_COUNT; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * FLAT_MIST_LAYER;
            double z = pos.getZ() + random.nextDouble();
            double driftX = (random.nextDouble() - random.nextDouble()) * FLAT_MIST_SIDE_DRIFT;
            double driftZ = (random.nextDouble() - random.nextDouble()) * FLAT_MIST_SIDE_DRIFT;
            Effects.spawn(level, tint, x, y, z, driftX, FLAT_MIST_RISE, driftZ);
        }
    }

    static int dustColor(RandomSource random) {
        int green = DUST_GREEN_MIN + random.nextInt(DUST_CHANNEL_BOUND - DUST_GREEN_MIN);
        int blue = DUST_BLUE_MIN + random.nextInt(DUST_CHANNEL_BOUND - DUST_BLUE_MIN);
        return EffectColor.opaque((DUST_RED << DUST_RED_SHIFT) | (green << DUST_GREEN_SHIFT) | blue);
    }

    private static AABB bounds(ServerLevel level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        return shape.isEmpty() ? FULL_CUBE : shape.bounds();
    }

    private static AABB outline(ServerLevel level, BlockPos pos) {
        return bounds(level, pos).move(pos);
    }

    private static boolean faceVisible(ServerLevel level, BlockPos pos, Direction face) {
        BlockPos neighbour = pos.relative(face);
        if (!level.isLoaded(neighbour)) {
            return false;
        }
        BlockState next = level.getBlockState(neighbour);
        boolean sealed = next.isSolidRender() && next.isFaceSturdy(level, neighbour, face.getOpposite());
        return !sealed;
    }

    private static void sparkleFace(ServerLevel level, BlockPos pos, AABB shell, Direction face, Vec3 source, RandomSource random) {
        double average = (shell.getXsize() + shell.getYsize() + shell.getZsize()) / AXIS_COUNT;
        int count = SHELL_SPARKLE_MULTIPLIER * Math.max(1, (int) (average * SHELL_SPARKLES_PER_EDGE));
        for (int i = 0; i < count; i++) {
            Vec3 point = pointOnFace(shell, face, random).add(pos.getX(), pos.getY(), pos.getZ());
            int delay = random.nextInt(SHELL_EXTRA_DELAY_BOUND) + (int) (point.distanceTo(source) * Effects.SHELL_TICKS_PER_BLOCK);
            float scale = (float) (SHELL_SCALE_BASE + random.nextGaussian() * SHELL_SCALE_SPREAD);
            SparkleParticleOptions options = new SparkleParticleOptions(dustColor(random), scale, delay, SHELL_DECAY, SHELL_GRAVITY, SHELL_BASE_AGE, true);
            Effects.spawn(level, options, point.x, point.y, point.z, 0.0, SHELL_RISE, 0.0);
        }
    }

    private static Vec3 pointOnFace(AABB shell, Direction face, RandomSource random) {
        Axis normal = face.getAxis();
        double[] coordinates = new double[AXIS_COUNT];
        for (Axis axis : Axis.values()) {
            double raw = axis == normal ? SHELL_CENTRE + face.getAxisDirection().getStep() * SHELL_FACE_OFFSET : SHELL_CENTRE + random.nextGaussian() * SHELL_SPREAD;
            coordinates[axis.ordinal()] = Mth.clamp(raw, shell.min(axis), shell.max(axis));
        }
        return new Vec3(coordinates[Axis.X.ordinal()], coordinates[Axis.Y.ordinal()], coordinates[Axis.Z.ordinal()]);
    }
}
