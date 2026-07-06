package com.leclowndu93150.thaumcraft.content.focus;

import com.leclowndu93150.thaumcraft.api.casters.FocusEffect;
import com.leclowndu93150.thaumcraft.api.casters.FocusPackage;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFocusImpactPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FocusFX {
    private static final double FX_RADIUS = 64.0;

    private FocusFX() {}

    public static void impact(ServerLevel level, Vec3 hit, Identifier partKey) {
        send(level, new ClientboundFocusImpactPayload(hit.x, hit.y, hit.z,
                0.0F, 0.0F, 0.0F, false, List.of(partKey)));
    }

    public static void burst(ServerLevel level, Vec3 source, Vec3 halfDirection, FocusPackage focusPackage) {
        List<FocusEffect> effects = focusPackage.getFocusEffects();
        if (effects.isEmpty()) {
            return;
        }
        List<Identifier> parts = new ArrayList<>(effects.size());
        for (FocusEffect effect : effects) {
            parts.add(effect.getKey());
        }
        Vec3 motion = halfDirection.add(casterVelocity(focusPackage));
        send(level, new ClientboundFocusImpactPayload(source.x, source.y, source.z,
                (float) motion.x, (float) motion.y, (float) motion.z, true, parts));
    }

    private static Vec3 casterVelocity(FocusPackage focusPackage) {
        LivingEntity caster = focusPackage.getCaster();
        return caster == null ? Vec3.ZERO : caster.getKnownMovement();
    }

    private static void send(ServerLevel level, ClientboundFocusImpactPayload payload) {
        PacketDistributor.sendToPlayersNear(level, null, payload.x(), payload.y(), payload.z(), FX_RADIUS, payload);
    }
}
