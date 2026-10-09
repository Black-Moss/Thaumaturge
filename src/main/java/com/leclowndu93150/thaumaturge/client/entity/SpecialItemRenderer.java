package com.leclowndu93150.thaumaturge.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class SpecialItemRenderer extends ItemEntityRenderer {
    private static final int FANCY_CONE_COUNT = 10;
    private static final int FAST_CONE_COUNT = 5;
    private static final float BOB_RATE_DIVISOR = 10.0F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float CLUSTER_LIFT = 0.25F;
    private static final float SPIN_PERIOD_TICKS = 500.0F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final float GROW_TICKS = 10.0F;
    private static final int CONE_BASE_COLOR = ARGB.colorFromFloat(0.0F, 1.0F, 0.0F, 1.0F);

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.item.isEmpty()) {
            int count = Minecraft.getInstance().options.cutoutLeaves().get() ? FANCY_CONE_COUNT : FAST_CONE_COUNT;
            float bob = Mth.sin(state.ageInTicks / BOB_RATE_DIVISOR + state.bobOffset) * BOB_AMPLITUDE + BOB_AMPLITUDE;
            float grow = Math.min(state.ageInTicks, GROW_TICKS) / GROW_TICKS;
            float spin = state.ageInTicks / SPIN_PERIOD_TICKS * FULL_TURN_DEGREES;
            poseStack.pushPose();
            poseStack.translate(0.0F, bob + CLUSTER_LIFT, 0.0F);
            EldritchOrbRenderer.submitBurst(poseStack, collector, count, grow, spin, CONE_BASE_COLOR);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }
}
