package com.leclowndu93150.thaumcraft.content.entity;

import com.leclowndu93150.thaumcraft.registry.TCEntities;
import com.leclowndu93150.thaumcraft.registry.TCItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class EntityCausalityCollapser extends ThrowableItemProjectile {
    private static final float EXPLOSION_STRENGTH = 2.0F;
    private static final double RIFT_COLLAPSE_RANGE = 3.0;
    private static final int TRAIL_STEPS = 3;

    public EntityCausalityCollapser(EntityType<? extends EntityCausalityCollapser> type, Level level) {
        super(type, level);
    }

    public EntityCausalityCollapser(Level level, LivingEntity shooter, ItemStack stack) {
        super(TCEntities.CAUSALITY_COLLAPSER.get(), shooter, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.CAUSALITY_COLLAPSER.get();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof EntityFluxRift || super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            for (int i = 0; i < TRAIL_STEPS; i++) {
                double coeff = (double) i / TRAIL_STEPS;
                this.level().addParticle(ParticleTypes.FLAME,
                        this.xOld + (this.getX() - this.xOld) * coeff,
                        this.yOld + (this.getY() - this.yOld) * coeff + this.getBbHeight() / 2.0F,
                        this.zOld + (this.getZ() - this.zOld) * coeff,
                        0.0125F * (this.random.nextFloat() - 0.5F),
                        0.0125F * (this.random.nextFloat() - 0.5F),
                        0.0125F * (this.random.nextFloat() - 0.5F));
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level().isClientSide()) {
            return;
        }
        this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                EXPLOSION_STRENGTH, Level.ExplosionInteraction.MOB);
        for (EntityFluxRift rift : this.level().getEntitiesOfClass(EntityFluxRift.class,
                this.getBoundingBox().inflate(RIFT_COLLAPSE_RANGE))) {
            rift.setCollapse(true);
        }
        this.discard();
    }
}
