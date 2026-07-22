package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.api.damagesource.TCDamageSources;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.particle.FlameFanParticleOptions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFire extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("fire");

    private static final int BASE_DAMAGE = 3;
    private static final int DURATION_COMPLEXITY_FACTOR = 1;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final int FIRE_PLACE_FLAGS = 11;
    private static final int PARTICLE_START = 640;
    private static final int PARTICLE_NUM = 10;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("base_auromancy"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.IGNIS;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") * DURATION_COMPLEXITY_FACTOR
                + getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return (BASE_DAMAGE + getSettingValue("power")) * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        FocusFX.impact(level, target.getLocation(), getKey());
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            if (struck.fireImmune()) {
                return false;
            }
            float fire = 1 + getSettingValue("duration") * getSettingValue("duration");
            float damage = getDamageForDisplay(finalPower);
            fire *= finalPower;
            struck.hurtServer(level, TCDamageSources.focusFire(level, struck, getPackage().getCaster()), damage);
            if (fire > 0.0F) {
                struck.igniteForSeconds(Math.round(fire));
            }
            return true;
        }
        if (target instanceof BlockHitResult blockHit && getSettingValue("duration") > 0) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (level.getBlockState(pos).isAir() && level.getRandom().nextFloat() < finalPower) {
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F,
                        level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, BaseFireBlock.getState(level, pos), FIRE_PLACE_FLAGS);
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.fire.burn", new NodeSettingIntRange(0, 5))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FlameFanParticleOptions data = new FlameFanParticleOptions((float) (1.5 + level.getRandom().nextGaussian() * 0.2F), -0.2F, 0.7F);
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 1.0F, 1.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
