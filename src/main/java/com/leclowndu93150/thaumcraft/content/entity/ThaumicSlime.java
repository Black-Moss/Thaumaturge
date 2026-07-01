package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.api.entity.ITaintedMob;
import com.leclowndu93150.thaumcraft.content.entity.ai.ThaumicSlimeSpitGoal;
import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public final class ThaumicSlime extends Slime implements ITaintedMob {
    private static final int LAUNCH_TICKS = 5;
    private static final float LAUNCHED_BONUS_DAMAGE = 2.0F;

    private int launched;
    private boolean wasOnGroundLast;

    public ThaumicSlime(EntityType<? extends ThaumicSlime> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new ThaumicSlimeSpitGoal(this));
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    protected float getAttackDamage() {
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (base <= 0.0F) {
            base = this.getSize() + 1.0F;
        }
        return base + (launched > 0 ? LAUNCHED_BONUS_DAMAGE : 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (launched > 0) {
            launched--;
        }
        boolean onGroundNow = this.onGround();
        if (onGroundNow && !wasOnGroundLast && this.level() instanceof ServerLevel server) {
            FX.slimeJumpFX(server, this, getSize());
        }
        wasOnGroundLast = onGroundNow;
    }

    public void markLaunched() {
        this.launched = LAUNCH_TICKS;
    }

    public boolean isLaunched() {
        return launched > 0;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getSquishSound() {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.GORE.get();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                   EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        int sizeBits = 1 + level.getRandom().nextInt(3);
        int size = 1 << sizeBits;
        this.setSize(size, true);
        return groupData;
    }

    public static boolean checkSpawnRules(EntityType<ThaumicSlime> type, LevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos,
                                          RandomSource random) {
        return false;
    }
}
