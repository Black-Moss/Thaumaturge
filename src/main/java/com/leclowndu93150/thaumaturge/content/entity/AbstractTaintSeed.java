package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTaintSeed extends AbstractRootedTaint {
    private static final double FOLLOW_RANGE = 16.0;
    private static final int PULSE_INTERVAL = 20;
    private static final int FUMES_PER_PULSE = 3;
    private static final double BODY_HALF = 0.5;
    private static final float FUME_SCALE = 1.5F;
    private static final double FUME_RISE_BASE = 0.015;
    private static final double FUME_RISE_SPREAD = 0.015;
    private static final float STARVATION_DAMAGE = 0.5F;
    private static final float STARVATION_FLUX = 0.1F;
    private static final int MAX_BONUS_ATTEMPTS = 3;
    private static final float ATTEMPTS_PER_SATURATION = 2.0F;
    private static final int SPREAD_HORIZONTAL_PER_AREA = 3;
    private static final double AFFLICTION_REACH_PER_AREA = 4.0;
    private static final int AFFLICTION_TICKS = 100;
    private static final float STRIKE_AGITATION = 0.5F;
    private static final float AGITATION_EASE = 0.75F;
    private static final float AGITATION_REST = 0.001F;

    public float attackAnim;
    private @Nullable BlockPos anchor;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, FOLLOW_RANGE);
    }

    @Override
    protected void rootedServerStep(ServerLevel level) {
        trackAnchor(level);
        if (!this.isAlive()) {
            return;
        }
        if (this.tickCount % PULSE_INTERVAL == 0) {
            pulse(level);
        }
    }

    private void trackAnchor(ServerLevel level) {
        BlockPos here = this.blockPosition();
        if (here.equals(this.anchor)) {
            return;
        }
        if (this.anchor != null) {
            TaintHelper.unregisterSeedAnchor(level, this.anchor);
        }
        this.anchor = here;
        TaintHelper.registerSeedAnchor(level, here);
    }

    private void pulse(ServerLevel level) {
        BlockPos here = this.blockPosition();
        releaseFumes(level);
        TaintEcology.touchActiveSeed(level, here);
        TaintBiomeManager.taintColumn(level, here);
        feedOrStarve(level, here);
        if (this.isAlive()) {
            afflictSurroundings(level);
        }
    }

    private void releaseFumes(ServerLevel level) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < FUMES_PER_PULSE; i++) {
            Vec3 spot = new Vec3(this.getRandomX(BODY_HALF), this.getRandomY(), this.getRandomZ(BODY_HALF));
            Effects.taint(level, spot).motion(0.0, FUME_RISE_BASE + random.nextDouble() * FUME_RISE_SPREAD, 0.0).scale(FUME_SCALE).send();
        }
    }

    private void feedOrStarve(ServerLevel level, BlockPos here) {
        float saturation = AuraHelper.getFluxSaturation(level, here);
        int attempts = 1;
        if (saturation <= 0.0F) {
            this.hurtServer(level, level.damageSources().starve(), STARVATION_DAMAGE);
            AuraHelper.polluteAura(level, here, STARVATION_FLUX, false);
        } else {
            attempts += Math.min(MAX_BONUS_ATTEMPTS, Mth.floor(saturation * ATTEMPTS_PER_SATURATION));
        }
        spreadFibres(level, attempts);
    }

    private void spreadFibres(ServerLevel level, int attempts) {
        int vertical = getArea();
        int horizontal = SPREAD_HORIZONTAL_PER_AREA * vertical;
        BlockPos centre = this.blockPosition();
        RandomSource random = this.getRandom();
        for (int i = 0; i < attempts; i++) {
            BlockPos spot = centre.offset(evenOffset(random, horizontal), evenOffset(random, vertical), evenOffset(random, horizontal));
            TaintHelper.attemptFibreGrowth(level, spot, true);
        }
    }

    private void afflictSurroundings(ServerLevel level) {
        int area = getArea();
        AABB reach = this.getBoundingBox().inflate(AFFLICTION_REACH_PER_AREA * area);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, reach, this::canAfflict)) {
            victim.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, AFFLICTION_TICKS, area - 1, true, false, false), this);
        }
    }

    private boolean canAfflict(LivingEntity victim) {
        return victim != this && victim.isAlive() && !victim.isSpectator() && !MobTraits.isTainted(victim);
    }

    private static int evenOffset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }

    @Override
    protected void onStrike(ServerLevel level) {
        this.playSound(TTSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void startStrikeAnimation() {
        this.attackAnim = STRIKE_AGITATION;
    }

    @Override
    protected void tickStrikeAnimation() {
        if (this.attackAnim <= 0.0F) {
            return;
        }
        this.attackAnim *= AGITATION_EASE;
        if (this.attackAnim < AGITATION_REST) {
            this.attackAnim = 0.0F;
        }
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (this.level() instanceof ServerLevel server && this.anchor != null) {
            TaintHelper.unregisterSeedAnchor(server, this.anchor);
            this.anchor = null;
        }
    }
}
