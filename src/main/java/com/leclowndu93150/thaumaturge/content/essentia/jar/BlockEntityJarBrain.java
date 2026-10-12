package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.mixin.world.entity.ExperienceOrbAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityJarBrain extends AbstractSyncedBlockEntity {
    public static final int XP_MAX = 2000;

    private static final String XP_KEY = "XP";
    private static final double PULL_RANGE = 8.0;
    private static final double PULL_FALLOFF_DISTANCE = 25.0;
    private static final double PULL_HORIZONTAL_WEIGHT = 0.3;
    private static final double PULL_VERTICAL_WEIGHT = 0.5;
    private static final double MIN_PULL_DISTANCE = 1.0E-3;
    private static final double COLLECT_MARGIN = 0.1;
    private static final double BLOCK_CENTER = 0.5;
    private static final float EAT_VOLUME = 0.1F;
    private static final float EAT_BASE_PITCH = 1.0F;
    private static final float EAT_PITCH_SPREAD = 0.2F;
    private static final float SIGH_VOLUME = 0.15F;
    private static final float SIGH_BASE_PITCH = 0.8F;
    private static final float SIGH_PITCH_SPREAD = 0.4F;
    private static final float TURN_RATE = 0.04F;
    private static final float IDLE_SPIN = 0.01F;
    private static final double PLAYER_RANGE = 6.0;
    private static final long SIGH_FIRST_DELAY = 30L;
    private static final long SIGH_MIN_DELAY = 100L;
    private static final int SIGH_DELAY_SPREAD = 500;
    private static final long UNSCHEDULED = -1L;

    public float rota;
    public float rotb;

    private int xp;
    private int eatDelay;
    private float targetYaw;
    private long nextSigh = UNSCHEDULED;

    public BlockEntityJarBrain(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR_BRAIN.get(), pos, state);
    }

    public int xp() {
        return xp;
    }

    public void setXp(int newXp) {
        xp = Mth.clamp(newXp, 0, XP_MAX);
        setChangedAndSync();
    }

    public void setEatDelay(int ticks) {
        eatDelay = ticks;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain jar) {
        ExperienceOrb lure = jar.xp < XP_MAX ? nearestOrb(level, pos) : null;
        if (jar.eatDelay > 0) {
            jar.eatDelay--;
        } else if (jar.xp < XP_MAX) {
            if (lure != null) {
                jar.attractOrb(lure, pos);
            }
            jar.collectOrbs(level, pos);
        }
        jar.sigh(level, pos, lure);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityJarBrain jar) {
        jar.rotb = jar.rota;
        ExperienceOrb lure = jar.xp < XP_MAX ? nearestOrb(level, pos) : null;
        Vec3 center = Vec3.atCenterOf(pos);
        if (lure != null) {
            jar.targetYaw = yawToward(center, lure.position());
        } else {
            Player watcher = level.getNearestPlayer(center.x, center.y, center.z, PLAYER_RANGE, false);
            jar.targetYaw = watcher != null ? yawToward(center, watcher.position()) : wrapRadians(jar.targetYaw + IDLE_SPIN);
        }
        float remaining = wrapRadians(jar.targetYaw - jar.rota);
        jar.rota = wrapRadians(jar.rota + remaining * TURN_RATE);
    }

    private static float wrapRadians(float angle) {
        return Mth.wrapDegrees(angle * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
    }

    private static float yawToward(Vec3 from, Vec3 to) {
        return (float) Mth.atan2(to.z - from.z, to.x - from.x);
    }

    private static @Nullable ExperienceOrb nearestOrb(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(PULL_RANGE), EntitySelector.ENTITY_STILL_ALIVE);
        ExperienceOrb nearest = null;
        double best = Double.MAX_VALUE;
        for (ExperienceOrb orb : orbs) {
            double distance = orb.distanceToSqr(center);
            if (distance <= best) {
                best = distance;
                nearest = orb;
            }
        }
        return nearest;
    }

    private void attractOrb(ExperienceOrb orb, BlockPos pos) {
        Vec3 toJar = Vec3.atCenterOf(pos).subtract(orb.position());
        double distance = toJar.length();
        if (distance < MIN_PULL_DISTANCE) {
            return;
        }
        double falloff = 1.0 - distance / PULL_FALLOFF_DISTANCE;
        double strength = falloff * falloff;
        double horizontal = strength * PULL_HORIZONTAL_WEIGHT / distance;
        double vertical = strength * PULL_VERTICAL_WEIGHT / distance;
        orb.setDeltaMovement(orb.getDeltaMovement().add(toJar.x * horizontal, toJar.y * vertical, toJar.z * horizontal));
    }

    private void collectOrbs(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(COLLECT_MARGIN), EntitySelector.ENTITY_STILL_ALIVE)) {
            if (xp >= XP_MAX) {
                return;
            }
            int eaten = orb.getValue() * ((ExperienceOrbAccessor) orb).thaumaturge$getCount();
            orb.discard();
            setXp(xp + eaten);
            level.playSound(null, orb.getX(), orb.getY(), orb.getZ(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, EAT_VOLUME, EAT_BASE_PITCH + (random.nextFloat() - random.nextFloat()) * EAT_PITCH_SPREAD);
        }
    }

    private void sigh(Level level, BlockPos pos, @Nullable ExperienceOrb lure) {
        if (lure != null) {
            return;
        }
        double x = pos.getX() + BLOCK_CENTER;
        double y = pos.getY() + BLOCK_CENTER;
        double z = pos.getZ() + BLOCK_CENTER;
        if (level.getNearestPlayer(x, y, z, PLAYER_RANGE, false) == null) {
            return;
        }
        long now = level.getGameTime();
        if (nextSigh == UNSCHEDULED) {
            nextSigh = now + SIGH_FIRST_DELAY;
            return;
        }
        if (now < nextSigh) {
            return;
        }
        RandomSource random = level.getRandom();
        nextSigh = now + SIGH_MIN_DELAY + random.nextInt(SIGH_DELAY_SPREAD);
        level.playSound(null, x, y, z, TTSounds.BRAIN.get(), SoundSource.AMBIENT, SIGH_VOLUME, SIGH_BASE_PITCH + random.nextFloat() * SIGH_PITCH_SPREAD);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        xp = input.getIntOr(XP_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(XP_KEY, xp);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (xp > 0) {
            components.set(TTDataComponents.STORED_XP.get(), xp);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        Integer stored = components.get(TTDataComponents.STORED_XP.get());
        if (stored != null) {
            setXp(stored);
        }
    }
}
