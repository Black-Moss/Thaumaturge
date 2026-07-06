package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectHeal extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("heal");

    private static final int POWER_COMPLEXITY_FACTOR = 4;
    private static final float UNDEAD_DAMAGE_FACTOR = 1.5F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.VICTUS;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return -getSettingValue("power") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        FocusFX.impact(level, target.getLocation(), getKey());
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            if (living.isInvertedHealAndHarm()) {
                living.hurtServer(level,
                        level.damageSources().indirectMagic(getPackage().getCaster(), getPackage().getCaster()),
                        getSettingValue("power") * finalPower * UNDEAD_DAMAGE_FACTOR);
            } else {
                living.heal(getSettingValue("power") * finalPower);
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{new NodeSetting("power", "focus.heal.power", new NodeSettingIntRange(1, 5))};
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW,
                SoundSource.PLAYERS, 2.0F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.1F));
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx + level.getRandom().nextGaussian() * 0.01,
                        my + level.getRandom().nextGaussian() * 0.01,
                        mz + level.getRandom().nextGaussian() * 0.01)
                .maxAge((int) (10.0F + 10.0F * level.getRandom().nextFloat()))
                .color(1.0F, 1.0F, 1.0F)
                .alpha(0.0F, 0.7F, 0.7F, 0.0F)
                .grid(64)
                .particles(0, 1, 1)
                .scale(level.getRandom().nextFloat() * 2.0F, level.getRandom().nextFloat())
                .slowDown(0.8)
                .gravity((float) (level.getRandom().nextGaussian() * 0.1F))
                .random(0.0125F, 0.0125F, 0.0125F)
                .rotation((float) level.getRandom().nextGaussian())
                .delay(level.getRandom().nextInt(4))
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }
}
