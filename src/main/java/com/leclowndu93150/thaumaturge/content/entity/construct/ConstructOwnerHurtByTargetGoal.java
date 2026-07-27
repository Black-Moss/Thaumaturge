package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public final class ConstructOwnerHurtByTargetGoal extends TargetGoal {
    private final EntityOwnedConstruct construct;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public ConstructOwnerHurtByTargetGoal(EntityOwnedConstruct construct) {
        super(construct, false);
        this.construct = construct;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!construct.isOwned()) {
            return false;
        }
        LivingEntity owner = construct.getOwner();
        if (owner == null) {
            return false;
        }
        ownerLastHurtBy = owner.getLastHurtByMob();
        int ts = owner.getLastHurtByMobTimestamp();
        return ts != timestamp && canAttack(ownerLastHurtBy, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurtBy);
        LivingEntity owner = construct.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
