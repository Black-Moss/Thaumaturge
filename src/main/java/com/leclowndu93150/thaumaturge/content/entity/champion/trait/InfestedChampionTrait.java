package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class InfestedChampionTrait extends AbstractChampionTrait {
    private static final float BURST_CHANCE = 0.4F;
    private static final double SPAWN_HEIGHT_SHARE = 0.5;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float GORE_VOLUME = 0.5F;
    private static final float GORE_PITCH = 1.0F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        if (attacker != null && mob.level() instanceof ServerLevel level && mob.getRandom().nextFloat() < BURST_CHANCE) {
            releaseCrawler(level, mob);
        }
        return amount;
    }

    private static void releaseCrawler(ServerLevel level, LivingEntity host) {
        EntityTaintCrawler crawler = TTEntities.TAINT_CRAWLER.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (crawler == null) {
            return;
        }
        RandomSource random = host.getRandom();
        double y = host.getY() + host.getBbHeight() * SPAWN_HEIGHT_SHARE;
        crawler.snapTo(host.getX(), y, host.getZ(), random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        level.addFreshEntity(crawler);
        host.playSound(TTSounds.GORE.get(), GORE_VOLUME, GORE_PITCH);
    }
}
