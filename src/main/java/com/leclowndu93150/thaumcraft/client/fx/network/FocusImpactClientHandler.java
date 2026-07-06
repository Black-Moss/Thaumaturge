package com.leclowndu93150.thaumcraft.client.fx.network;

import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.FocusEngine;
import com.leclowndu93150.thaumcraft.api.casters.IFocusElement;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFocusImpactPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FocusImpactClientHandler {
    private static final int IMPACT_PARTICLE_BUDGET = 15;
    private static final int BURST_PARTICLE_BUDGET = 10;
    private static final double IMPACT_SPREAD = 0.15;
    private static final double BURST_JITTER_DIVISOR = 20.0;

    private FocusImpactClientHandler() {}

    public static void handle(ClientboundFocusImpactPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundFocusImpactPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || payload.parts().isEmpty()) {
            return;
        }
        int budget = payload.burst() ? BURST_PARTICLE_BUDGET : IMPACT_PARTICLE_BUDGET;
        int amount = Math.max(1, budget / payload.parts().size());
        RandomSource rand = level.getRandom();
        for (Identifier key : payload.parts()) {
            IFocusElement element = FocusEngine.getElement(key);
            if (!(element instanceof FocusEffect effect)) {
                continue;
            }
            for (int a = 0; a < amount; a++) {
                if (payload.burst()) {
                    effect.renderParticleFX(level, payload.x(), payload.y(), payload.z(),
                            payload.mx() + rand.nextGaussian() / BURST_JITTER_DIVISOR,
                            payload.my() + rand.nextGaussian() / BURST_JITTER_DIVISOR,
                            payload.mz() + rand.nextGaussian() / BURST_JITTER_DIVISOR);
                } else {
                    effect.renderParticleFX(level, payload.x(), payload.y(), payload.z(),
                            rand.nextGaussian() * IMPACT_SPREAD,
                            rand.nextGaussian() * IMPACT_SPREAD,
                            rand.nextGaussian() * IMPACT_SPREAD);
                }
            }
        }
    }
}
