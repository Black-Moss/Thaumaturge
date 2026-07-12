package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.aura.AuraHelper;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumcraft.content.fx.FX;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectPrimal extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("primal");

    private static final int BASE_COMPLEXITY = 20;
    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int BASE_DAMAGE = 4;
    private static final float EXPLOSION_STRENGTH = 1.5F;
    private static final int CHAOS_CHANCE = 100;
    private static final float CHAOS_FLUX = 5.0F;
    private static final int PARTICLE_GRID = 16;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_primal"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.PRAECANTATIO;
    }

    @Override
    public int getComplexity() {
        return BASE_COMPLEXITY + getSettingValue("power") * POWER_COMPLEXITY_FACTOR;
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
        Vec3 origin = target.getLocation();
        FX.bamf(level, origin).withSound().fancy().send();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, getPackage().getCaster()),
                    getDamageForDisplay(finalPower));
        }
        level.explode(getPackage().getCaster(), origin.x, origin.y, origin.z,
                EXPLOSION_STRENGTH, Level.ExplosionInteraction.MOB);
        if (level.getRandom().nextInt(CHAOS_CHANCE) == 0) {
            BlockPos pos = BlockPos.containing(origin);
            if (level.getRandom().nextBoolean()) {
                AuraHelper.polluteAura(level, pos, CHAOS_FLUX, true);
            } else {
                NodeGenerator.createRandomNodeAt(level, pos.above(), level.getRandom(), false, false, true,
                        NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
            }
        }
        return true;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("power", "focus.common.power", new NodeSettingIntRange(1, 5))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .maxAge(8)
                .color(level.getRandom().nextFloat(), level.getRandom().nextFloat(), level.getRandom().nextFloat())
                .alpha(0.0F, 0.8F, 0.8F, 0.8F, 0.0F)
                .grid(PARTICLE_GRID)
                .particles(72 + level.getRandom().nextInt(4), 1, 1)
                .scale(1.5F + level.getRandom().nextFloat() * 2.0F)
                .loop(false)
                .slowDown(0.9)
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }
}
