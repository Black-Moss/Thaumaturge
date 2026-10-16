package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityFluxRift extends Entity {
    public static final int MAX_RIFT_SIZE = 100;
    public static final int MIN_RIFT_SIZE = 1;
    public static final float MAX_STABILITY = 100.0F;
    public static final float MIN_STABILITY = -100.0F;

    private static final String SIZE_KEY = "RiftSize";
    private static final String COLLAPSE_SIZE_KEY = "MaxSize";
    private static final String SEED_KEY = "RiftSeed";
    private static final String STABILITY_KEY = "Stability";
    private static final String COLLAPSING_KEY = "collapse";

    private static final EntityDataAccessor<Integer> DATA_SIZE = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SEED = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_STABILITY = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_COLLAPSING = SynchedEntityData.defineId(EntityFluxRift.class, EntityDataSerializers.BOOLEAN);

    private static final int DEFAULT_RIFT_SIZE = 5;
    private static final int UNSEEDED = 0;
    private static final int MIN_BODY_POINTS = 3;
    private static final float STABILIZER_NUDGE = 0.125F;
    private static final int DECAY_INTERVAL = 120;
    private static final float DECAY_AMOUNT = 0.2F;
    private static final int HUM_INTERVAL = 300;
    private static final float HUM_VOLUME = 0.15F;
    private static final float HUM_VOLUME_DEVIATION = 0.066F;
    private static final float HUM_PITCH = 0.75F;
    private static final float HUM_PITCH_DEVIATION = 0.1F;

    public final List<Vec3> outline;
    public final List<Float> outlineWidths;

    private final RiftOutline body = new RiftOutline();
    private final RiftSegmentSweep sweep = new RiftSegmentSweep();
    private final RiftCollapse collapse = new RiftCollapse();
    private final RiftTaskSchedule schedule = new RiftTaskSchedule(List.of(new RiftGrowthTask(), new RiftEventTask()));
    private int collapseSize;

    public EntityFluxRift(EntityType<? extends EntityFluxRift> type, Level level) {
        super(type, level);
        this.outline = this.body.points();
        this.outlineWidths = this.body.widths();
        this.noPhysics = true;
        this.setNoGravity(true);
        reshape();
    }

    public static void spawnNear(ServerLevel level, BlockPos chunkCorner) {
        RiftSpawner.tryForm(level, chunkCorner);
    }

    public int currentSize() {
        return this.entityData.get(DATA_SIZE);
    }

    public void resize(int size) {
        this.entityData.set(DATA_SIZE, Mth.clamp(size, 0, MAX_RIFT_SIZE));
    }

    public int collapseSize() {
        return this.collapseSize;
    }

    public int seed() {
        return this.entityData.get(DATA_SEED);
    }

    public void reseed(int seed) {
        this.entityData.set(DATA_SEED, seed);
    }

    public float stabilityValue() {
        return this.entityData.get(DATA_STABILITY);
    }

    public Stability stabilityTier() {
        return Stability.of(stabilityValue());
    }

    public void adjustStability(float amount) {
        setStability(stabilityValue() + amount);
    }

    public void nudgeStability() {
        adjustStability(STABILIZER_NUDGE);
    }

    public boolean isCollapsing() {
        return this.entityData.get(DATA_COLLAPSING);
    }

    public void beginCollapse() {
        if (isCollapsing()) {
            return;
        }
        this.collapseSize = currentSize();
        this.entityData.set(DATA_COLLAPSING, true);
    }

    private void setStability(float stability) {
        this.entityData.set(DATA_STABILITY, Mth.clamp(stability, MIN_STABILITY, MAX_STABILITY));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, DEFAULT_RIFT_SIZE);
        builder.define(DATA_SEED, UNSEEDED);
        builder.define(DATA_STABILITY, 0.0F);
        builder.define(DATA_COLLAPSING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_SIZE.equals(accessor) || DATA_SEED.equals(accessor)) {
            reshape();
        }
    }

    private void reshape() {
        this.body.shape(seed(), currentSize());
        this.setBoundingBox(this.makeBoundingBox());
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        if (this.body == null) {
            return super.makeBoundingBox(position);
        }
        return this.body.bounds().move(position);
    }

    @Override
    public void tick() {
        if (this.level() instanceof ServerLevel level) {
            serverTick(level);
        }
    }

    private void serverTick(ServerLevel level) {
        if (seed() == UNSEEDED) {
            reseed(nonZeroSeed(this.random));
        }
        if (this.outline.size() < MIN_BODY_POINTS) {
            beginCollapse();
        }
        this.sweep.tick(level, this);
        if (isCollapsing() && this.collapse.tick(level, this)) {
            return;
        }
        if (this.tickCount % DECAY_INTERVAL == 0) {
            adjustStability(-DECAY_AMOUNT);
        }
        this.schedule.tick(level, this);
        if (!this.isRemoved() && this.tickCount % HUM_INTERVAL == 0) {
            hum(level);
        }
    }

    static int nonZeroSeed(RandomSource random) {
        int seed = random.nextInt();
        return seed == UNSEEDED ? 1 : seed;
    }

    private void hum(ServerLevel level) {
        float volume = Math.max(0.0F, HUM_VOLUME + (float) this.random.nextGaussian() * HUM_VOLUME_DEVIATION);
        float pitch = HUM_PITCH + (float) this.random.nextGaussian() * HUM_PITCH_DEVIATION;
        level.playSound(null, this.getX(), this.getY(), this.getZ(), TTSounds.EVILPORTAL.get(), SoundSource.AMBIENT, volume, pitch);
    }

    @Override
    public void move(MoverType type, Vec3 delta) {}

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity source) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt(SIZE_KEY, currentSize());
        output.putInt(COLLAPSE_SIZE_KEY, this.collapseSize);
        output.putInt(SEED_KEY, seed());
        output.putFloat(STABILITY_KEY, stabilityValue());
        output.putBoolean(COLLAPSING_KEY, isCollapsing());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        reseed(input.getIntOr(SEED_KEY, UNSEEDED));
        resize(input.getIntOr(SIZE_KEY, DEFAULT_RIFT_SIZE));
        setStability(input.getFloatOr(STABILITY_KEY, 0.0F));
        boolean collapsing = input.getBooleanOr(COLLAPSING_KEY, false);
        int savedCollapseSize = input.getIntOr(COLLAPSE_SIZE_KEY, 0);
        this.collapseSize = collapsing && savedCollapseSize <= 0 ? currentSize() : savedCollapseSize;
        this.entityData.set(DATA_COLLAPSING, collapsing);
    }

    public enum Stability {
        VERY_STABLE, STABLE, UNSTABLE, VERY_UNSTABLE;

        private static final float VERY_STABLE_ABOVE = 50.0F;
        private static final float VERY_UNSTABLE_AT = -25.0F;

        public static Stability of(float stability) {
            if (stability > VERY_STABLE_ABOVE) {
                return VERY_STABLE;
            }
            if (stability >= 0.0F) {
                return STABLE;
            }
            return stability > VERY_UNSTABLE_AT ? UNSTABLE : VERY_UNSTABLE;
        }
    }
}
