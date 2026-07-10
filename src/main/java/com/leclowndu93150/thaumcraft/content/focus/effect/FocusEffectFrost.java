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
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFrost extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("frost");

    private static final int BASE_DAMAGE = 3;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final int SLOW_TICKS_PER_DURATION = 20;
    private static final float POTENCY_DIVISOR = 3.0F;
    private static final float MAX_FREEZE_RADIUS = 16.0F;
    private static final int FREEZE_RADIUS_FACTOR = 2;
    private static final int MELT_DELAY_MIN = 60;
    private static final int MELT_DELAY_MAX = 120;

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
        return TCAspects.GELUM;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("duration") + getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
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
            float damage = getDamageForDisplay(finalPower);
            int duration = SLOW_TICKS_PER_DURATION * getSettingValue("duration");
            int potency = (int) (1.0F + getSettingValue("power") * finalPower / POTENCY_DIVISOR);
            struck.hurtServer(level, level.damageSources().thrown(struck, getPackage().getCaster()), damage);
            if (struck instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, potency));
            }
        } else if (target instanceof BlockHitResult blockHit) {
            float f = Math.min(MAX_FREEZE_RADIUS, FREEZE_RADIUS_FACTOR * getSettingValue("power") * finalPower);
            for (BlockPos pos : BlockPos.betweenClosed(
                    blockHit.getBlockPos().offset((int) -f, (int) -f, (int) -f),
                    blockHit.getBlockPos().offset((int) f, (int) f, (int) f))) {
                if (pos.distToCenterSqr(target.getLocation().x, target.getLocation().y, target.getLocation().z) > f * f) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.WATER) && state.getFluidState().isSource()
                        && level.isUnobstructed(Blocks.FROSTED_ICE.defaultBlockState(), pos, CollisionContext.empty())) {
                    level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                    level.scheduleTick(pos.immutable(), Blocks.FROSTED_ICE,
                            Mth.nextInt(level.getRandom(), MELT_DELAY_MIN, MELT_DELAY_MAX));
                }
            }
        }
        return false;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5)),
                new NodeSetting("duration", "focus.common.duration", new NodeSettingIntRange(2, 10))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .maxAge(40 + level.getRandom().nextInt(40))
                .alpha(1.0F, 0.0F)
                .particles(8, 1, 1)
                .gravity(0.033F)
                .slowDown(0.8)
                .random(0.0025F, 1.0E-4F, 0.0025F)
                .scale((float) (0.7F + level.getRandom().nextGaussian() * 0.3F))
                .rotation(level.getRandom().nextFloat() * 3.0F, (float) level.getRandom().nextGaussian() / 4.0F)
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(Entity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ZOMBIE_VILLAGER_CURE,
                SoundSource.PLAYERS, 0.2F, 1.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
