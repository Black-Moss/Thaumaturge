package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

final class PechMoods {
    private static final byte EVENT_MUTTER = 16;
    private static final byte EVENT_GOSSIP = 17;
    private static final byte EVENT_TEMPER = 18;
    private static final byte EVENT_DELIGHT = 19;

    private static final float MUTTER_LEVEL = (float) Math.PI;
    private static final float GOSSIP_LEVEL = (float) (Math.PI * 2.0);
    private static final float CHATTER_DECAY = 0.75F;

    private static final int CONTENT_ONE_IN = 25;
    private static final int CROSS_ONE_IN = 15;
    private static final int TEMPER_BURST = 5;
    private static final int DELIGHT_BURST = 5;
    private static final double LOW_OFFSET = 0.5;
    private static final double SPREAD_FRACTION = 1.0;
    private static final double DRIFT = 0.02;

    private PechMoods() {}

    static void decay(EntityPech pech) {
        if (pech.chatterLevel > 0.0F) {
            pech.chatterLevel = pech.chatterLevel * CHATTER_DECAY;
        }
    }

    static void mutter(EntityPech pech) {
        pech.chatterLevel = MUTTER_LEVEL;
        pech.level().broadcastEntityEvent(pech, EVENT_MUTTER);
    }

    static void gossip(EntityPech pech) {
        pech.chatterLevel = GOSSIP_LEVEL;
        pech.level().broadcastEntityEvent(pech, EVENT_GOSSIP);
    }

    static void flareUp(EntityPech pech) {
        pech.chatterLevel = GOSSIP_LEVEL;
        pech.level().broadcastEntityEvent(pech, EVENT_TEMPER);
    }

    static void delight(EntityPech pech) {
        pech.level().broadcastEntityEvent(pech, EVENT_DELIGHT);
    }

    static boolean handleEvent(EntityPech pech, byte id) {
        switch (id) {
            case EVENT_MUTTER -> pech.chatterLevel = MUTTER_LEVEL;
            case EVENT_GOSSIP -> pech.chatterLevel = GOSSIP_LEVEL;
            case EVENT_TEMPER -> {
                pech.chatterLevel = GOSSIP_LEVEL;
                puff(pech, ParticleTypes.ANGRY_VILLAGER, TEMPER_BURST);
            }
            case EVENT_DELIGHT -> puff(pech, ParticleTypes.HAPPY_VILLAGER, DELIGHT_BURST);
            default -> {
                return false;
            }
        }
        return true;
    }

    static void tickClient(EntityPech pech) {
        RandomSource random = pech.getRandom();
        if (pech.isDomesticated() && random.nextInt(CONTENT_ONE_IN) == 0) {
            puff(pech, ParticleTypes.HAPPY_VILLAGER, 1);
        }
        if (pech.rageTicks() > 0 && random.nextInt(CROSS_ONE_IN) == 0) {
            puff(pech, ParticleTypes.ANGRY_VILLAGER, 1);
        }
    }

    private static void puff(EntityPech pech, ParticleOptions particle, int count) {
        Level level = pech.level();
        RandomSource random = pech.getRandom();
        double spread = pech.getBbWidth() * SPREAD_FRACTION;
        double rise = pech.getBbHeight();
        for (int i = 0; i < count; i++) {
            double x = pech.getX() + (random.nextDouble() * 2.0 - 1.0) * spread;
            double y = pech.getY() + LOW_OFFSET + random.nextDouble() * rise;
            double z = pech.getZ() + (random.nextDouble() * 2.0 - 1.0) * spread;
            level.addParticle(particle, x, y, z, random.nextGaussian() * DRIFT, random.nextGaussian() * DRIFT, random.nextGaussian() * DRIFT);
        }
    }
}
