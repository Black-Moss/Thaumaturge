package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public final class EntityFallingTaint extends Entity implements IEntityWithComplexSpawn {
    private static final String BLOCK_KEY = "Block";
    private static final String ORIGIN_KEY = "Origin";
    private static final String TIME_KEY = "Time";
    private static final double GRAVITY = 0.04;
    private static final double AIR_DRAG = 0.98;
    private static final double LANDING_HORIZONTAL_DAMPING = 0.7;
    private static final double LANDING_VERTICAL_BOUNCE = -0.5;
    private static final int GRACE_TICKS = 100;
    private static final int MAX_FALL_TICKS = 600;
    private static final float LAND_VOLUME = 0.5F;
    private static final float LAND_PITCH = 1.0F;

    private BlockState tile = TTBlocks.TAINT_CRUST.get().defaultBlockState();
    private BlockPos origin = BlockPos.ZERO;
    private int time;

    public EntityFallingTaint(EntityType<? extends EntityFallingTaint> type, Level level) {
        super(type, level);
    }

    public EntityFallingTaint(Level level, double x, double y, double z, BlockState state, BlockPos origin) {
        this(TTEntities.FALLING_TAINT.get(), level);
        this.tile = state;
        this.origin = origin;
        restAt(x, y, z);
    }

    private void restAt(double x, double y, double z) {
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(x, y, z);
        this.setOldPosAndRot();
    }

    public BlockState getFallTile() {
        return this.tile;
    }

    public BlockPos origin() {
        return this.origin;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void tick() {
        if (this.tile.isAir()) {
            this.discard();
            return;
        }
        if (this.level() instanceof ServerLevel level && !clearOrigin(level)) {
            return;
        }
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -GRAVITY, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level() instanceof ServerLevel level) {
            this.time++;
            BlockPos pos = this.blockPosition();
            if (!this.onGround() && !level.getBlockState(pos.below()).is(TTBlocks.FLUX_GOO)) {
                fall(level, pos);
            } else {
                land(level, pos);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store(BLOCK_KEY, BlockState.CODEC, this.tile);
        output.putLong(ORIGIN_KEY, this.origin.asLong());
        output.putInt(TIME_KEY, this.time);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read(BLOCK_KEY, BlockState.CODEC).filter(state -> !state.isAir()).ifPresent(state -> this.tile = state);
        this.origin = BlockPos.of(input.getLongOr(ORIGIN_KEY, 0L));
        this.time = input.getIntOr(TIME_KEY, 0);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(Block.getId(this.tile)).writeBlockPos(this.origin);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        int stateId = buffer.readVarInt();
        BlockPos source = buffer.readBlockPos();
        this.tile = Block.stateById(stateId);
        this.origin = source;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    private boolean clearOrigin(ServerLevel level) {
        if (this.time != 0) {
            return true;
        }
        if (!level.getBlockState(this.origin).is(this.tile.getBlock())) {
            this.discard();
            return false;
        }
        level.setBlock(this.origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    private void fall(ServerLevel level, BlockPos pos) {
        this.setDeltaMovement(this.getDeltaMovement().scale(AIR_DRAG));
        boolean outsideWorld = pos.getY() < level.getMinY() || pos.getY() > level.getMaxY();
        if (this.time > GRACE_TICKS && outsideWorld || this.time > MAX_FALL_TICKS) {
            this.discard();
        }
    }

    private void land(ServerLevel level, BlockPos pos) {
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x * LANDING_HORIZONTAL_DAMPING, motion.y * LANDING_VERTICAL_BOUNCE, motion.z * LANDING_HORIZONTAL_DAMPING);
        level.playSound(null, pos, TTSounds.GORE.get(), SoundSource.BLOCKS, LAND_VOLUME, LAND_PITCH);
        this.discard();
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.canBeReplaced() || current.is(TTBlocks.FLUX_GOO)) {
            level.setBlock(pos, this.tile, Block.UPDATE_ALL);
        }
    }
}
