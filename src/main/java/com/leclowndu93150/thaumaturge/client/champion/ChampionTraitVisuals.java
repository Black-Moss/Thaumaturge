package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.trait.MobTraitVisuals;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ChampionTraitVisuals {
    private static final float OPAQUE = 1.0F;
    private static final float HALF_GATE = 0.5F;
    private static final float HALF = 0.5F;
    private static final double SINK = -0.02;
    private static final double RISE_SLOW = 0.02;
    private static final double RISE = 0.03;

    private static final float BOLD_SCALE = 0.2F;
    private static final float BOLD_RED_BASE = 0.3F;
    private static final float BOLD_RED_DROP = 0.1F;
    private static final float BOLD_BLUE_BASE = 0.8F;
    private static final float BOLD_BLUE_SPREAD = 0.2F;
    private static final float BOLD_HEIGHT_FRACTION = 1.0F / 3.0F;

    private static final float SPINE_RED_BASE = 0.5F;
    private static final float SPINE_GREEN_BASE = 0.1F;
    private static final float SPINE_BLUE_BASE = 0.1F;
    private static final float SPINE_SPREAD = 0.2F;
    private static final int SHARD_VARIANTS = 4;
    private static final float SPINE_SCALE_BASE = 1.2F;
    private static final float SPINE_SCALE_SPREAD = 0.3F;
    private static final int SPINE_AGE = 3;

    private static final float ARMOR_GATE = 0.25F;
    private static final float ARMOR_RED_GREEN = 0.9F;
    private static final float ARMOR_BLUE_BASE = 0.9F;
    private static final float ARMOR_BLUE_SPREAD = 0.1F;
    private static final float ARMOR_ALPHA = 0.7F;
    private static final float ARMOR_SCALE_BASE = 0.6F;
    private static final float ARMOR_SCALE_SPREAD = 0.2F;
    private static final int ARMOR_AGE_BASE = 5;
    private static final int ARMOR_AGE_SPREAD = 4;
    private static final int NO_DELAY = 0;

    private static final float MIGHTY_GATE = 0.3F;
    private static final float MIGHTY_COLOR_BASE = 0.8F;
    private static final float MIGHTY_COLOR_SPREAD = 0.2F;
    private static final float MIGHTY_SCALE_BASE = 1.0F;
    private static final float MIGHTY_SCALE_SPREAD = 0.3F;
    private static final int MIGHTY_AGE_BASE = 4;
    private static final int MIGHTY_AGE_SPREAD = 3;

    private static final double GRIM_DRIFT = -0.02;
    private static final float GRIM_SCALE_BASE = 0.6F;
    private static final float GRIM_SCALE_SPREAD = 0.4F;
    private static final float GRIM_ALPHA = 0.8F;
    private static final float GRIM_TINT = 0.6F;
    private static final float NO_LIFT = 0.0F;

    private static final float WARDED_BASE = 0.5F;
    private static final float WARDED_SPREAD = 0.1F;

    private static final float WARP_RED_BASE = 0.8F;
    private static final float WARP_RED_SPREAD = 0.2F;
    private static final float WARP_BLUE_BASE = 0.9F;
    private static final float WARP_BLUE_SPREAD = 0.1F;
    private static final float WARP_ALPHA = 0.7F;
    private static final float WARP_SCALE_BASE = 0.6F;
    private static final float WARP_SCALE_SPREAD = 0.4F;

    private static final float UNDYING_LOW_BASE = 0.1F;
    private static final float UNDYING_LOW_SPREAD = 0.1F;
    private static final float UNDYING_GREEN_BASE = 0.8F;
    private static final float UNDYING_GREEN_SPREAD = 0.2F;
    private static final float UNDYING_ALPHA = 0.9F;
    private static final float UNDYING_SCALE_BASE = 0.5F;
    private static final float UNDYING_SCALE_SPREAD = 0.2F;

    private static final float FIERY_SCALE_BASE = 0.7F;
    private static final float FIERY_SCALE_SPREAD = 0.2F;
    private static final float FIERY_ALPHA = 0.7F;

    private static final float SICKLY_RED = 0.2F;
    private static final float SICKLY_GREEN_BASE = 0.6F;
    private static final float SICKLY_GREEN_SPREAD = 0.1F;
    private static final float SICKLY_BLUE_BASE = 0.2F;
    private static final float SICKLY_BLUE_SPREAD = 0.1F;
    private static final float SICKLY_SCALE_BASE = 0.9F;
    private static final float SICKLY_SCALE_SPREAD = 0.3F;
    private static final float SICKLY_END_SCALE = 0.9F;

    private static final float VENOM_RED = 0.2F;
    private static final float VENOM_GREEN_BASE = 0.6F;
    private static final float VENOM_GREEN_SPREAD = 0.1F;
    private static final float VENOM_BLUE_BASE = 0.2F;
    private static final float VENOM_BLUE_SPREAD = 0.1F;

    private static final float VAMPIRIC_GATE = 0.2F;
    private static final float VAMPIRIC_RED_BASE = 0.9F;
    private static final float VAMPIRIC_RED_SPREAD = 0.1F;

    private ChampionTraitVisuals() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ChampionTraitVisuals::register);
    }

    private static void register() {
        MobTraitVisuals.registerParticles(TTMobTraits.BOLD.getKey(), ChampionTraitVisuals::bold);
        MobTraitVisuals.registerParticles(TTMobTraits.SPINED.getKey(), ChampionTraitVisuals::spined);
        MobTraitVisuals.registerParticles(TTMobTraits.ARMORED.getKey(), ChampionTraitVisuals::armored);
        MobTraitVisuals.registerParticles(TTMobTraits.MIGHTY.getKey(), ChampionTraitVisuals::mighty);
        MobTraitVisuals.registerParticles(TTMobTraits.GRIM.getKey(), ChampionTraitVisuals::grim);
        MobTraitVisuals.registerTint(TTMobTraits.GRIM.getKey(), ARGB.colorFromFloat(OPAQUE, GRIM_TINT, GRIM_TINT, GRIM_TINT));
        MobTraitVisuals.registerParticles(TTMobTraits.WARDED.getKey(), ChampionTraitVisuals::warded);
        MobTraitVisuals.registerParticles(TTMobTraits.WARP.getKey(), ChampionTraitVisuals::warp);
        MobTraitVisuals.registerParticles(TTMobTraits.UNDYING.getKey(), ChampionTraitVisuals::undying);
        MobTraitVisuals.registerParticles(TTMobTraits.FIERY.getKey(), ChampionTraitVisuals::fiery);
        MobTraitVisuals.registerParticles(TTMobTraits.SICKLY.getKey(), ChampionTraitVisuals::sickly);
        MobTraitVisuals.registerParticles(TTMobTraits.VENOMOUS.getKey(), ChampionTraitVisuals::venomous);
        MobTraitVisuals.registerParticles(TTMobTraits.VAMPIRIC.getKey(), ChampionTraitVisuals::vampiric);
        MobTraitVisuals.registerParticles(TTMobTraits.INFESTED.getKey(), ChampionTraitVisuals::infested);
    }

    private static float spread(RandomSource random, float base, float range) {
        return base + random.nextFloat() * range;
    }

    private static boolean passes(RandomSource random, float chance) {
        return random.nextFloat() <= chance;
    }

    private static void bold(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        double low = mob.getBoundingBox().minY + random.nextFloat() * mob.getBbHeight() * BOLD_HEIGHT_FRACTION;
        int color = ARGB.colorFromFloat(OPAQUE, BOLD_RED_BASE - random.nextFloat() * BOLD_RED_DROP, 0.0F, spread(random, BOLD_BLUE_BASE, BOLD_BLUE_SPREAD));
        level.addParticle(new SparkParticleOptions(color, OPAQUE, BOLD_SCALE), x, low, z, 0.0, 0.0, 0.0);
    }

    private static void spined(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, spread(random, SPINE_RED_BASE, SPINE_SPREAD), spread(random, SPINE_GREEN_BASE, SPINE_SPREAD), spread(random, SPINE_BLUE_BASE, SPINE_SPREAD));
        int variant = random.nextInt(SHARD_VARIANTS);
        float scale = spread(random, SPINE_SCALE_BASE, SPINE_SCALE_SPREAD);
        level.addParticle(new CrackShardParticleOptions(color, variant, scale, SPINE_AGE), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void armored(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, ARMOR_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, ARMOR_RED_GREEN, ARMOR_RED_GREEN, spread(random, ARMOR_BLUE_BASE, ARMOR_BLUE_SPREAD));
        float scale = spread(random, ARMOR_SCALE_BASE, ARMOR_SCALE_SPREAD);
        int age = ARMOR_AGE_BASE + random.nextInt(ARMOR_AGE_SPREAD);
        level.addParticle(new ShieldSparkParticleOptions(color, ARMOR_ALPHA, scale, age, NO_DELAY, true), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void mighty(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, MIGHTY_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, spread(random, MIGHTY_COLOR_BASE, MIGHTY_COLOR_SPREAD), spread(random, MIGHTY_COLOR_BASE, MIGHTY_COLOR_SPREAD),
                spread(random, MIGHTY_COLOR_BASE, MIGHTY_COLOR_SPREAD));
        int variant = random.nextInt(SHARD_VARIANTS);
        float scale = spread(random, MIGHTY_SCALE_BASE, MIGHTY_SCALE_SPREAD);
        int age = MIGHTY_AGE_BASE + random.nextInt(MIGHTY_AGE_SPREAD);
        level.addParticle(new CrackShardParticleOptions(color, variant, scale, age), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void grim(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        level.addParticle(new FlameFanParticleOptions(spread(random, GRIM_SCALE_BASE, GRIM_SCALE_SPREAD), NO_LIFT, GRIM_ALPHA), x, y, z, 0.0, GRIM_DRIFT, 0.0);
    }

    private static void warded(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        level.addParticle(
                TTParticles.colorOf(TTParticles.LEAF_MOTE, spread(random, WARDED_BASE, WARDED_SPREAD), spread(random, WARDED_BASE, WARDED_SPREAD), spread(random, WARDED_BASE, WARDED_SPREAD)), x, y, z,
                0.0, 0.0, 0.0);
    }

    private static void warp(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, spread(random, WARP_RED_BASE, WARP_RED_SPREAD), 0.0F, spread(random, WARP_BLUE_BASE, WARP_BLUE_SPREAD));
        float scale = spread(random, WARP_SCALE_BASE, WARP_SCALE_SPREAD);
        float endScale = spread(random, WARP_SCALE_BASE, WARP_SCALE_SPREAD);
        level.addParticle(new WispFlameParticleOptions(color, WARP_ALPHA, scale, endScale, NO_DELAY), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void undying(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, spread(random, UNDYING_LOW_BASE, UNDYING_LOW_SPREAD), spread(random, UNDYING_GREEN_BASE, UNDYING_GREEN_SPREAD),
                spread(random, UNDYING_LOW_BASE, UNDYING_LOW_SPREAD));
        float scale = spread(random, UNDYING_SCALE_BASE, UNDYING_SCALE_SPREAD);
        float endScale = spread(random, UNDYING_SCALE_BASE, UNDYING_SCALE_SPREAD);
        level.addParticle(new WispFlameParticleOptions(color, UNDYING_ALPHA, scale, endScale, NO_DELAY), x, y, z, 0.0, RISE, 0.0);
    }

    private static void fiery(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        level.addParticle(new FlameFanParticleOptions(spread(random, FIERY_SCALE_BASE, FIERY_SCALE_SPREAD), NO_LIFT, FIERY_ALPHA), x, y, z, 0.0, RISE, 0.0);
    }

    private static void sickly(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        int color = ARGB.colorFromFloat(OPAQUE, SICKLY_RED, spread(random, SICKLY_GREEN_BASE, SICKLY_GREEN_SPREAD), spread(random, SICKLY_BLUE_BASE, SICKLY_BLUE_SPREAD));
        float scale = spread(random, SICKLY_SCALE_BASE, SICKLY_SCALE_SPREAD);
        level.addParticle(new FluxSwirlParticleOptions(color, scale, SICKLY_END_SCALE), x, y, z, 0.0, SINK, 0.0);
    }

    private static void venomous(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        level.addParticle(TTParticles.colorOf(TTParticles.GOO_DRIP, VENOM_RED, spread(random, VENOM_GREEN_BASE, VENOM_GREEN_SPREAD), spread(random, VENOM_BLUE_BASE, VENOM_BLUE_SPREAD)), x, y, z, 0.0,
                RISE_SLOW, 0.0);
    }

    private static void vampiric(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, VAMPIRIC_GATE)) {
            return;
        }
        level.addParticle(TTParticles.colorOf(TTParticles.GOO_DRIP, spread(random, VAMPIRIC_RED_BASE, VAMPIRIC_RED_SPREAD), 0.0F, 0.0F), x, y, z, 0.0, 0.0, 0.0);
    }

    private static void infested(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        if (!passes(random, HALF_GATE)) {
            return;
        }
        double width = mob.getBbWidth();
        double centerY = (mob.getBoundingBox().minY + mob.getBoundingBox().maxY) * HALF;
        double px = mob.getX() + (random.nextFloat() - HALF) * width;
        double pz = mob.getZ() + (random.nextFloat() - HALF) * width;
        level.addParticle(TTParticles.TAINT_SPLOSION.get(), px, centerY, pz, 0.0, 0.0, 0.0);
    }
}
