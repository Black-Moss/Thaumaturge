package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

public final class TaintMobConversion {
    private static final float REPLACEMENT_PRESSURE = 0.04F;
    private static final float GENERIC_PRESSURE = 0.01F;
    private static final float MIN_HEALTH = 1.0F;

    private TaintMobConversion() {}

    public static boolean canConvert(ServerLevel level, LivingEntity source) {
        return !ThaumaturgeCommonConfig.WUSS_MODE.get() && level.getDifficulty() != Difficulty.PEACEFUL && source instanceof Mob && !(source instanceof ITaintedMob)
                && !source.is(TCEntityTags.TAINT_CONVERSION_IMMUNE);
    }

    public static @Nullable TaintConversion conversionFor(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(TCDataMaps.TAINT_CONVERSION);
    }

    public static void tryConvert(ServerLevel level, LivingEntity source) {
        if (!canConvert(level, source)) {
            return;
        }
        TaintConversion conversion = conversionFor(source.getType());
        Mob replacement = conversion == null ? null : createReplacement(level, source, conversion.into(), EntitySpawnReason.CONVERSION);
        if (replacement != null) {
            source.discard();
            level.addFreshEntity(replacement);
            TaintEcology.addPressure(level, replacement.blockPosition(), REPLACEMENT_PRESSURE);
            return;
        }
        ChampionHelper.makeTainted(source);
        TaintEcology.addPressure(level, source.blockPosition(), GENERIC_PRESSURE);
    }

    public static @Nullable Mob createReplacement(ServerLevel level, LivingEntity source, EntityType<?> into, EntitySpawnReason reason) {
        Entity created = into.create(level, reason);
        if (!(created instanceof Mob replacement)) {
            if (created != null) {
                created.discard();
            }
            return null;
        }
        replacement.snapTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        replacement.setYHeadRot(source.getYHeadRot());
        replacement.setDeltaMovement(source.getDeltaMovement());
        replacement.setSilent(source.isSilent());
        replacement.setGlowingTag(source.hasGlowingTag());
        replacement.setInvulnerable(source.isInvulnerable());
        if (source.hasCustomName()) {
            replacement.setCustomName(source.getCustomName());
            replacement.setCustomNameVisible(source.isCustomNameVisible());
        }
        if (source instanceof Mob sourceMob) {
            replacement.setNoAi(sourceMob.isNoAi());
            replacement.setLeftHanded(sourceMob.isLeftHanded());
            if (sourceMob.isPersistenceRequired()) {
                replacement.setPersistenceRequired();
            }
        }
        if (source instanceof AgeableMob sourceAgeable && replacement instanceof AgeableMob replacementAgeable) {
            replacementAgeable.setAge(sourceAgeable.getAge());
        }
        if (replacement instanceof TaintConversionTarget target) {
            target.copyConvertedState(source);
        }
        float healthRatio = source.getMaxHealth() <= 0.0F ? 1.0F : source.getHealth() / source.getMaxHealth();
        replacement.setHealth(Math.max(MIN_HEALTH, Math.min(replacement.getMaxHealth(), replacement.getMaxHealth() * healthRatio)));
        return replacement;
    }
}
