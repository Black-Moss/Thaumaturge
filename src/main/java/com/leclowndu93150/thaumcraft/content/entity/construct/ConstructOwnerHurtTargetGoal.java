package com.leclowndu93150.thaumcraft.content.entity.construct;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public final class ConstructOwnerHurtTargetGoal extends TargetGoal {
    private final EntityOwnedConstruct construct;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public ConstructOwnerHurtTargetGoal(EntityOwnedConstruct construct) {
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
        ownerLastHurt = owner.getLastHurtMob();
        int ts = owner.getLastHurtMobTimestamp();
        return ts != timestamp && canAttack(ownerLastHurt, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurt);
        LivingEntity owner = construct.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
