package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintSporeStalk;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractTaintSpore extends Monster {
    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 10;

    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(AbstractTaintSpore.class, EntityDataSerializers.INT);
    private static final String SIZE_KEY = "SporeSize";
    private static final byte EVENT_RELEASE = 16;
    private static final int RELEASE_TICKS = 30;
    private static final int UPKEEP_INTERVAL = 20;
    private static final int GROWTH_INTERVAL = 1200;
    private static final float STARVATION_DAMAGE = 1.0F;
    private static final float DISPLAY_GROWTH_PER_TICK = 0.02F;
    private static final double STILL_SPEED = 0.0;

    private boolean bursting;
    private boolean displayReady;
    private float displaySize;
    private float previousDisplaySize;
    private int releaseTicks;

    protected AbstractTaintSpore(EntityType<? extends AbstractTaintSpore> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    static AttributeSupplier.Builder createSporeAttributes(double maxHealth) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, maxHealth).add(Attributes.MOVEMENT_SPEED, STILL_SPEED);
    }

    protected abstract boolean requiresStalkSupport();

    protected abstract void onBurst(ServerLevel level);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIZE, MIN_SIZE);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide()) {
            advanceDisplay();
        }
    }

    private void advanceDisplay() {
        float target = getSporeSize();
        if (!displayReady) {
            displayReady = true;
            displaySize = target;
        }
        previousDisplaySize = displaySize;
        displaySize = target > displaySize ? Math.min(target, displaySize + DISPLAY_GROWTH_PER_TICK) : target;
        if (releaseTicks > 0) {
            releaseTicks--;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server && tickCount % UPKEEP_INTERVAL == 0) {
            upkeep(server);
        }
    }

    private void upkeep(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            demoteStalk(level);
            discard();
            return;
        }
        if (tickCount % GROWTH_INTERVAL == 0 && getSporeSize() < MAX_SIZE) {
            setSporeSize(getSporeSize() + 1);
        }
        if (!TaintBiomeManager.isTainted(level, blockPosition())) {
            hurtServer(level, damageSources().starve(), STARVATION_DAMAGE);
            if (isRemoved()) {
                return;
            }
        }
        if (requiresStalkSupport() && !level.getBlockState(blockPosition().below()).is(TTBlocks.TAINT_SPORE_STALK)) {
            burst(level);
        }
    }

    private void demoteStalk(ServerLevel level) {
        BlockPos below = blockPosition().below();
        BlockState state = level.getBlockState(below);
        if (state.is(TTBlocks.TAINT_SPORE_STALK) && state.getValue(BlockTaintSporeStalk.MATURE)) {
            level.setBlock(below, state.setValue(BlockTaintSporeStalk.MATURE, false), Block.UPDATE_CLIENTS);
        }
    }

    public int getSporeSize() {
        return entityData.get(SIZE);
    }

    protected void setSporeSize(int size) {
        entityData.set(SIZE, Mth.clamp(size, MIN_SIZE, MAX_SIZE));
    }

    public float getDisplaySize(float partialTick) {
        return displayReady ? Mth.lerp(partialTick, previousDisplaySize, displaySize) : getSporeSize();
    }

    public int releaseTicks() {
        return releaseTicks;
    }

    protected final void signalRelease(ServerLevel level) {
        level.broadcastEntityEvent(this, EVENT_RELEASE);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_RELEASE) {
            releaseTicks = RELEASE_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (level() instanceof ServerLevel server) {
            burst(server);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel server) {
            burst(server);
        } else {
            super.die(source);
        }
    }

    protected final void burst(ServerLevel level) {
        if (bursting) {
            return;
        }
        bursting = true;
        onBurst(level);
        demoteStalk(level);
        discard();
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        int size = entityData.get(SIZE);
        output.putInt(SIZE_KEY, size);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        int stored = input.getIntOr(SIZE_KEY, MIN_SIZE);
        setSporeSize(stored);
    }
}
