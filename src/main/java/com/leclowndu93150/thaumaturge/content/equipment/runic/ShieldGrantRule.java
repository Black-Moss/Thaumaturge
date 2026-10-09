package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ShieldGrantRule {
    private ShieldGrantRule() {}

    public static boolean isDue(ServerLevel level, ServerPlayer player, BlockPos pos, RunicShieldState state, int shield, long now, float cost) {
        return shield < state.maxCharge && now >= state.nextCycle && !AuraHelper.shouldPreserveAura(level, player, pos) && AuraHelper.getVis(level, pos) >= cost;
    }
}
