package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import net.minecraft.client.particle.SingleQuadParticle;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;

public final class TCParticleLayers {
    public static final SingleQuadParticle.Layer LEGACY_TRANSLUCENT = new SingleQuadParticle.Layer(true, ParticleTextures.PARTICLES, TCRenderPipelines.FX_TRANSLUCENT);
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
