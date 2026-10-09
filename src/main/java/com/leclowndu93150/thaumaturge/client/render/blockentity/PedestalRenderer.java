package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PedestalRenderer<T extends BlockEntityPedestal> implements BlockEntityRenderer<T, PedestalRenderState> {
    private static final float DEFAULT_ITEM_SCALE = 1.25F;
    private static final float DEFAULT_FLOAT_HEIGHT = 0.0F;
    private static final float REST_HEIGHT = 1.0F;
    private static final float TALL_PEDESTAL_REST_HEIGHT = 0.75F;
    private static final float GROUND_CLEARANCE = 1.0F / 16.0F;
    private static final float CENTER = 0.5F;
    private static final float FULL_TURN = 360.0F;

    private final ItemModelResolver itemModelResolver;
    private final float itemScale;
    private final float floatHeight;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this(context, DEFAULT_ITEM_SCALE, DEFAULT_FLOAT_HEIGHT);
    }

    public PedestalRenderer(BlockEntityRendererProvider.Context context, float itemScale, float floatHeight) {
        this.itemModelResolver = context.itemModelResolver();
        this.itemScale = itemScale;
        this.floatHeight = floatHeight;
    }

    @Override
    public PedestalRenderState createRenderState() {
        return new PedestalRenderState();
    }

    @Override
    public void extractRenderState(T pedestal, PedestalRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pedestal, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = pedestal.getItem();
        if (stack.isEmpty()) {
            state.item = null;
            return;
        }
        ItemStackRenderState item = state.item == null ? new ItemStackRenderState() : state.item;
        itemModelResolver.updateForTopItem(item, stack, ItemDisplayContext.GROUND, pedestal.getLevel(), null, Long.hashCode(pedestal.getBlockPos().asLong()));
        state.item = item;
        BlockState block = pedestal.getBlockState();
        boolean tall = block.is(TTBlocks.PEDESTAL_ANCIENT) || block.is(TTBlocks.PEDESTAL_ELDRITCH);
        state.height = (tall ? TALL_PEDESTAL_REST_HEIGHT : REST_HEIGHT) + floatHeight;
        Entity camera = Minecraft.getInstance().getCameraEntity();
        state.spin = ((camera == null ? 0 : camera.tickCount) + partialTicks) % FULL_TURN;
        state.groundLift = LegacyItemLift.bottomLift(item) + GROUND_CLEARANCE;
    }

    @Override
    public void submit(PedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(CENTER, state.height, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
        poseStack.scale(itemScale, itemScale, itemScale);
        poseStack.translate(0.0F, state.groundLift, 0.0F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
