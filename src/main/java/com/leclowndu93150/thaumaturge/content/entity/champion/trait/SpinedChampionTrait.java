package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class SpinedChampionTrait extends AbstractChampionTrait {
    private static final int THORNS_BASE = 1;
    private static final int THORNS_SPREAD = 3;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        if (attacker != null && !source.is(DamageTypes.THORNS) && mob.level() instanceof ServerLevel level) {
            attacker.hurtServer(level, mob.damageSources().thorns(mob), THORNS_BASE + mob.getRandom().nextInt(THORNS_SPREAD));
            level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.THORNS_HIT, SoundSource.HOSTILE, SOUND_VOLUME, SOUND_PITCH);
        }
        return amount;
    }
}
