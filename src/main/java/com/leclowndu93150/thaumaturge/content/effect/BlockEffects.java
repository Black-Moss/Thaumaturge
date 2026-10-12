package com.leclowndu93150.thaumaturge.content.effect;

import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
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

    private static final double SHELL_GROWTH = 0.02;
    private static final double SHELL_SPARKLES_PER_FACE_AREA = 24.0;
    private static final int SHELL_MIN_SPARKLES_PER_FACE = 3;
    private static final double SHELL_SPREAD = 0.45;
    private static final int SHELL_EXTRA_DELAY_BOUND = 6;
    private static final double SHELL_RISE = 0.002;
    private static final float SHELL_SCALE = 0.4F;
    private static final float SHELL_DECAY = 0.99F;
    private static final int SHELL_BASE_AGE = 15;

    private static final EffectColor DUST_GOLD = new EffectColor(0.86F, 0.58F, 0.12F);
    private static final EffectColor DUST_CREAM = new EffectColor(1.0F, 0.96F, 0.84F);

    private static final int MIST_COUNT = 5;
    private static final double MIST_SIDE_DRIFT = 0.008;
    private static final double MIST_RISE_BASE = 0.015;
    private static final double MIST_RISE_SPREAD = 0.015;

    private static final int FLAT_MIST_COUNT = 6;
    private static final double FLAT_MIST_LAYER = 0.15;
    private static final double FLAT_MIST_FLOOR_LIFT = 0.02;
    private static final double FLAT_MIST_SIDE_DRIFT = 0.003;
    private static final double FLAT_MIST_RISE = 0.0008;

    private BlockEffects() {}

    static void sparkles(ServerLevel level, BlockPos pos, Vec3 source) {
        AABB shell = outline(level, pos).inflate(SHELL_GROWTH);
        RandomSource random = level.getRandom();
        for (Direction face : Direction.values()) {
            if (faceVisible(level, pos, face)) {
                sparkleFace(level, shell, face, source, random);
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
            double y = pos.getY() + FLAT_MIST_FLOOR_LIFT + random.nextDouble() * FLAT_MIST_LAYER;
            double z = pos.getZ() + random.nextDouble();
            Effects.spawn(level, tint, x, y, z, random.nextGaussian() * FLAT_MIST_SIDE_DRIFT, FLAT_MIST_RISE, random.nextGaussian() * FLAT_MIST_SIDE_DRIFT);
        }
    }

    static int dustColor(RandomSource random) {
        float t = random.nextFloat();
        return new EffectColor(Mth.lerp(t, DUST_GOLD.r(), DUST_CREAM.r()), Mth.lerp(t, DUST_GOLD.g(), DUST_CREAM.g()), Mth.lerp(t, DUST_GOLD.b(), DUST_CREAM.b())).argb();
    }

    private static AABB outline(ServerLevel level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        AABB local = shape.isEmpty() ? FULL_CUBE : shape.bounds();
        return local.move(pos);
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

    private static void sparkleFace(ServerLevel level, AABB shell, Direction face, Vec3 source, RandomSource random) {
        Axis normal = face.getAxis();
        double area = 1.0;
        for (Axis axis : Axis.values()) {
            if (axis != normal) {
                area *= shell.max(axis) - shell.min(axis);
            }
        }
        int count = Math.max(SHELL_MIN_SPARKLES_PER_FACE, (int) Math.round(area * SHELL_SPARKLES_PER_FACE_AREA));
        for (int i = 0; i < count; i++) {
            Vec3 point = pointOnFace(shell, face, random);
            int delay = (int) (point.distanceTo(source) * Effects.SHELL_TICKS_PER_BLOCK) + random.nextInt(SHELL_EXTRA_DELAY_BOUND);
            SparkleParticleOptions options = new SparkleParticleOptions(dustColor(random), SHELL_SCALE, delay, SHELL_DECAY, 0.0F, SHELL_BASE_AGE, true);
            Effects.spawn(level, options, point.x, point.y, point.z, 0.0, SHELL_RISE, 0.0);
        }
    }

    private static Vec3 pointOnFace(AABB shell, Direction face, RandomSource random) {
        Axis normal = face.getAxis();
        double[] coordinates = new double[AXIS_COUNT];
        for (Axis axis : Axis.values()) {
            double min = shell.min(axis);
            double max = shell.max(axis);
            if (axis == normal) {
                coordinates[axis.ordinal()] = face.getAxisDirection() == AxisDirection.POSITIVE ? max : min;
            } else {
                double centre = (min + max) / 2.0;
                double spread = (max - min) * SHELL_SPREAD;
                coordinates[axis.ordinal()] = Mth.clamp(centre + random.nextGaussian() * spread, min, max);
            }
        }
        return new Vec3(coordinates[Axis.X.ordinal()], coordinates[Axis.Y.ordinal()], coordinates[Axis.Z.ordinal()]);
    }
}
