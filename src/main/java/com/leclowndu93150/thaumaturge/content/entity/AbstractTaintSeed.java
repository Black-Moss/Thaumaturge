package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractTaintSeed extends AbstractRootedTaint {
    private static final double SEED_FOLLOW_RANGE = 16.0;
    private static final float STRIKE_ANIMATION_START = 0.5F;
    private static final float STRIKE_ANIMATION_DECAY = 0.75F;
    private static final float STRIKE_ANIMATION_FLOOR = 0.001F;
    private static final int PULSE_INTERVAL = 20;
    private static final int FUME_COUNT = 3;
    private static final double FUME_RISE_BASE = 0.015;
    private static final double FUME_RISE_SPREAD = 0.015;
    private static final float FUME_SCALE = 1.5F;
    private static final float STARVATION_DAMAGE = 0.5F;
    private static final float STARVATION_FLUX = 0.1F;
    private static final int SPREAD_EXTRA_LIMIT = 3;
    private static final float SPREAD_SATURATION_FACTOR = 2.0F;
    private static final int SPREAD_HORIZONTAL_REACH = 3;
    private static final int AURA_REACH = 4;
    private static final int AURA_EFFECT_TICKS = 100;

    public float attackAnim;

    private boolean registered;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, SEED_FOLLOW_RANGE);
    }

    @Override
    protected void onStrike(ServerLevel level) {
        this.playSound(TTSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void startStrikeAnimation() {
        this.attackAnim = STRIKE_ANIMATION_START;
    }

    @Override
    protected void tickStrikeAnimation() {
        this.attackAnim *= STRIKE_ANIMATION_DECAY;
        if (this.attackAnim < STRIKE_ANIMATION_FLOOR) {
            this.attackAnim = 0.0F;
        }
    }

    @Override
    protected void rootedServerStep(ServerLevel level) {
        BlockPos pos = this.blockPosition();
        if (!this.registered) {
            this.registered = true;
            TaintApi.addTaintSeed(level, pos);
        }
        if (this.tickCount % PULSE_INTERVAL != 0) {
            return;
        }
        emitFumes(level);
        TaintEcology.touchActiveSeed(level, pos);
        TaintBiomeManager.taintColumn(level, pos);
        float saturation = Math.max(0.0F, AuraHelper.getFluxSaturation(level, pos));
        if (saturation <= 0.0F) {
            this.hurtServer(level, level.damageSources().starve(), STARVATION_DAMAGE);
            AuraHelper.polluteAura(level, pos, STARVATION_FLUX, false);
        } else {
            spreadFibres(level, pos, saturation);
        }
        afflictNearby(level);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel server) {
            TaintApi.removeTaintSeed(server, this.blockPosition());
        }
        super.remove(reason);
    }

    private void emitFumes(ServerLevel level) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < FUME_COUNT; i++) {
            Vec3 at = new Vec3(this.getX() + (random.nextDouble() - 0.5) * this.getBbWidth(), this.getY() + random.nextDouble() * this.getBbHeight(),
                    this.getZ() + (random.nextDouble() - 0.5) * this.getBbWidth());
            Effects.taint(level, at).motion(0.0, FUME_RISE_BASE + random.nextDouble() * FUME_RISE_SPREAD, 0.0).scale(FUME_SCALE).send();
        }
    }

    private void spreadFibres(ServerLevel level, BlockPos pos, float saturation) {
        RandomSource random = this.getRandom();
        int area = this.getArea();
        int attempts = 1 + Math.min(SPREAD_EXTRA_LIMIT, (int) Math.floor(saturation * SPREAD_SATURATION_FACTOR));
        int horizontal = SPREAD_HORIZONTAL_REACH * area;
        for (int i = 0; i < attempts; i++) {
            BlockPos target = pos.offset(offset(random, horizontal), offset(random, area), offset(random, horizontal));
            TaintApi.spreadFibres(level, target, true);
        }
    }

    private void afflictNearby(ServerLevel level) {
        int area = this.getArea();
        AABB reach = this.getBoundingBox().inflate(AURA_REACH * area);
        int amplifier = Math.max(area - 1, 0);
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, reach, this::canAfflict)) {
            creature.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, AURA_EFFECT_TICKS, amplifier, true, false, false));
        }
    }

    private boolean canAfflict(LivingEntity creature) {
        return creature != this && !MobTraits.isTainted(creature);
    }

    private static int offset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }
}
