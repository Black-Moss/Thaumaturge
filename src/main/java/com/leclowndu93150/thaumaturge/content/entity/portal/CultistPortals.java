package com.leclowndu93150.thaumaturge.content.entity.portal;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

public final class CultistPortals {
    public static final byte PULSE_EVENT = 16;
    public static final int PULSE_TICKS = 10;
    public static final float SOUND_VOLUME = 0.75F;
    public static final int AMBIENT_INTERVAL = 540;

    private static final float KNIGHT_CHANCE = 0.67F;
    private static final double ARRIVAL_LIFT = 0.25;
    private static final double TOUCH_RANGE_SQR = 3.0;
    private static final float ZAP_VOLUME = 1.0F;
    private static final float ZAP_PITCH_BASE = 1.0F;
    private static final float ZAP_PITCH_SPREAD = 0.1F;
    private static final float ARRIVAL_VOLUME = 1.0F;
    private static final float ARRIVAL_PITCH = 1.0F;

    private CultistPortals() {}

    public static EntityCultist rollMinion(ServerLevel level, RandomSource random) {
        EntityType<? extends EntityCultist> type = random.nextFloat() < KNIGHT_CHANCE ? TTEntities.CULTIST_KNIGHT.get() : TTEntities.CULTIST_CLERIC.get();
        return type.create(level, EntitySpawnReason.MOB_SUMMONED);
    }

    public static int cultistsNear(Mob portal, double range) {
        return portal.level().getEntitiesOfClass(EntityCultist.class, portal.getBoundingBox().inflate(range)).size();
    }

    public static void summon(Mob portal, ServerLevel level, Mob arrival) {
        RandomSource random = portal.getRandom();
        double x = portal.getX() + (random.nextFloat() - random.nextFloat());
        double y = portal.getY() + ARRIVAL_LIFT;
        double z = portal.getZ() + (random.nextFloat() - random.nextFloat());
        arrival.snapTo(x, y, z, arrival.getYRot(), arrival.getXRot());
        EventHooks.finalizeMobSpawn(arrival, level, level.getCurrentDifficultyAt(arrival.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        level.addFreshEntity(arrival);
        if (arrival instanceof EntityCultist cultist) {
            cultist.spawnCultistArrivalParticles();
        }
        level.playSound(null, arrival.getX(), arrival.getY(), arrival.getZ(), TTSounds.WIND.get(), SoundSource.HOSTILE, ARRIVAL_VOLUME, ARRIVAL_PITCH);
    }

    public static void touch(Mob portal, Player player, float damage) {
        if (!(portal.level() instanceof ServerLevel level) || portal.distanceToSqr(player) >= TOUCH_RANGE_SQR) {
            return;
        }
        if (player.hurtServer(level, level.damageSources().indirectMagic(portal, portal), damage)) {
            RandomSource random = portal.getRandom();
            level.playSound(null, portal.getX(), portal.getY(), portal.getZ(), TTSounds.ZAP.get(), portal.getSoundSource(), ZAP_VOLUME,
                    ZAP_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * ZAP_PITCH_SPREAD);
        }
    }

    public static void collapse(Mob portal, float power) {
        if (portal.level() instanceof ServerLevel level) {
            level.explode(portal, portal.getX(), portal.getY(), portal.getZ(), power, Level.ExplosionInteraction.NONE);
        }
    }
}
