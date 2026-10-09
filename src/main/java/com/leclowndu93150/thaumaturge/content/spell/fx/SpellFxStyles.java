package com.leclowndu93150.thaumaturge.content.spell.fx;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SpellFxStyles {
    private static final double PASS_MOTION = 1.0;
    private static final double NO_MOTION = 0.0;
    private static final double HALF_MOTION = 0.5;
    private static final int CRACK_COLOR = 0xFFFFFF;

    private static final FxStyle SPARKLE = new FxStyle(FxFamilies::sparkle, FxParams.defaults().scale(0.7F, 0.4F).age(6, 4).decay(1.0F).vertical(0.0F).toggle(true), PASS_MOTION);
    private static final FxStyle MOTE = new FxStyle(FxFamilies::mote, FxParams.defaults().age(14, 6).vertical(0.0F).toggle(true), PASS_MOTION);
    private static final FxStyle FLAME = new FxStyle(FxFamilies::flame, FxParams.defaults().scale(1.4F, 0.2F).vertical(-0.2F).alpha(0.7F), NO_MOTION);
    private static final FxStyle FROST = new FxStyle(FxFamilies::frost, FxParams.defaults().scale(0.7F, 0.25F), NO_MOTION);
    private static final FxStyle GUST = new FxStyle(FxFamilies::gust, FxParams.defaults().scale(1.8F, 0.4F), NO_MOTION);
    private static final FxStyle PEBBLE = new FxStyle(FxFamilies::pebble, FxParams.defaults().scale(1.0F, 0.2F), NO_MOTION);
    private static final FxStyle FLUX = new FxStyle(FxFamilies::flux, FxParams.defaults().shade(0.3F, 0.25F).scale(1.8F, 1.0F).endScale(0.2F, 0.3F), NO_MOTION);
    private static final FxStyle HEAL = new FxStyle(FxFamilies::heal, FxParams.defaults(), NO_MOTION);
    private static final FxStyle CURSE = new FxStyle(FxFamilies::curse, FxParams.defaults(), NO_MOTION);
    private static final FxStyle CRACK = new FxStyle(FxFamilies::crack, FxParams.defaults().fixedColor(CRACK_COLOR).variant(0, 4).scale(1.6F, 0.3F).age(6, 6), NO_MOTION);
    private static final FxStyle RIFT = new FxStyle(FxFamilies::rift, FxParams.defaults().scale(0.7F, 0.25F), NO_MOTION);
    private static final FxStyle PRIMAL = new FxStyle(FxFamilies::primal, FxParams.defaults(), NO_MOTION);
    private static final FxStyle WARD = new FxStyle(FxFamilies::ward, FxParams.defaults().alpha(0.9F).scale(0.6F, 0.4F).age(6, 6).delay(0, 6).toggle(true), PASS_MOTION);
    private static final FxStyle BUBBLE = new FxStyle(FxFamilies::bubble, FxParams.defaults().alpha(0.9F).scale(0.3F, 0.3F).age(14, 8).vertical(0.01F).toggle(false), PASS_MOTION);
    private static final FxStyle SPARK = new FxStyle(FxFamilies::spark, FxParams.defaults().alpha(0.9F).scale(0.3F, 0.2F), NO_MOTION);
    private static final FxStyle LEAF = new FxStyle(FxFamilies::leaf, FxParams.defaults(), PASS_MOTION);
    private static final FxStyle SMOKE = new FxStyle(FxFamilies::smoke, FxParams.defaults(), HALF_MOTION);

    private SpellFxStyles() {}

    public static void sparkle(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        SPARKLE.spawn(level, at, motion, color, random);
    }

    public static void mote(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        MOTE.spawn(level, at, motion, color, random);
    }

    public static void flame(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        FLAME.spawn(level, at, motion, color, random);
    }

    public static void frost(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        FROST.spawn(level, at, motion, color, random);
    }

    public static void gust(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        GUST.spawn(level, at, motion, color, random);
    }

    public static void pebble(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        PEBBLE.spawn(level, at, motion, color, random);
    }

    public static void flux(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        FLUX.spawn(level, at, motion, color, random);
    }

    public static void heal(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        HEAL.spawn(level, at, motion, color, random);
    }

    public static void curse(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        CURSE.spawn(level, at, motion, color, random);
    }

    public static void crack(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        CRACK.spawn(level, at, motion, color, random);
    }

    public static void rift(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        RIFT.spawn(level, at, motion, color, random);
    }

    public static void primal(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        PRIMAL.spawn(level, at, motion, color, random);
    }

    public static void ward(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        WARD.spawn(level, at, motion, color, random);
    }

    public static void bubble(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        BUBBLE.spawn(level, at, motion, color, random);
    }

    public static void spark(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        SPARK.spawn(level, at, motion, color, random);
    }

    public static void leaf(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        LEAF.spawn(level, at, motion, color, random);
    }

    public static void smoke(Level level, Vec3 at, Vec3 motion, int color, RandomSource random) {
        SMOKE.spawn(level, at, motion, color, random);
    }
}
