package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.flux.FluxGooFluid;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    public static final ThrowProfile THROW = new ThrowProfile(0.66F, 1.0F, -5.0F, 0.5F);

    private static final byte SHATTER_EVENT = 3;
    private static final double EFFECT_REACH = 5.0;
    private static final int EFFECT_TICKS = 100;
    private static final int PLANT_ATTEMPTS = 10;
    private static final float PLANT_CHANCE = 0.5F;
    private static final float PLANT_SPREAD = 5.0F;
    private static final int PLANT_SCAN_ABOVE = 2;
    private static final int PLANT_SCAN_BELOW = 3;
    private static final float PLANT_PRESSURE = 0.03F;
    private static final int GOO_ATTEMPTS = 3;
    private static final int GOO_REACH = 2;
    private static final int GOO_MAX_QUANTITY = 2;
    private static final int SPLOSION_COUNT = 100;
    private static final double SPLOSION_VELOCITY = 1.0;
    private static final int SHARD_COUNT = 8;
    private static final double SHARD_SPREAD = 0.15;
    private static final double SHARD_LIFT = 0.2;
    private static final float BREAK_VOLUME = 1.0F;
    private static final float BREAK_PITCH_BASE = 0.9F;
    private static final float BREAK_PITCH_SPREAD = 0.1F;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity thrower, ItemStack stack) {
        super(TTEntities.BOTTLE_TAINT.get(), thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.BOTTLE_TAINT.get();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != SHATTER_EVENT) {
            super.handleEntityEvent(id);
            return;
        }
        Level level = this.level();
        RandomSource random = this.getRandom();
        for (int i = 0; i < SPLOSION_COUNT; i++) {
            level.addParticle(TTParticles.TAINT_SPLOSION.get(), this.getX(), this.getY() + random.nextDouble() * this.getBbHeight(), this.getZ(), signed(random) * SPLOSION_VELOCITY,
                    signed(random) * SPLOSION_VELOCITY, signed(random) * SPLOSION_VELOCITY);
        }
        ItemParticleOption shard = new ItemParticleOption(ParticleTypes.ITEM, TTItems.BOTTLE_TAINT.get());
        for (int i = 0; i < SHARD_COUNT; i++) {
            level.addParticle(shard, this.getX(), this.getY(), this.getZ(), random.nextGaussian() * SHARD_SPREAD, random.nextDouble() * SHARD_LIFT, random.nextGaussian() * SHARD_SPREAD);
        }
        level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, BREAK_VOLUME, BREAK_PITCH_BASE + random.nextFloat() * BREAK_PITCH_SPREAD,
                false);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 impact = hit.getLocation();
        BlockPos origin = BlockPos.containing(impact);
        afflictNearby(level, impact);
        plantFibres(level, origin);
        scatterGoo(level, origin);
        level.broadcastEntityEvent(this, SHATTER_EVENT);
        this.discard();
    }

    private void afflictNearby(ServerLevel level, Vec3 impact) {
        AABB reach = new AABB(impact, impact).inflate(EFFECT_REACH);
        for (LivingEntity creature : level.getEntitiesOfClass(LivingEntity.class, reach, EntityBottleTaint::canAfflict)) {
            creature.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, EFFECT_TICKS, 0));
        }
    }

    private void plantFibres(ServerLevel level, BlockPos origin) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < PLANT_ATTEMPTS; i++) {
            if (random.nextFloat() >= PLANT_CHANCE) {
                continue;
            }
            BlockPos column = origin.offset(plantOffset(random), 0, plantOffset(random));
            if (level.hasChunkAt(column) && !TaintBlooms.isProtected(level, column) && (TaintBiomeManager.isTainted(level, column) || TaintBiomeManager.taintColumn(level, column))) {
                plantInColumn(level, column);
            }
        }
    }

    private void plantInColumn(ServerLevel level, BlockPos column) {
        for (int y = column.getY() + PLANT_SCAN_ABOVE; y >= column.getY() - PLANT_SCAN_BELOW; y--) {
            BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
            BlockState state = level.getBlockState(pos);
            if ((state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty() && BlockTaintFibre.hasSolidAttachment(level, pos)) {
                level.setBlock(pos, BlockTaintFibre.stateForWorld(level, pos), Block.UPDATE_ALL);
                TaintApi.addEcologicalPressure(level, pos, PLANT_PRESSURE);
                return;
            }
        }
    }

    private void scatterGoo(ServerLevel level, BlockPos origin) {
        RandomSource random = this.getRandom();
        for (int i = 0; i < GOO_ATTEMPTS; i++) {
            BlockPos column = origin.offset(random.nextInt(GOO_REACH * 2 + 1) - GOO_REACH, 0, random.nextInt(GOO_REACH * 2 + 1) - GOO_REACH);
            int quantity = 1 + random.nextInt(GOO_MAX_QUANTITY);
            if (level.hasChunkAt(column)) {
                placeGoo(level, column, quantity);
            }
        }
    }

    private void placeGoo(ServerLevel level, BlockPos column, int quantity) {
        if (!tryGoo(level, column, quantity)) {
            tryGoo(level, column.below(), quantity);
        }
    }

    private boolean tryGoo(ServerLevel level, BlockPos pos, int quantity) {
        BlockState state = level.getBlockState(pos);
        BlockPos support = pos.below();
        if ((state.isAir() || state.canBeReplaced()) && level.getBlockState(support).isRedstoneConductor(level, support)) {
            level.setBlock(pos, FluxGooFluid.gooBlockState(quantity), Block.UPDATE_ALL);
            return true;
        }
        return false;
    }

    private static boolean canAfflict(LivingEntity creature) {
        return !MobTraits.isTainted(creature) && !creature.is(EntityTypeTags.UNDEAD);
    }

    private static int plantOffset(RandomSource random) {
        return (int) ((random.nextFloat() - random.nextFloat()) * PLANT_SPREAD);
    }

    private static double signed(RandomSource random) {
        return random.nextDouble() * 2.0 - 1.0;
    }
}
