package com.leclowndu93150.thaumcraft.client.fx.network;

import com.leclowndu93150.thaumcraft.client.fx.render.instance.ArcInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.instance.BeamInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.instance.BoltInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.instance.BoreStreamInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.instance.VoidStreamInstance;
import com.leclowndu93150.thaumcraft.client.fx.render.manager.BeamManager;
import com.leclowndu93150.thaumcraft.client.fx.render.manager.BoreVoidStreamManager;
import com.leclowndu93150.thaumcraft.client.fx.render.manager.EssentiaStreamManager;
import com.leclowndu93150.thaumcraft.network.fx.ClientboundFXStreamPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public final class FXStreamClientHandler {
    private FXStreamClientHandler() {}

    public static void handle(ClientboundFXStreamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundFXStreamPayload p) {
        switch (p.kind()) {
            case ARC -> BeamManager.addArc(new ArcInstance(
                    p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(),
                    p.color(), p.extraFloat()));
            case BOLT -> BeamManager.addBolt(new BoltInstance(
                    p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(),
                    p.color(), p.extraFloat()));
            case BEAM -> BeamManager.addBeam(new BeamInstance(
                    p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(),
                    p.color(), p.extraInt(), p.extraInt2(),
                    p.extraFloat(),
                    p.hasFlag(ClientboundFXStreamPayload.FLAG_REVERSE),
                    p.entityId(),
                    p.hasFlag(ClientboundFXStreamPayload.FLAG_WITH_SOURCE)));
            case ESSENTIA -> EssentiaStreamManager.spawn(
                    p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(),
                    p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2());
            case BORE -> BoreVoidStreamManager.addBore(new BoreStreamInstance(
                    p.sx(), p.sy(), p.sz(), p.entityId(),
                    p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2()));
            case VOID -> BoreVoidStreamManager.addVoid(new VoidStreamInstance(
                    p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(),
                    p.extraInt(), p.extraFloat()));
        }
    }
}
