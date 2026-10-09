package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

public final class CultistHurtByTargetGoal extends HurtByTargetGoal {
    private static final double ALERT_RANGE_Y = 10.0;

    public CultistHurtByTargetGoal(PathfinderMob mob) {
        super(mob);
        setAlertOthers();
    }

    @Override
    protected void alertOthers() {
        LivingEntity attacker = mob.getLastHurtByMob();
        if (attacker == null) {
            return;
        }
        double range = getFollowDistance();
        AABB area = mob.getBoundingBox().inflate(range, ALERT_RANGE_Y, range);
        for (EntityCultist other : mob.level().getEntitiesOfClass(EntityCultist.class, area, EntitySelector.NO_SPECTATORS)) {
            if (other != mob && other.getTarget() == null && !other.isAlliedTo(attacker)) {
                alertOther(other, attacker);
            }
        }
    }
}
