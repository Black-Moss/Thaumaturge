package com.leclowndu93150.thaumaturge.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

public final class SpecialItemRenderer extends ItemEntityRenderer {
    private static final int FANCY_RAYS = 10;
    private static final int FAST_RAYS = 5;
    private static final float REACH = 0.95F;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 500.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float RAY_LIFT = 0.3F;
    private static final float ITEM_BOB_RATE_DIVISOR = 10.0F;
    private static final float ITEM_BOB_HEIGHT = 0.1F;
    private static final int RAY_CORE = ARGB.color(225, 255, 248, 255);
    private static final int RAY_TIP = ARGB.color(0, 225, 110, 235);

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ItemEntityRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof State special) {
            special.fast = Minecraft.getInstance().options.graphicsPreset().get() == GraphicsPreset.FAST;
        }
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.item.isEmpty()) {
            return;
        }
        float age = state.ageInTicks;
        float bob = state.shouldBob ? Mth.sin(age / ITEM_BOB_RATE_DIVISOR + state.bobOffset) * ITEM_BOB_HEIGHT + ITEM_BOB_HEIGHT : 0.0F;
        int rays = state instanceof State special && special.fast ? FAST_RAYS : FANCY_RAYS;
        float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
        poseStack.pushPose();
        poseStack.translate(0.0F, bob + RAY_LIFT, 0.0F);
        EldritchOrbRenderer.submitBurst(poseStack, collector, rays, REACH * grow, age / SPIN_TICKS_PER_TURN * FULL_TURN, RAY_CORE, RAY_TIP);
        poseStack.popPose();
    }

    public static final class State extends ItemEntityRenderState {
        public boolean fast;
    }
}
