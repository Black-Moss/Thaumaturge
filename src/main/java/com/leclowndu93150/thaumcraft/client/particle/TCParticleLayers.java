package com.leclowndu93150.thaumcraft.client.particle;

import com.leclowndu93150.thaumcraft.client.effect.pipeline.TCRenderPipelines;
import net.minecraft.client.particle.SingleQuadParticle;

public final class TCParticleLayers {
    private TCParticleLayers() {}

    public static SingleQuadParticle.Layer additive(ParticleSheet sheet) {
        return sheet.layer(true, TCRenderPipelines.FX_ADDITIVE);
    }

    public static SingleQuadParticle.Layer translucent(ParticleSheet sheet) {
        return sheet.layer(false, TCRenderPipelines.FX_TRANSLUCENT);
    }

    public static SingleQuadParticle.Layer additiveNoDepth(ParticleSheet sheet) {
        return sheet.layer(true, TCRenderPipelines.FX_ADDITIVE_NO_DEPTH);
    }

    public static SingleQuadParticle.Layer translucentNoDepth(ParticleSheet sheet) {
        return sheet.layer(false, TCRenderPipelines.FX_TRANSLUCENT_NO_DEPTH);
    }
}
