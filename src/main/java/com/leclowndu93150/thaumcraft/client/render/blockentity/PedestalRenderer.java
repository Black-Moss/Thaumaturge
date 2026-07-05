package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.content.infusion.BlockEntityPedestal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PedestalRenderer implements BlockEntityRenderer<BlockEntityPedestal, PedestalRenderState> {
    private static final float ITEM_SCALE = 1.25F;
    private static final float ITEM_HEIGHT = 0.75F;
    private static final float SPIN_DEGREES_PER_TICK = 1.0F;
    private static final float HOVER_LIFT = 0.1F;
    private static final float GROUND_SCALE_LIFT_FACTOR = 0.25F;

    private final ItemModelResolver itemModelResolver;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityPedestal pedestal, PedestalRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = pedestal.getItem();
        if (stack.isEmpty()) {
            state.item = null;
            return;
        }
        ItemStackRenderState itemState = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(itemState, stack, ItemDisplayContext.GROUND, pedestal.getLevel(),
                null, HashCommon.long2int(pedestal.getBlockPos().asLong()));
        state.item = itemState;
        state.groundLift = HOVER_LIFT + GROUND_SCALE_LIFT_FACTOR * (float) itemState.getModelBoundingBox().getYsize();
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float ticks = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        state.spin = ticks % 360.0F * SPIN_DEGREES_PER_TICK;
    }

    @Override
    public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_HEIGHT, 0.5F);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
        poseStack.translate(0.0F, state.groundLift, 0.0F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
