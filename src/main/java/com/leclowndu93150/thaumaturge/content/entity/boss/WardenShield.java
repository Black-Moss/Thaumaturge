package com.leclowndu93150.thaumaturge.content.entity.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class WardenShield {
    private static final double CAPACITY_FACTOR = 0.66;
    private static final int REGENERATION_INTERVAL = 25;
    private static final float REGENERATION_AMOUNT = 1.0F;

    private final LivingEntity bearer;
    private final ServerBossEvent bar;

    WardenShield(LivingEntity bearer) {
        this.bearer = bearer;
        this.bar = new ServerBossEvent(Mth.createInsecureUUID(bearer.getRandom()), Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    static double capacityFor(double maxHealth) {
        return maxHealth * CAPACITY_FACTOR;
    }

    void raise() {
        bearer.setAbsorptionAmount(bearer.getAbsorptionAmount() + capacity());
    }

    boolean broken() {
        return bearer.getAbsorptionAmount() <= 0.0F;
    }

    void tick() {
        int capacity = capacity();
        float absorption = bearer.getAbsorptionAmount();
        if (bearer.tickCount % REGENERATION_INTERVAL == 0 && absorption < capacity && bearer.invulnerableTime <= 0) {
            bearer.setAbsorptionAmount(absorption + REGENERATION_AMOUNT);
        }
        bar.setProgress(capacity > 0 ? Mth.clamp(bearer.getAbsorptionAmount() / capacity, 0.0F, 1.0F) : 0.0F);
    }

    void show(ServerPlayer player) {
        bar.addPlayer(player);
    }

    void hide(ServerPlayer player) {
        bar.removePlayer(player);
    }

    private int capacity() {
        return (int) capacityFor(bearer.getAttributeBaseValue(Attributes.MAX_HEALTH));
    }
}
