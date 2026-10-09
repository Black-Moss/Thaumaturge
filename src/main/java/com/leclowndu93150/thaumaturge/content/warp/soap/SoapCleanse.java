package com.leclowndu93150.thaumaturge.content.warp.soap;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.PlayerWarpState;
import com.leclowndu93150.thaumaturge.content.warp.WarpLedger;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public final class SoapCleanse {
    private static final int BASE_NORMAL_CLEANSE = 1;
    private static final int WARD_BONUS = 1;
    private static final int PURIFYING_FLUID_BONUS = 1;

    public static final SoapCleanse SANITY_SOAP = new SoapCleanse(BASE_NORMAL_CLEANSE,
            List.of(new EffectCleanseBonus(TTMobEffects.WARP_WARD, WARD_BONUS), new StandingInCleanseBonus(TTBlocks.PURIFYING_FLUID, PURIFYING_FLUID_BONUS)));

    private final int baseNormal;
    private final List<CleanseBonus> bonuses;

    public SoapCleanse(int baseNormal, List<CleanseBonus> bonuses) {
        this.baseNormal = baseNormal;
        this.bonuses = List.copyOf(bonuses);
    }

    public int normalAmount(ServerPlayer player) {
        int amount = baseNormal;
        for (CleanseBonus bonus : bonuses) {
            amount += bonus.bonus(player);
        }
        return amount;
    }

    public void apply(ServerPlayer player) {
        PlayerWarpState state = WarpLedger.state(player);
        if (state.get(WarpType.NORMAL) > 0) {
            WarpLedger.change(player, -normalAmount(player), WarpType.NORMAL);
        }
        int temporary = state.get(WarpType.TEMPORARY);
        if (temporary > 0) {
            WarpLedger.change(player, -temporary, WarpType.TEMPORARY);
        }
    }
}
