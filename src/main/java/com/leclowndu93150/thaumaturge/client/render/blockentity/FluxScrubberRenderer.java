package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.device.fluxscrubber.BlockEntityFluxScrubber;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FluxScrubberRenderer implements BlockEntityRenderer<BlockEntityFluxScrubber, FluxScrubberRenderState> {
    private static final Identifier MESH = TTIds.rl("models/mesh/flux_scrubber.ttmesh");
    private static final Identifier TEXTURE = TTIds.rl("textures/block/flux_scrubber.png");
    private static final String TIP_PART = "Tip";
    private static final float BOB_PERIOD = 8.0F;
    private static final float BOB_AMPLITUDE = 0.075F;
    private static final long PHASE_RANGE = 1000L;
    private static final int WHITE = 0xFFFFFFFF;

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public FluxScrubberRenderState createRenderState() {
        return new FluxScrubberRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityFluxScrubber scrubber, FluxScrubberRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(scrubber, state, partialTicks, cameraPosition, breakProgress);
        state.facing = scrubber.getBlockState().getValue(BlockStateProperties.FACING);
        Level level = scrubber.getLevel();
        float time = (level == null ? 0L : level.getGameTime()) + partialTicks + Math.floorMod(scrubber.getBlockPos().asLong(), PHASE_RANGE);
        state.bob = BOB_AMPLITUDE * Mth.sin(time / BOB_PERIOD) + BOB_AMPLITUDE;
    }

    @Override
    public void submit(FluxScrubberRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        LegacyFacingPose.apply(poseStack, state.facing);
        poseStack.translate(0.0F, 0.0F, -state.bob);
        for (TTMeshPart part : GolemMeshes.get(MESH).parts()) {
            if (TIP_PART.equals(part.name())) {
                collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE));
            }
        }
        poseStack.popPose();
    }
}
