package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class AbstractTaintacle extends AbstractRootedTaint {
    private static final double TAINTACLE_FOLLOW_RANGE = 12.0;
    private static final double SPAWN_EXCLUSION_HORIZONTAL = 24.0;
    private static final double SPAWN_EXCLUSION_VERTICAL = 8.0;
    private static final int STARVATION_INTERVAL = 20;
    private static final float STARVATION_DAMAGE = 1.0F;
    private static final float REST_FLAIL = 1.0F;
    private static final float STRIKE_FLAIL = 3.0F;
    private static final float FLAIL_DECAY = 0.01F;
    private static final float NO_ENRAGE = 0.0F;

    public float flailIntensity = REST_FLAIL;

    protected AbstractTaintacle(EntityType<? extends AbstractTaintacle> type, Level level) {
        super(type, level);
    }

    public float enrage() {
        return NO_ENRAGE;
    }

    @Override
    protected void rootedServerStep(ServerLevel level) {
        boolean starvationTick = this.tickCount % STARVATION_INTERVAL == 0;
        if (starvationTick && !isInTaintedBiome(level)) {
            this.hurtServer(level, level.damageSources().starve(), STARVATION_DAMAGE);
        }
    }

    @Override
    protected void startStrikeAnimation() {
        flailTo(STRIKE_FLAIL);
    }

    @Override
    protected void tickStrikeAnimation() {
        if (this.flailIntensity > REST_FLAIL) {
            flailTo(this.flailIntensity - FLAIL_DECAY);
        }
    }

    private void flailTo(float intensity) {
        this.flailIntensity = intensity;
    }

    private boolean isInTaintedBiome(ServerLevel level) {
        return level.getBiome(this.blockPosition()).is(TTBiomeTags.IS_TAINTED);
    }

    public static boolean checkTaintacleSpawnRules(EntityType<? extends AbstractTaintacle> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getBiome(pos).is(TTBiomeTags.IS_TAINTED)) {
            return false;
        }
        boolean rooted = isTaintSubstrate(level.getBlockState(pos)) || isTaintSubstrate(level.getBlockState(pos.below()));
        if (!rooted) {
            return false;
        }
        AABB exclusion = new AABB(pos).inflate(SPAWN_EXCLUSION_HORIZONTAL, SPAWN_EXCLUSION_VERTICAL, SPAWN_EXCLUSION_HORIZONTAL);
        return level.getEntitiesOfClass(EntityTaintacle.class, exclusion).isEmpty() && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public static AttributeSupplier.Builder createTaintacleAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, TAINTACLE_FOLLOW_RANGE);
    }

    private static boolean isTaintSubstrate(BlockState state) {
        return state.is(TTBlocks.TAINT_FIBRE) || state.is(TTBlocks.TAINT_SOIL);
    }
}
