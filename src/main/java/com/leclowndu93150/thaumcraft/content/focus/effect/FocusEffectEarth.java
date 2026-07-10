package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectEarth extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("earth");

    private static final int DAMAGE_FACTOR = 2;
    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final float HARDNESS_DIVISOR = 25.0F;
    private static final float BREAK_VIS_COST = 0.1F;
    private static final int PARTICLE_START_BASE = 75;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_elemental"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.TERRA;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return DAMAGE_FACTOR * getSettingValue("power") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        FocusFX.impact(level, target.getLocation(), getKey());
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            struck.hurtServer(level, level.damageSources().thrown(struck, getPackage().getCaster()),
                    getDamageForDisplay(finalPower));
            return true;
        }
        if (target instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            if (getPackage().getCaster() instanceof Player player
                    && level.getBlockState(pos).getDestroySpeed(level, pos) <= getDamageForDisplay(finalPower) / HARDNESS_DIVISOR) {
                BlockBreakerEngine.addBreaker(level, pos, level.getBlockState(pos), player,
                        false, false, 0, 1.0F, 0.0F, 1.0F, num, BREAK_VIS_COST);
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5))};
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        float s = (float) (1.0 + level.getRandom().nextGaussian() * 0.2F);
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .gravity(0.4F)
                .layer(1)
                .maxAge(20 + level.getRandom().nextInt(10))
                .alpha(1.0F, 0.0F)
                .particles(PARTICLE_START_BASE + level.getRandom().nextInt(4), 1, 1)
                .slowDown(0.9)
                .rotation((float) level.getRandom().nextGaussian())
                .scale(s, s / 2.0F)
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.PLAYERS, 0.25F, 1.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
