package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class CrucibleFx {
    private static final int BOIL_EFFECT_COUNT = 10;
    private static final double BAMF_LIFT = 0.75;
    private static final float BAMF_RED = 0.5F;
    private static final float BAMF_GREEN = 0.1F;
    private static final float BAMF_BLUE = 0.6F;

    private static final double CENTRE = 0.5;
    private static final double FROTH_SPREAD = 0.3;
    private static final int FROTH_DOWN_PASSES = 2;
    private static final double RIM_TOP = 1.0;
    private static final double RIM_OUTSET = 0.04;

    private static final int BUBBLE_ODDS = 6;
    private static final double PIXEL = 1.0 / 16.0;
    private static final int BUBBLE_GRID_INSET = 3;
    private static final int BUBBLE_GRID_CELLS = 10;
    private static final double BUBBLE_SURFACE_LIFT = 0.02;
    private static final int NO_ASPECT_COLOR = 0xFFFFFF;

    private CrucibleFx() {}

    static void froth(ServerLevel level, BlockPos pos, float height, boolean overfilled) {
        RandomSource random = level.getRandom();
        double x = pos.getX() + CENTRE + spread(random, FROTH_SPREAD);
        double z = pos.getZ() + CENTRE + spread(random, FROTH_SPREAD);
        Effects.cauldronFoamUp(level, new Vec3(x, pos.getY() + height, z)).send();
        if (!overfilled) {
            return;
        }
        for (int pass = 0; pass < FROTH_DOWN_PASSES; pass++) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                Effects.cauldronFoamDown(level, rimPoint(pos, side, random)).send();
            }
        }
    }

    static void bubble(ServerLevel level, BlockPos pos, float height, AspectList aspects) {
        RandomSource random = level.getRandom();
        if (aspects.isEmpty() || random.nextInt(BUBBLE_ODDS) != 0) {
            return;
        }
        double x = pos.getX() + gridCoordinate(random);
        double z = pos.getZ() + gridCoordinate(random);
        int color = randomColor(aspects, random);
        Effects.cauldronBubble(level, new Vec3(x, pos.getY() + height + BUBBLE_SURFACE_LIFT, z)).color(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color)).send();
    }

    static void boilBurst(ServerLevel level, BlockPos pos, float surface, AspectList aspects, int heat) {
        RandomSource random = level.getRandom();
        Vec3 centre = new Vec3(pos.getX() + CENTRE, pos.getY() + surface, pos.getZ() + CENTRE);
        for (int i = 0; i < BOIL_EFFECT_COUNT; i++) {
            int color = aspects.isEmpty() ? NO_ASPECT_COLOR : randomColor(aspects, random);
            Effects.cauldronBoil(level, centre).color(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color)).heat(heat).send();
        }
    }

    static void craftComplete(ServerLevel level, BlockPos pos) {
        Vec3 at = Vec3.atCenterOf(pos).add(0.0, BAMF_LIFT, 0.0);
        Effects.bamf(level, at).color(BAMF_RED, BAMF_GREEN, BAMF_BLUE).withSound().fancy().side(Direction.UP).send();
    }

    private static Vec3 rimPoint(BlockPos pos, Direction side, RandomSource random) {
        Direction along = side.getClockWise();
        double out = CENTRE + RIM_OUTSET;
        double slide = random.nextDouble() - CENTRE;
        double x = pos.getX() + CENTRE + side.getStepX() * out + along.getStepX() * slide;
        double z = pos.getZ() + CENTRE + side.getStepZ() * out + along.getStepZ() * slide;
        return new Vec3(x, pos.getY() + RIM_TOP, z);
    }

    private static double gridCoordinate(RandomSource random) {
        return (BUBBLE_GRID_INSET + random.nextInt(BUBBLE_GRID_CELLS) + CENTRE) * PIXEL;
    }

    private static double spread(RandomSource random, double reach) {
        return (random.nextDouble() * 2.0 - 1.0) * reach;
    }

    private static int randomColor(AspectList aspects, RandomSource random) {
        return aspects.entries().get(random.nextInt(aspects.size())).aspect().value().color();
    }
}
