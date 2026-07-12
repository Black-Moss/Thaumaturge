package com.leclowndu93150.thaumcraft.content.focus.effect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.NodeSetting;
import com.leclowndu93150.thaumcraft.api.casters.NodeSettingIntRange;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import com.leclowndu93150.thaumcraft.content.entity.EntityFireBat;
import com.leclowndu93150.thaumcraft.content.fx.data.FXGenericData;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectHellbat extends FocusEffect {
    private static final Identifier KEY = TCIds.rl("hellbat");

    private static final int BAT_COMPLEXITY_FACTOR = 8;
    private static final int SPAWN_LEVEL_EVENT = 2004;
    private static final float SPAWN_SPREAD = 0.5F;
    private static final int PARTICLE_START = 640;
    private static final int PARTICLE_NUM = 10;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public ResearchGate getResearch() {
        return new ResearchGate(TCIds.rl("focus_hellbat"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> getAspect() {
        return TCAspects.BESTIA;
    }

    @Override
    public int getComplexity() {
        return getSettingValue("bats") * BAT_COMPLEXITY_FACTOR;
    }

    @Override
    public float getDamageForDisplay(float finalPower) {
        return getSettingValue("bats") * finalPower;
    }

    @Override
    public boolean execute(HitResult target, @Nullable Trajectory trajectory, float finalPower, int num) {
        if (!(getPackage().getLevel() instanceof ServerLevel level)) {
            return false;
        }
        LivingEntity struck = target instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living ? living : null;
        LivingEntity caster = getPackage().getCaster();
        if (struck == caster) {
            struck = null;
        }
        Vec3 origin = target.getLocation();
        int bats = getSettingValue("bats");
        int bonus = Math.round(finalPower) - 1;
        boolean spawned = false;
        for (int i = 0; i < bats; i++) {
            EntityFireBat bat = TCEntities.FIRE_BAT.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (bat == null) {
                continue;
            }
            bat.snapTo(
                    origin.x + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SPAWN_SPREAD,
                    origin.y + 1.0 + level.getRandom().nextFloat() * SPAWN_SPREAD,
                    origin.z + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SPAWN_SPREAD,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            bat.summon(caster, struck, bonus);
            if (level.addFreshEntity(bat)) {
                spawned = true;
            }
        }
        if (spawned) {
            level.levelEvent(SPAWN_LEVEL_EVENT, BlockPos.containing(origin), 0);
            level.playSound(null, origin.x, origin.y, origin.z, TCSounds.ICE.get(), SoundSource.PLAYERS,
                    0.2F, 0.95F + level.getRandom().nextFloat() * 0.1F);
        }
        return spawned;
    }

    @Override
    public NodeSetting[] createSettings() {
        return new NodeSetting[]{
                new NodeSetting("bats", "focus.hellbat.bats", new NodeSettingIntRange(1, 3))
        };
    }

    @Override
    public void renderParticleFX(Level level, double x, double y, double z, double mx, double my, double mz,
            double dx, double dy, double dz) {
        FXGenericData data = FXGenericData.builder()
                .motion(mx, my, mz)
                .drift(dx, dy, dz)
                .gravity(-0.1F)
                .maxAge(10)
                .alpha(0.7F)
                .particles(PARTICLE_START, PARTICLE_NUM, 1)
                .slowDown(0.75)
                .scale((float) (1.0 + level.getRandom().nextGaussian() * 0.2F))
                .build();
        level.addParticle(data, x, y, z, 0.0, 0.0, 0.0);
    }
}
