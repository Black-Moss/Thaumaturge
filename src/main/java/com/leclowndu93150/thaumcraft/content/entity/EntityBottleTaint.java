package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.api.entity.ITaintedMob;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCMobEffects;
import com.leclowndu93150.thaumcraft.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class EntityBottleTaint extends ThrowableItemProjectile implements ItemSupplier {
    private static final double SPLASH_RADIUS = 5.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final int GOO_ATTEMPTS = 10;
    private static final int GOO_SPREAD_RADIUS = 4;

    public EntityBottleTaint(EntityType<? extends EntityBottleTaint> type, Level level) {
        super(type, level);
    }

    public EntityBottleTaint(Level level, LivingEntity owner) {
        super(TCEntities.BOTTLE_TAINT.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GLASS_BOTTLE;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurtServer(serverLevel(), this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        applyAreaEffect(server);
        scatterGoo(server);
        server.playSound(null, this.blockPosition(), TCSounds.GORE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        server.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    private ServerLevel serverLevel() {
        return (ServerLevel) this.level();
    }

    private void applyAreaEffect(ServerLevel server) {
        AABB box = new AABB(this.position(), this.position()).inflate(SPLASH_RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, box,
                e -> !(e instanceof ITaintedMob) && !e.is(EntityTypeTags.UNDEAD))) {
            target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true, false));
        }
    }

    private void scatterGoo(ServerLevel server) {
        BlockPos center = this.blockPosition();
        for (int attempt = 0; attempt < GOO_ATTEMPTS; attempt++) {
            int xx = server.getRandom().nextInt(GOO_SPREAD_RADIUS * 2 + 1) - GOO_SPREAD_RADIUS;
            int zz = server.getRandom().nextInt(GOO_SPREAD_RADIUS * 2 + 1) - GOO_SPREAD_RADIUS;
            BlockPos candidate = center.offset(xx, 0, zz);
            if (!tryPlaceGoo(server, candidate) && !tryPlaceGoo(server, candidate.below())) {
                continue;
            }
        }
    }

    private static boolean tryPlaceGoo(ServerLevel server, BlockPos pos) {
        BlockState here = server.getBlockState(pos);
        BlockState below = server.getBlockState(pos.below());
        if (!here.canBeReplaced() && !here.isAir()) {
            return false;
        }
        if (!below.isSolid()) {
            return false;
        }
        if (server.getRandom().nextBoolean()) {
            return false;
        }
        server.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }
}
