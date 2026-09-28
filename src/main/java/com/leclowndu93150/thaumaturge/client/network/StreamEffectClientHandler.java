package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoreStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.VoidStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.manager.BeamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.BoreVoidStreamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.EssentiaStreamManager;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class StreamEffectClientHandler {
    private StreamEffectClientHandler() {}

    public static void handle(ClientboundStreamEffectPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static Vec3 boltStart(ClientboundStreamEffectPayload p) {
        LocalPlayer player = Minecraft.getInstance().player;
        Vec3 tip = player != null && p.entityId() == player.getId() ? WandTipTracker.firstPersonTip() : null;
        return tip != null ? tip : new Vec3(p.sx(), p.sy(), p.sz());
    }

    private static void dispatch(ClientboundStreamEffectPayload p) {
        switch (p.kind()) {
            case ARC -> BeamManager.addArc(new ArcInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraFloat()));
            case BOLT -> {
                Vec3 start = boltStart(p);
                BeamManager.addBolt(new BoltInstance(start.x, start.y, start.z, p.tx(), p.ty(), p.tz(), p.color(), p.extraFloat()));
            }
            case BEAM -> BeamManager.addBeam(new BeamInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraInt(), p.extraInt2(), p.extraFloat(),
                    p.hasFlag(ClientboundStreamEffectPayload.FLAG_REVERSE), p.entityId(), p.hasFlag(ClientboundStreamEffectPayload.FLAG_WITH_SOURCE)));
            case ESSENTIA -> EssentiaStreamManager.spawn(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2());
            case BORE -> BoreVoidStreamManager.addBore(new BoreStreamInstance(p.sx(), p.sy(), p.sz(), p.entityId(), p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2()));
            case VOID -> BoreVoidStreamManager.addVoid(new VoidStreamInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.extraInt(), p.extraFloat()));
        }
    }
}
