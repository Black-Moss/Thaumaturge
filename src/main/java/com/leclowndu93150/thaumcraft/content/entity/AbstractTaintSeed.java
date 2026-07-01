package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.entity.ITaintedMob;
import com.leclowndu93150.thaumcraft.content.taint.TaintHelper;
import com.leclowndu93150.thaumcraft.content.taint.spread.TaintSeedRegistry;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class AbstractTaintSeed extends Monster implements ITaintedMob {
    private static final int SPREAD_INTERVAL = 20;
    private static final float STARVE_DAMAGE = 0.5F;
    private static final float STARVE_POLLUTION = 0.1F;
    private static final float FLUX_TAINT_RADIUS_MULT = 4.0F;
    private static final int FLUX_TAINT_TICKS = 100;

    private boolean registered;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (!registered) {
            TaintSeedRegistry.get(server).addSeed(this.blockPosition());
            registered = true;
        }
        if (this.tickCount % SPREAD_INTERVAL != 0) {
            return;
        }
        BlockPos pos = this.blockPosition();
        float saturation = AuraHelper.getFluxSaturation(server, pos);
        if (saturation <= 0.0F) {
            this.hurtServer(server, server.damageSources().starve(), STARVE_DAMAGE);
            AuraHelper.polluteAura(server, pos, STARVE_POLLUTION, false);
        } else {
            int area = getArea();
            int dx = Mth.nextInt(server.getRandom(), -area * 3, area * 3);
            int dy = Mth.nextInt(server.getRandom(), -area, area);
            int dz = Mth.nextInt(server.getRandom(), -area * 3, area * 3);
            TaintHelper.spreadFibres(server, pos.offset(dx, dy, dz), true);
        }
        applyAuraTouch(server);
    }

    private void applyAuraTouch(ServerLevel server) {
        double radius = getArea() * FLUX_TAINT_RADIUS_MULT;
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius), e -> e != this && !(e instanceof ITaintedMob))) {
            target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT,
                    FLUX_TAINT_TICKS, Math.max(0, getArea() - 1), true, false, false));
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel server) {
            TaintSeedRegistry.get(server).removeSeed(this.blockPosition());
        }
        super.remove(reason);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.TENTACLE.get();
    }
}
