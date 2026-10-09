package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class RunicShielding {
    public static final Identifier MODIFIER_ID = TTIds.rl("runic_shielding");

    private static final int RESCAN_INTERVAL_TICKS = 20;
    private static final int NO_SHIELD = 0;
    private static final int SHIELD_STEP = 1;
    private static final float FREE = 0.0F;
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    private RunicShielding() {}

    public static int worn(ServerPlayer player) {
        int total = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            total += InfusionRunicAugmentRecipe.charge(player.getItemBySlot(slot));
        }
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            for (ItemStack curio : ThaumaturgeCuriosCompat.equippedCurios(player)) {
                total += InfusionRunicAugmentRecipe.charge(curio);
            }
        }
        return total;
    }

    public static void tick(ServerPlayer player) {
        RunicShieldState state = player.getData(TTAttachments.RUNIC_SHIELD);
        if (player.tickCount % RESCAN_INTERVAL_TICKS == 0) {
            ShieldRescan.run(player, state);
        }
        if (state.maxCharge <= NO_SHIELD) {
            return;
        }
        ServerLevel level = player.level();
        long now = level.getGameTime();
        int shield = (int) player.getAbsorptionAmount();
        ShieldDepletionTracker.observe(state, shield, now);
        BlockPos pos = player.blockPosition();
        float cost = ThaumaturgeCommonConfig.SHIELD_COST.get().floatValue();
        if (ShieldGrantRule.isDue(level, player, pos, state, shield, now, cost)) {
            grant(level, player, state, pos, shield, now, cost);
        }
    }

    private static void grant(ServerLevel level, ServerPlayer player, RunicShieldState state, BlockPos pos, int shield, long now, float cost) {
        int granted = shield + SHIELD_STEP;
        player.setAbsorptionAmount(granted);
        state.lastCharge = granted;
        state.nextCycle = now + ThaumaturgeCommonConfig.SHIELD_RECHARGE.get();
        if (cost > FREE) {
            AuraHelper.drainVis(level, pos, cost, false);
        }
    }
}
