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
    private static final double FROTH_INSET = 0.2;
    private static final double FROTH_SPAN = 0.6;
    private static final int FROTH_DOWN_PASSES = 2;
    private static final int BUBBLE_ODDS = 6;
    private static final int BUBBLE_CELL_MIN = 5;
    private static final int BUBBLE_CELL_RANGE = 22;
    private static final double BUBBLE_CELLS_PER_BLOCK = 32.0;
    private static final double BUBBLE_CELL_OFFSET = 1.0 / 64.0;
    private static final double BUBBLE_RISE = 0.05;
    private static final double BOIL_INSET = 0.2;
    private static final double BOIL_SPAN = 0.6;
    private static final double BOIL_RISE = 0.1;
    private static final int BOIL_EFFECT_COUNT = 10;
    private static final double BAMF_LIFT = 0.75;
    private static final float BAMF_RED = 0.5F;
    private static final float BAMF_GREEN = 0.1F;
    private static final float BAMF_BLUE = 0.6F;

    private CrucibleFx() {}

    static void froth(ServerLevel level, BlockPos pos, float fluidHeight, boolean overflowing) {
        RandomSource random = level.getRandom();
        double x = pos.getX();
        double z = pos.getZ();
        Effects.cauldronFoamUp(level, new Vec3(x + FROTH_INSET + random.nextFloat() * FROTH_SPAN, pos.getY() + fluidHeight, z + FROTH_INSET + random.nextFloat() * FROTH_SPAN)).send();
        if (!overflowing) {
            return;
        }
        double top = pos.getY() + 1.0;
        for (int pass = 0; pass < FROTH_DOWN_PASSES; pass++) {
            Effects.cauldronFoamDown(level, new Vec3(x, top, z + random.nextFloat())).send();
            Effects.cauldronFoamDown(level, new Vec3(x + 1.0, top, z + random.nextFloat())).send();
            Effects.cauldronFoamDown(level, new Vec3(x + random.nextFloat(), top, z)).send();
            Effects.cauldronFoamDown(level, new Vec3(x + random.nextFloat(), top, z + 1.0)).send();
        }
    }

    static void bubble(ServerLevel level, BlockPos pos, float fluidHeight, AspectList aspects) {
        RandomSource random = level.getRandom();
        if (aspects.isEmpty() || random.nextInt(BUBBLE_ODDS) != 0) {
            return;
        }
        double cellX = BUBBLE_CELL_MIN + random.nextInt(BUBBLE_CELL_RANGE);
        double cellZ = BUBBLE_CELL_MIN + random.nextInt(BUBBLE_CELL_RANGE);
        int color = randomColor(aspects, random);
        Vec3 at = new Vec3(pos.getX() + cellX / BUBBLE_CELLS_PER_BLOCK + BUBBLE_CELL_OFFSET, pos.getY() + BUBBLE_RISE + fluidHeight, pos.getZ() + cellZ / BUBBLE_CELLS_PER_BLOCK + BUBBLE_CELL_OFFSET);
        Effects.cauldronBubble(level, at).color(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color)).send();
    }

    static void boilBurst(ServerLevel level, BlockPos pos, float fluidHeight, AspectList aspects, int intensity) {
        RandomSource random = level.getRandom();
        double surface = pos.getY() + BOIL_RISE + fluidHeight;
        for (int effect = 0; effect < BOIL_EFFECT_COUNT; effect++) {
            int color = aspects.isEmpty() ? ARGB.white(1.0F) : randomColor(aspects, random);
            Vec3 at = new Vec3(pos.getX() + BOIL_INSET - random.nextFloat() * BOIL_SPAN, surface, pos.getZ() + BOIL_INSET - random.nextFloat() * BOIL_SPAN);
            Effects.cauldronBoil(level, at).color(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color)).heat(intensity).send();
        }
    }

    static void craftComplete(ServerLevel level, BlockPos pos) {
        Vec3 at = Vec3.atCenterOf(pos).add(0.0, BAMF_LIFT, 0.0);
        Effects.bamf(level, at).color(BAMF_RED, BAMF_GREEN, BAMF_BLUE).withSound().fancy().side(Direction.UP).send();
    }

    private static int randomColor(AspectList aspects, RandomSource random) {
        return aspects.entries().get(random.nextInt(aspects.size())).aspect().value().color();
    }
}
