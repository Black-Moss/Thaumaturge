package com.leclowndu93150.thaumaturge.content.equipment.runic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ShieldRescan {
    private static final int NONE = 0;
    private static final float NO_ABSORPTION = 0.0F;

    private ShieldRescan() {}

    public static void run(ServerPlayer player, RunicShieldState state) {
        int newMax = RunicShielding.worn(player);
        int previousMax = state.maxCharge;
        state.maxCharge = newMax;
        clampAbsorption(player, previousMax - newMax);
        syncModifier(player, newMax);
    }

    private static void clampAbsorption(ServerPlayer player, int lost) {
        if (lost <= NONE) {
            return;
        }
        player.setAbsorptionAmount(Math.max(NO_ABSORPTION, player.getAbsorptionAmount() - lost));
    }

    private static void syncModifier(ServerPlayer player, int max) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(RunicShielding.MODIFIER_ID);
        int applied = existing == null ? NONE : (int) existing.amount();
        if (applied == max) {
            return;
        }
        attribute.removeModifier(RunicShielding.MODIFIER_ID);
        if (max != NONE) {
            attribute.addTransientModifier(new AttributeModifier(RunicShielding.MODIFIER_ID, max, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
