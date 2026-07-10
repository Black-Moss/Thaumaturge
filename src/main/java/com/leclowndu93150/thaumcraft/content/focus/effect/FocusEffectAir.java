package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.content.focus.FocusFX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectAir extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("air");

    private static final int BASE_DAMAGE = 1;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final float KNOCKBACK_FACTOR = 0.25F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    private static final int PARTICLE_GRID = 32;
    private static final int PARTICLE_START = 337;
    private static final int PARTICLE_NUM = 5;

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
        return TCAspects.AER;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
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
        level.playSound(null, target.getLocation().x, target.getLocation().y, target.getLocation().z,
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 0.66F);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            float damage = getDamageForDisplay(finalPower);
            struck.hurtServer(level, level.damageSources().thrown(struck, getPackage().getCaster()), damage);
            if (struck instanceof LivingEntity living) {
                if (trajectory != null) {
                    living.knockback(damage * KNOCKBACK_FACTOR, -trajectory.direction().x, -trajectory.direction().z);
                } else {
                    living.knockback(damage * KNOCKBACK_FACTOR,
                            -Mth.sin(struck.getYRot() * DEG_TO_RAD),
                            Mth.cos(struck.getYRot() * DEG_TO_RAD));
                }
            }
            return true;
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
        float s = (float) (2.0 + level.getRandom().nextGaussian() * 0.5);
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .gravity(-0.1F)
                .maxAge(20 + level.getRandom().nextInt(10))
                .alpha(0.5F, 0.0F)
                .grid(PARTICLE_GRID)
                .particles(PARTICLE_START, PARTICLE_NUM, 1)
                .slowDown(0.75)
                .rotation((float) level.getRandom().nextGaussian() / 2.0F)
                .scale(s, s * 2.0F)
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), TCSounds.WIND.get(),
                SoundSource.PLAYERS, 0.125F, 2.0F);
    }
}
