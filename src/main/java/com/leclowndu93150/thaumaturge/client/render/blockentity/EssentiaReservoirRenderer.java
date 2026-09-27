package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaReservoirRenderer implements BlockEntityRenderer<BlockEntityEssentiaReservoir, EssentiaReservoirRenderState> {
    private static final int FLUID_ALPHA = 0xE6;
    private static final float CYCLE_TICKS = 20.0F;
    private static final float FLUID_LIFT = 2.0F / 16.0F;
    private static final float FLUID_WIDEN = 1.25F;
    private static final int MIN_BLOCK_LIGHT = 12;

    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public EssentiaReservoirRenderState createRenderState() {
        return new EssentiaReservoirRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEssentiaReservoir reservoir, EssentiaReservoirRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(reservoir, state, partialTicks, cameraPosition, breakProgress);
        state.amount = reservoir.getStoredAmount();
        List<AspectInstance> entries = reservoir.contents().entries();
        if (entries.isEmpty() || reservoir.getLevel() == null) {
            state.color = -1;
            return;
        }
        float cycle = (reservoir.getLevel().getGameTime() + partialTicks) / CYCLE_TICKS;
        int index = Math.floorMod(Mth.floor(cycle), entries.size());
        int from = entries.get(Math.floorMod(index - 1, entries.size())).aspect().value().color();
        int to = entries.get(index).aspect().value().color();
        state.color = ARGB.color(FLUID_ALPHA, ARGB.srgbLerp(cycle - Mth.floor(cycle), from, to));
    }

    @Override
    public void submit(EssentiaReservoirRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.amount <= 0) {
            return;
        }
        int light = LightCoordsUtil.pack(Math.max(LightCoordsUtil.block(state.lightCoords), MIN_BLOCK_LIGHT), LightCoordsUtil.sky(state.lightCoords));
        poseStack.pushPose();
        poseStack.translate(0.5F, FLUID_LIFT, 0.5F);
        poseStack.scale(FLUID_WIDEN, 1.0F, FLUID_WIDEN);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        JarRenderer.submitFluid(state.amount, BlockEntityEssentiaReservoir.CAPACITY, state.color, light, Minecraft.getInstance().getAtlasManager().get(JarRenderer.ANIMATED_GLOW_SPRITE), poseStack,
                collector);
        poseStack.popPose();
    }
}
