package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityTaintCow extends Cow implements ITaintedMob {
    private static final double MAX_HEALTH = 40.0;
    private static final double ATTACK_DAMAGE = 6.0;
    private static final double MOVEMENT_SPEED = 0.27;
    private static final double FOLLOW_RANGE = 24.0;

    public EntityTaintCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.FOLLOW_RANGE,
                FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        TaintedLivestockGoals.addHostileGoals(this, goalSelector, targetSelector, true, true);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        TaintMobCombat.onHit(this, target, hit);
        return hit;
    }
}
