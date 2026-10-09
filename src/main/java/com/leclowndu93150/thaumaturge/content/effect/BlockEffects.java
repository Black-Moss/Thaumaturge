package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BlockEffects {
    private static final AABB FULL_CUBE = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private static final double SPARKLE_BOX_GROWTH = 0.1;
    private static final double SPARKLE_DENSITY = 20.0;
    private static final double SPARKLE_EDGE_COUNT = 3.0;
    private static final int SPARKLE_FACE_MULTIPLIER = 2;
    private static final double SPARKLE_FACE_SPREAD = 0.6;
    private static final int SPARKLE_GREEN_FLOOR = 189;
    private static final int SPARKLE_GREEN_RANGE = 67;
    private static final int SPARKLE_BLUE_FLOOR = 64;
    private static final int SPARKLE_BLUE_RANGE = 192;
    private static final int SPARKLE_RED_BITS = 0xFF0000;
    private static final int SPARKLE_GREEN_SHIFT = 8;
    private static final double SPARKLE_DELAY_PER_BLOCK = 16.0;
    private static final int SPARKLE_DELAY_JITTER = 5;
    private static final double SPARKLE_RISE = 0.0025;
    private static final float SPARKLE_SCALE = 0.4F;
    private static final double SPARKLE_SCALE_DEVIATION = 0.1;
    private static final float SPARKLE_DECAY = 1.0F;
    private static final float SPARKLE_GRAVITY = 0.01F;
    private static final int SPARKLE_BASE_AGE = 16;
    private static final int MIST_COUNT = 8;
    private static final double MIST_DRIFT_DEVIATION = 0.01;
    private static final double MIST_RISE = 0.075;
    private static final int FLAT_MIST_COUNT = 6;
    private static final double FLAT_MIST_HEIGHT = 0.125;
    private static final double FLAT_MIST_DRIFT = 0.005;
    private static final double FLAT_MIST_RISE = 0.005;

    private BlockEffects() {}

    static void sparkles(ServerLevel level, BlockPos pos, Vec3 source) {
        BlockState state = level.getBlockState(pos);
        AABB box = outline(level, pos, state).inflate(SPARKLE_BOX_GROWTH).move(pos);
        int base = Math.max(1, (int) ((box.getXsize() + box.getYsize() + box.getZsize()) / SPARKLE_EDGE_COUNT * SPARKLE_DENSITY));
        RandomSource random = level.getRandom();
        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.relative(face);
            if (!level.hasChunkAt(neighborPos) || !Block.shouldRenderFace(state, level.getBlockState(neighborPos), face)) {
                continue;
            }
            for (int i = 0; i < base * SPARKLE_FACE_MULTIPLIER; i++) {
                sparkleOnFace(level, random, box, face, source);
            }
        }
    }

    static void mist(ServerLevel level, BlockPos pos, int color) {
        AABB box = outline(level, pos, level.getBlockState(pos));
        ParticleOptions options = TTParticles.colorOf(TTParticles.BLOCK_MIST, EffectColor.opaque(color));
        RandomSource random = level.getRandom();
        for (int i = 0; i < MIST_COUNT; i++) {
            double x = pos.getX() + box.minX + random.nextDouble() * box.getXsize();
            double y = pos.getY() + box.minY + random.nextDouble() * box.getYsize();
            double z = pos.getZ() + box.minZ + random.nextDouble() * box.getZsize();
            Effects.spawn(level, options, x, y, z, random.nextGaussian() * MIST_DRIFT_DEVIATION, random.nextDouble() * MIST_RISE, random.nextGaussian() * MIST_DRIFT_DEVIATION);
        }
    }

    static void flatMist(ServerLevel level, BlockPos pos, int color) {
        ParticleOptions options = TTParticles.colorOf(TTParticles.MIST_FLAT, EffectColor.opaque(color));
        RandomSource random = level.getRandom();
        for (int i = 0; i < FLAT_MIST_COUNT; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * FLAT_MIST_HEIGHT;
            double z = pos.getZ() + random.nextDouble();
            Effects.spawn(level, options, x, y, z, (random.nextDouble() - random.nextDouble()) * FLAT_MIST_DRIFT, FLAT_MIST_RISE, (random.nextDouble() - random.nextDouble()) * FLAT_MIST_DRIFT);
        }
    }

    private static void sparkleOnFace(ServerLevel level, RandomSource random, AABB box, Direction face, Vec3 source) {
        double x = coordinate(box, Axis.X, face, random);
        double y = coordinate(box, Axis.Y, face, random);
        double z = coordinate(box, Axis.Z, face, random);
        int green = SPARKLE_GREEN_FLOOR + random.nextInt(SPARKLE_GREEN_RANGE);
        int blue = SPARKLE_BLUE_FLOOR + random.nextInt(SPARKLE_BLUE_RANGE);
        int color = EffectColor.opaque(SPARKLE_RED_BITS | (green << SPARKLE_GREEN_SHIFT) | blue);
        int delay = (int) (SPARKLE_DELAY_PER_BLOCK * Math.sqrt(source.distanceToSqr(x, y, z))) + random.nextInt(SPARKLE_DELAY_JITTER);
        float scale = (float) (SPARKLE_SCALE + random.nextGaussian() * SPARKLE_SCALE_DEVIATION);
        SparkleParticleOptions options = new SparkleParticleOptions(color, scale, delay, SPARKLE_DECAY, SPARKLE_GRAVITY, SPARKLE_BASE_AGE, true);
        Effects.spawn(level, options, x, y, z, 0.0, SPARKLE_RISE, 0.0);
    }

    private static double coordinate(AABB box, Axis axis, Direction face, RandomSource random) {
        double min = box.min(axis);
        double max = box.max(axis);
        if (face.getAxis() == axis) {
            return face.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
        }
        return Mth.clamp((min + max) / 2.0 + random.nextGaussian() * SPARKLE_FACE_SPREAD, min, max);
    }

    private static AABB outline(ServerLevel level, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getShape(level, pos);
        return shape.isEmpty() ? FULL_CUBE : shape.bounds();
    }
}
