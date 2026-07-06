package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectCurse extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("curse");

    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int DURATION_TICKS_FACTOR = 20;
    private static final float POTENCY_DIVISOR = 2.0F;
    private static final float CASCADE_START = 0.85F;
    private static final float CASCADE_STEP = 0.15F;
    private static final int BAMF_COLOR = 6946821;
    private static final double MAX_SAP_RADIUS = 8.0;
    private static final double SAP_RADIUS_FACTOR = 1.5;
    private static final float COLOR_DIVISOR = 255.0F;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.MORTUUS;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (1.0F + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        FX.bamf(level, target.getLocation())
                .color(((BAMF_COLOR >> 16) & 0xFF) / COLOR_DIVISOR,
                        ((BAMF_COLOR >> 8) & 0xFF) / COLOR_DIVISOR,
                        (BAMF_COLOR & 0xFF) / COLOR_DIVISOR)
                .withSound()
                .fancy()
                .send();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            float damage = getDamageForDisplay(finalPower);
            int duration = DURATION_TICKS_FACTOR * getSettingValue("duration");
            int eff = (int) (getSettingValue("power") * finalPower / POTENCY_DIVISOR);
            if (eff < 0) {
                eff = 0;
            }
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, getPackage().getCaster()), damage);
            if (struck instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, eff));
                float c = CASCADE_START;
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration * 2, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration * 3, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.UNLUCK, duration * 3, eff));
                }
            }
        } else if (target instanceof BlockHitResult blockHit) {
            float f = (float) Math.min(MAX_SAP_RADIUS, SAP_RADIUS_FACTOR * getSettingValue("power") * finalPower);
            for (BlockPos pos : BlockPos.betweenClosed(
                    blockHit.getBlockPos().offset((int) -f, (int) -f, (int) -f),
                    blockHit.getBlockPos().offset((int) f, (int) f, (int) f))) {
                if (pos.distToCenterSqr(target.getLocation().x, target.getLocation().y, target.getLocation().z) <= f * f
                        && level.getBlockState(pos.above()).isAir()
                        && level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                    level.setBlockAndUpdate(pos.above(), TCBlocks.EFFECT_SAP.get().defaultBlockState());
                }
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration", new NodeSettingIntRange(1, 10))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .maxAge(8)
                .color(0.41F + level.getRandom().nextFloat() * 0.2F, 0.0F,
                        0.019F + level.getRandom().nextFloat() * 0.2F)
                .alpha(0.0F, level.getRandom().nextFloat(), level.getRandom().nextFloat(),
                        level.getRandom().nextFloat(), 0.0F)
                .grid(16)
                .particles(72 + level.getRandom().nextInt(4), 1, 1)
                .scale(2.0F + level.getRandom().nextFloat() * 4.0F)
                .loop(false)
                .slowDown(0.9)
                .gravity(0.0F)
                .rotation(level.getRandom().nextFloat(), 0.0F)
                .delay(level.getRandom().nextInt(4))
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ELDER_GUARDIAN_CURSE,
                SoundSource.PLAYERS, 0.15F, 1.0F + caster.level().getRandom().nextFloat() / 2.0F);
    }
}
