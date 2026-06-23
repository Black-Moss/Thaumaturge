package com.leclowndu93150.thaumcraft.client.fx.render.texture;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.fx.render.pipeline.TCRenderPipelines;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.resources.Identifier;

public final class TCParticleLayer {
    public static final Identifier SHEET = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/particle/fx_generic.png");
    public static final Identifier NITOR_CORE_SHEET = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/particle/nitor_core.png");

    public static final SingleQuadParticle.Layer ADDITIVE = new SingleQuadParticle.Layer(
            true, SHEET, TCRenderPipelines.FX_ADDITIVE);

    public static final SingleQuadParticle.Layer TRANSLUCENT = new SingleQuadParticle.Layer(
            false, SHEET, TCRenderPipelines.FX_TRANSLUCENT);

    public static final SingleQuadParticle.Layer ADDITIVE_NO_DEPTH = new SingleQuadParticle.Layer(
            true, SHEET, TCRenderPipelines.FX_ADDITIVE_NO_DEPTH);

    public static final SingleQuadParticle.Layer TRANSLUCENT_NO_DEPTH = new SingleQuadParticle.Layer(
            false, SHEET, TCRenderPipelines.FX_TRANSLUCENT_NO_DEPTH);

    public static final SingleQuadParticle.Layer NITOR_CORE = new SingleQuadParticle.Layer(
            false, NITOR_CORE_SHEET, TCRenderPipelines.FX_TRANSLUCENT);

    public static SingleQuadParticle.Layer byLayer(int layer) {
        return switch (layer) {
            case 1 -> TRANSLUCENT;
            case 2 -> ADDITIVE_NO_DEPTH;
            case 3 -> TRANSLUCENT_NO_DEPTH;
            default -> ADDITIVE;
        };
    }

    private TCParticleLayer() {}
}
