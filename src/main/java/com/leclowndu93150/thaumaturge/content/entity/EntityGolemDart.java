package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class EntityGolemDart extends AbstractArrow {

    public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) {
        super(type, level);
    }

    private EntityGolemDart(LivingEntity shooter) {
        super(TTEntities.GOLEM_DART.get(), shooter, shooter.level(), new ItemStack(Items.ARROW), null);
        pickup = Pickup.DISALLOWED;
    }

    public static void loose(LivingEntity shooter, LivingEntity target, double damage, double lift, float speed, float inaccuracy) {
        EntityGolemDart dart = new EntityGolemDart(shooter);
        dart.setBaseDamage(damage);
        Vec3 aim = new Vec3(target.getX(), target.getEyeY() + lift, target.getZ()).subtract(dart.position());
        dart.shoot(aim.x, aim.y, aim.z, speed, inaccuracy);
        shooter.level().addFreshEntity(dart);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return Items.ARROW.getDefaultInstance();
    }
}
