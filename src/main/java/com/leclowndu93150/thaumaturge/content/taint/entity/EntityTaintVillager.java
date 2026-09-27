package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class EntityTaintVillager extends Villager implements ITaintedMob, TaintConversionTarget {
    private static final double MAX_HEALTH = 30.0;
    private static final double ATTACK_DAMAGE = 4.0;
    private static final double MOVEMENT_SPEED = 0.3;
    private static final double FOLLOW_RANGE = 24.0;

    public EntityTaintVillager(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Villager.createAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE).add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        TaintedLivestockGoals.addHostileGoals(this, goalSelector, targetSelector, false, false);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {}

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void copyConvertedState(LivingEntity source) {
        if (source instanceof Villager villager) {
            setVillagerData(villager.getVillagerData());
        }
    }
}
