package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public final class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater, CultistPortalRenderer.State> {
    private static final float SHADOW_RADIUS = 0.1F;
    private static final float HALF_HEIGHT = 1.5F;
    private static final float WIDTH_FACTOR = 1.3F;

    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public CultistPortalRenderer.State createRenderState() {
        return new CultistPortalRenderer.State();
    }

    @Override
    public void extractRenderState(EntityCultistPortalGreater entity, CultistPortalRenderer.State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        CultistPortalRenderer.capture(state, entity, true, entity.tickCount + partialTicks, entity.pulse);
    }

    @Override
    public void submit(CultistPortalRenderer.State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        CultistPortalRenderer.submitPortal(state, poseStack, collector, camera, HALF_HEIGHT, WIDTH_FACTOR);
    }
}
