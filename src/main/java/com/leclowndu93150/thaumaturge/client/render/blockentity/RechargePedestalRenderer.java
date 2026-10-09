package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.content.aura.BlockEntityRechargePedestal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class RechargePedestalRenderer implements BlockEntityRenderer<BlockEntityRechargePedestal, RechargePedestalRenderState> {
    private static final double TOP_CENTER_X = 0.5;
    private static final double TOP_CENTER_Y = 1.0;
    private static final double TOP_CENTER_Z = 0.5;
    private static final double NODE_CENTER = 0.5;
    private static final float LINE_SPEED = -0.02F;
    private static final float LINE_FRACTION = 1.0F;
    private static final float LINE_HALF_WIDTH = 0.15F;

    private final PedestalRenderer<BlockEntityRechargePedestal> pedestal;

    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context, float itemScale, float floatHeight) {
        this.pedestal = new PedestalRenderer<>(context, itemScale, floatHeight);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityRechargePedestal recharge) {
        AABB own = new AABB(recharge.getBlockPos());
        BlockPos drain = recharge.getDrainPos();
        return drain == null ? own : own.minmax(new AABB(drain));
    }

    @Override
    public RechargePedestalRenderState createRenderState() {
        return new RechargePedestalRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityRechargePedestal recharge, RechargePedestalRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        pedestal.extractRenderState(recharge, state, partialTicks, cameraPosition, breakProgress);
        BlockPos drain = recharge.getDrainPos();
        Level level = recharge.getLevel();
        state.draining = drain != null && level != null;
        if (!state.draining) {
            return;
        }
        state.drainFromX = drain.getX() + NODE_CENTER;
        state.drainFromY = drain.getY() + NODE_CENTER;
        state.drainFromZ = drain.getZ() + NODE_CENTER;
        state.drainColor = recharge.getDrainColor();
        state.time = level.getGameTime();
        state.partial = partialTicks;
    }

    @Override
    public void submit(RechargePedestalRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        pedestal.submit(state, poseStack, collector, camera);
        if (!state.draining) {
            return;
        }
        BlockPos pos = state.blockPos;
        Vec3 origin = new Vec3(pos.getX() + TOP_CENTER_X, pos.getY() + TOP_CENTER_Y, pos.getZ() + TOP_CENTER_Z);
        Vec3 offset = new Vec3(state.drainFromX, state.drainFromY, state.drainFromZ).subtract(origin);
        float time = FloatyLineRenderer.time(state.time, state.partial);
        int color = state.drainColor;
        LateWorldRenderQueue.enqueue(origin, (late, buffers) -> FloatyLineRenderer.draw(late, buffers, offset, time, color, LINE_SPEED, LINE_FRACTION, LINE_HALF_WIDTH));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
