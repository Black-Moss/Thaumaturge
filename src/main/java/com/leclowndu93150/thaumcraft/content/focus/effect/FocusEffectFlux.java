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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFlux extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("flux");

    private static final int BASE_DAMAGE = 3;
    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int PARTICLE_START = 128;
    private static final int PARTICLE_NUM = 14;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.VITIUM;
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
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, getPackage().getCaster()),
                    getDamageForDisplay(finalPower));
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5))};
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW,
                SoundSource.PLAYERS, 2.0F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.1F));
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx + level.getRandom().nextGaussian() * 0.01,
                        my + level.getRandom().nextGaussian() * 0.01,
                        mz + level.getRandom().nextGaussian() * 0.01)
                .drift(dx, dy, dz)
                .maxAge((int) (15.0F + 10.0F * level.getRandom().nextFloat()))
                .color(0.25F + level.getRandom().nextFloat() * 0.25F, 0.0F,
                        0.25F + level.getRandom().nextFloat() * 0.25F)
                .alpha(0.0F, 1.0F, 1.0F, 0.0F)
                .grid(64)
                .particles(PARTICLE_START, PARTICLE_NUM, 1)
                .scale(2.0F + level.getRandom().nextFloat(), 0.25F + level.getRandom().nextFloat() * 0.25F)
                .loop(true)
                .slowDown(0.9)
                .gravity((float) (level.getRandom().nextGaussian() * 0.1F))
                .random(0.0125F, 0.0125F, 0.0125F)
                .rotation((float) level.getRandom().nextGaussian())
                .delay(level.getRandom().nextInt(4))
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }
}
