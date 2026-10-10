package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.warp.PlayerWarpState;
import com.leclowndu93150.thaumaturge.content.warp.WarpGear;
import com.leclowndu93150.thaumaturge.content.warp.WarpLedger;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WarpCheck {
    private static final int TEMPORARY_FADE = 1;
    private static final int WEAKEST_OUTCOME = 1;
    private static final int GRINNING_DEVIL_MASK = 0;
    private static final int DEVIL_EASE_MIN = 2;
    private static final int DEVIL_EASE_MAX = 5;

    private WarpCheck() {}

    public static void run(ServerPlayer player) {
        PlayerWarpState state = WarpLedger.state(player);
        state.reduce(WarpType.TEMPORARY, TEMPORARY_FADE);
        WarpReading reading = WarpReading.of(state, WarpGear.worn(player));
        RandomSource random = player.getRandom();
        if (reading.primed() && random.nextDouble() < reading.fireChance()) {
            unleash(player, state, reading, random);
        }
        player.syncData(TTAttachments.WARP);
    }

    private static void unleash(ServerPlayer player, PlayerWarpState state, WarpReading reading, RandomSource random) {
        int effective = reading.effective();
        int strength = random.nextInt(effective) + reading.gear() - devilEase(player, random);
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.heartbeat());
        state.setCounter(reading.counterAfterEvent());
        if (strength >= WEAKEST_OUTCOME) {
            WarpRoll roll = new WarpRoll(player, player.level(), random, effective, reading.normal());
            WarpOutcomeTable.standard().select(strength).ifPresent(outcome -> outcome.apply(roll));
        }
        WarpMilestones.review(player, reading.actual());
    }

    private static int devilEase(ServerPlayer player, RandomSource random) {
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean grinning = helm.getItem() instanceof FortressArmorItem && FortressArmorItem.mask(helm) == GRINNING_DEVIL_MASK;
        return grinning ? Mth.nextInt(random, DEVIL_EASE_MIN, DEVIL_EASE_MAX) : 0;
    }
}
