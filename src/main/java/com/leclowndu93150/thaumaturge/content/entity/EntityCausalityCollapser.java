package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.projectile.AbstractThrownCharge;
import com.leclowndu93150.thaumaturge.content.entity.projectile.ChargeTrail;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityCausalityCollapser extends AbstractThrownCharge {
    public static final ThrowProfile THROW = new ThrowProfile(0.8F, 2.0F, 0.0F, 0.3F);

    private static final float BLAST_POWER = 2.0F;
    private static final double RIFT_REACH = 3.0;
    private static final int TRAIL_COLOR = 0xF07A1E;
    private static final float TRAIL_SHADE = 0.3F;
    private static final float TRAIL_ALPHA = 0.55F;
    private static final float TRAIL_SCALE = 3.0F;
    private static final ChargeTrail EMBER_TRAIL = new ChargeTrail(TRAIL_COLOR, TRAIL_SHADE, TRAIL_ALPHA, TRAIL_SCALE);

    public EntityCausalityCollapser(EntityType<? extends EntityCausalityCollapser> type, Level level) {
        super(type, level);
    }

    public EntityCausalityCollapser(Level level, LivingEntity thrower, ItemStack stack) {
        super(TTEntities.CAUSALITY_COLLAPSER.get(), thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.CAUSALITY_COLLAPSER.get();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof EntityFluxRift && entity.isAlive() || super.canHitEntity(entity);
    }

    @Override
    protected ChargeTrail trail() {
        return EMBER_TRAIL;
    }

    @Override
    protected void detonate(ServerLevel level) {
        level.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_POWER, Level.ExplosionInteraction.MOB);
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, this.getBoundingBox().inflate(RIFT_REACH))) {
            rift.beginCollapse();
        }
    }
}
