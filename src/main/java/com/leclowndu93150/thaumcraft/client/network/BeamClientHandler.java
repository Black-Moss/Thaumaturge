package com.leclowndu93150.thaumcraft.client.network;

import com.leclowndu93150.thaumcraft.client.render.beam.ArcInstance;
import com.leclowndu93150.thaumcraft.client.render.beam.BeamInstance;
import com.leclowndu93150.thaumcraft.client.render.beam.BeamManager;
import com.leclowndu93150.thaumcraft.client.render.beam.BoltInstance;
import com.leclowndu93150.thaumcraft.network.ClientboundArcPayload;
import com.leclowndu93150.thaumcraft.network.ClientboundBeamPayload;
import com.leclowndu93150.thaumcraft.network.ClientboundBoltPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class BeamClientHandler {
    private BeamClientHandler() {}

    public static void handleArc(ClientboundArcPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BeamManager.addArc(new ArcInstance(
                payload.sx(), payload.sy(), payload.sz(),
                payload.tx(), payload.ty(), payload.tz(),
                payload.color(), payload.gravity())));
    }

    public static void handleBolt(ClientboundBoltPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BeamManager.addBolt(new BoltInstance(
                payload.sx(), payload.sy(), payload.sz(),
                payload.tx(), payload.ty(), payload.tz(),
                payload.color(), payload.width())));
    }

    public static void handleBeam(ClientboundBeamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> BeamManager.addBeam(new BeamInstance(
                payload.sx(), payload.sy(), payload.sz(),
                payload.tx(), payload.ty(), payload.tz(),
                payload.color(), payload.age(), payload.beamType(),
                payload.endMod(), payload.reverse(),
                payload.sourceEntityId(), payload.withSource())));
    }
}
