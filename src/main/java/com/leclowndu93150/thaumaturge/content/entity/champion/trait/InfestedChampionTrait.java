package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class InfestedChampionTrait extends AbstractChampionTrait {
    private static final float SPAWN_CHANCE = 0.4F;
    private static final float CHEST_HEIGHT_FRACTION = 0.5F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        if (attacker != null && mob.level() instanceof ServerLevel level && mob.getRandom().nextFloat() < SPAWN_CHANCE) {
            EntityTaintCrawler crawler = TTEntities.TAINT_CRAWLER.get().create(level, EntitySpawnReason.EVENT);
            if (crawler != null) {
                crawler.snapTo(mob.getX(), mob.getY() + mob.getBbHeight() * CHEST_HEIGHT_FRACTION, mob.getZ(), mob.getRandom().nextFloat() * FULL_TURN_DEGREES, 0.0F);
                level.addFreshEntity(crawler);
                level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), TTSounds.GORE.get(), SoundSource.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
            }
        }
        return amount;
    }
}
