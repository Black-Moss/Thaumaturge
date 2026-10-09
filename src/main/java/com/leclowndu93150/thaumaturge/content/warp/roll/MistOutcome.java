package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.content.warp.WarpNotices;
import com.leclowndu93150.thaumaturge.content.warp.spawn.GuardianSummons;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import java.util.function.ToIntFunction;
import net.neoforged.neoforge.network.PacketDistributor;

public record MistOutcome(ToIntFunction<WarpRoll> guardians) implements WarpOutcome {
    private static final int MAX_GUARDIANS = 8;
    private static final String NOTICE = "warp.thaumaturge.text.6";

    @Override
    public void apply(WarpRoll roll) {
        PacketDistributor.sendToPlayer(roll.player(), ClientboundWarpFXPayload.mist());
        int count = Math.min(MAX_GUARDIANS, guardians.applyAsInt(roll));
        for (int i = 0; i < count; i++) {
            GuardianSummons.summon(roll.level(), roll.player(), roll.random());
        }
        WarpNotices.send(roll.player(), NOTICE);
    }
}
