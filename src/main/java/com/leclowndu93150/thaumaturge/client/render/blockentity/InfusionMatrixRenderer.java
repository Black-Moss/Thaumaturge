package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.MatrixCubeModel;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class InfusionMatrixRenderer implements BlockEntityRenderer<BlockEntityInfusionMatrix, InfusionMatrixRenderState> {
    private static final Identifier TEXTURE_NORMAL = TTIds.rl("textures/block/infuser_normal.png");
    private static final Identifier TEXTURE_ANCIENT = TTIds.rl("textures/block/infuser_ancient.png");
    private static final Identifier TEXTURE_ELDRITCH = TTIds.rl("textures/block/infuser_eldritch.png");
    private static final int PROBE_X = -1;
    private static final int PROBE_Y = -2;
    private static final int PROBE_Z = -1;
    private static final float CENTER = 0.5F;
    private static final float HOVER_BASE = 0.3F;
    private static final float CLUSTER_SCALE = 0.8F;
    private static final float SATELLITE_SCALE = 0.5F;
    private static final float CORE_SCALE = 0.75F;
    private static final float SATELLITE_OFFSET = 0.3125F;
    private static final float QUARTER_TURN = 90.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float TILT = 45.0F;
    private static final float PRECESSION_DIVISOR = 4.0F;
    private static final float IDLE_WAVE_PERIOD = 40.0F;
    private static final float CRAFT_WAVE_PERIOD = 25.0F;
    private static final float IDLE_BOB_DIVISOR = 4.5F;
    private static final float CRAFT_BOB_DIVISOR = 3.5F;
    private static final float BOB_BLEND_TICKS = 15.0F;
    private static final float INSTABILITY_RAMP_TICKS = 50.0F;
    private static final float INSTABILITY_CAP = 6.0F;
    private static final float UNSTABLE_GAIN = 0.66F;
    private static final float JITTER_SCALE = 0.01F;
    private static final float JITTER_PERIOD_X = 15.0F;
    private static final float JITTER_PERIOD_Y = 14.0F;
    private static final float JITTER_PERIOD_Z = 13.0F;
    private static final float JITTER_PHASE = 10.0F;
    private static final float GLOW_RED = 0.8F;
    private static final float GLOW_GREEN = 0.1F;
    private static final float GLOW_BLUE = 1.0F;
    private static final float GLOW_AMPLITUDE = 0.1F;
    private static final float GLOW_BASE = 0.2F;
    private static final float GLOW_PERIOD = 4.0F;
    private static final int SATELLITE_COUNT = 8;
    private static final int GLOW_PHASE_X = 2;
    private static final int GLOW_PHASE_Y = 3;
    private static final int GLOW_PHASE_Z = 4;
    private static final int FANCY_FANS = 20;
    private static final int PLAIN_FANS = 10;
    private static final float HALO_DURATION = 500.0F;
    private static final float HALO_RAMP_TICKS = 50.0F;
    private static final float FAN_LENGTH_BASE = 5.0F;
    private static final float FAN_LENGTH_RANGE = 20.0F;
    private static final float FAN_LENGTH_DIVISOR = 20.0F;
    private static final float FAN_WIDTH_BASE = 1.0F;
    private static final float FAN_WIDTH_RANGE = 2.0F;
    private static final float FAN_WIDTH_DIVISOR = 20.0F;
    private static final float FAN_HALF_SPREAD = 0.866F;
    private static final float FAN_DEPTH_SPREAD = 0.5F;
    private static final int SALT_X = 0;
    private static final int SALT_Y = 1;
    private static final int SALT_Z = 2;
    private static final int SALT_LENGTH = 3;
    private static final int SALT_WIDTH = 4;
    private static final int SALT_STRIDE = 8;
    private static final float HASH_RANGE = 1 << 24;
    private static final int HASH_SHIFT = 8;

    private final MatrixCubeModel model;

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MatrixCubeModel(context.bakeLayer(TTModelLayers.MATRIX_CUBE));
    }

    @Override
    public InfusionMatrixRenderState createRenderState() {
        return new InfusionMatrixRenderState();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public void extractRenderState(BlockEntityInfusionMatrix matrix, InfusionMatrixRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(matrix, state, partialTicks, cameraPosition, breakProgress);
        Minecraft minecraft = Minecraft.getInstance();
        state.texture = textureFor(matrix.getLevel(), matrix);
        state.fancyGraphics = minecraft.options.cutoutLeaves().get();
        state.crafting = matrix.isCrafting();
        state.active = matrix.isActive();
        state.craftTicks = matrix.clientCraftTicks;
        state.stability = matrix.stability();
        state.startUp = matrix.clientStartUp;
        state.animationTime = cameraAge(minecraft.getCameraEntity()) + partialTicks;
    }

    private static int cameraAge(@Nullable Entity camera) {
        return camera != null ? camera.tickCount : 0;
    }

    private static Identifier textureFor(@Nullable Level level, BlockEntityInfusionMatrix matrix) {
        if (level == null) {
            return TEXTURE_NORMAL;
        }
        Block below = level.getBlockState(matrix.getBlockPos().offset(PROBE_X, PROBE_Y, PROBE_Z)).getBlock();
        if (below == TTBlocks.PILLAR_ANCIENT.get()) {
            return TEXTURE_ANCIENT;
        }
        return below == TTBlocks.PILLAR_ELDRITCH.get() ? TEXTURE_ELDRITCH : TEXTURE_NORMAL;
    }

    @Override
    public void submit(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float time = state.animationTime;
        float startUp = state.startUp;
        float jitter = state.active ? JITTER_SCALE * startUp * instability(state) : 0.0F;
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER + (HOVER_BASE + bob(time, state.craftTicks)) * startUp, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees((time % FULL_TURN) * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees((time / PRECESSION_DIVISOR % FULL_TURN) * startUp));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT * startUp));
        poseStack.scale(CLUSTER_SCALE, CLUSTER_SCALE, CLUSTER_SCALE);
        submitCube(state, poseStack, collector, 0.0F, 0.0F, 0.0F, 0, 0, 0, CORE_SCALE, 0);
        for (int corner = 0; corner < SATELLITE_COUNT; corner++) {
            submitSatellite(state, poseStack, collector, time, jitter, (corner >> 2) & 1, (corner >> 1) & 1, corner & 1);
        }
        poseStack.popPose();
        if (state.crafting) {
            submitHalo(state, poseStack, collector);
        }
    }

    private static float instability(InfusionMatrixRenderState state) {
        float gain = state.stability < 0.0F ? -state.stability * UNSTABLE_GAIN : 1.0F;
        float scaled = gain * Math.min(state.craftTicks, INSTABILITY_RAMP_TICKS) / INSTABILITY_RAMP_TICKS;
        return Math.min(INSTABILITY_CAP, 1.0F + scaled);
    }

    private static float bob(float time, int craftTicks) {
        float idle = cycle(time, IDLE_WAVE_PERIOD) / IDLE_BOB_DIVISOR;
        float crafting = cycle(time, CRAFT_WAVE_PERIOD) / CRAFT_BOB_DIVISOR;
        return Mth.lerp(Math.min(craftTicks, BOB_BLEND_TICKS) / BOB_BLEND_TICKS, idle, crafting);
    }

    private static float cycle(float time, float period) {
        return Mth.sin(Mth.TWO_PI * (time % period) / period);
    }

    private void submitSatellite(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float time, float jitter, int a, int b, int c) {
        float x = drift(a, time, jitter, JITTER_PERIOD_X);
        float y = drift(b, time, jitter, JITTER_PERIOD_Y);
        float z = drift(c, time, jitter, JITTER_PERIOD_Z);
        int glowPhase = GLOW_PHASE_X * a + GLOW_PHASE_Y * b + GLOW_PHASE_Z * c;
        submitCube(state, poseStack, collector, x, y, z, a, b, c, SATELLITE_SCALE, glowPhase);
    }

    private static float drift(int flag, float time, float jitter, float period) {
        return offset(flag) + jitter * Mth.sin((time + JITTER_PHASE * flag) / period);
    }

    private static float offset(int flag) {
        return flag == 1 ? SATELLITE_OFFSET : -SATELLITE_OFFSET;
    }

    private void submitCube(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float x, float y, float z, int a, int b, int c, float scale, int glowPhase) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(QUARTER_TURN * a));
        poseStack.mulPose(Axis.YP.rotationDegrees(QUARTER_TURN * b));
        poseStack.mulPose(Axis.ZP.rotationDegrees(QUARTER_TURN * c));
        poseStack.scale(scale, scale, scale);
        collector.submitModelPart(model.cube, poseStack, RenderTypes.entityCutout(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        if (state.active) {
            float alpha = (GLOW_AMPLITUDE * Mth.sin((state.animationTime + glowPhase) / GLOW_PERIOD) + GLOW_BASE) * state.startUp;
            int tint = ARGB.colorFromFloat(alpha, GLOW_RED, GLOW_GREEN, GLOW_BLUE);
            collector.submitModelPart(model.glow, poseStack, TTFXRenderTypes.entityAdditiveUnlit(state.texture), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, tint, null);
        }
        poseStack.popPose();
    }

    private void submitHalo(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float progress = state.craftTicks / HALO_DURATION;
        float ramp = Math.min(state.craftTicks, HALO_RAMP_TICKS) / HALO_RAMP_TICKS;
        float centerAlpha = Math.max(0.0F, 1.0F - progress);
        int fanCount = state.fancyGraphics ? FANCY_FANS : PLAIN_FANS;
        for (int fan = 0; fan < fanCount; fan++) {
            float length = fanExtent(fan, SALT_LENGTH, FAN_LENGTH_RANGE, FAN_LENGTH_BASE, FAN_LENGTH_DIVISOR) * ramp;
            float width = fanExtent(fan, SALT_WIDTH, FAN_WIDTH_RANGE, FAN_WIDTH_BASE, FAN_WIDTH_DIVISOR) * ramp;
            poseStack.pushPose();
            poseStack.translate(CENTER, CENTER, CENTER);
            poseStack.mulPose(orientation(fan));
            poseStack.mulPose(Axis.ZP.rotationDegrees(progress * FULL_TURN));
            collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, new FanGeometry(length, width, centerAlpha));
            poseStack.popPose();
        }
    }

    private static float fanExtent(int fan, int salt, float range, float base, float divisor) {
        return (unit(fan, salt) * range + base) / divisor;
    }

    private record FanGeometry(float length, float width, float centerAlpha) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            float side = FAN_HALF_SPREAD * width;
            float depth = FAN_DEPTH_SPREAD * width;
            float[] rimX = {-side, side, 0.0F};
            float[] rimZ = {-depth, -depth, width};
            for (int corner = 0; corner < rimX.length; corner++) {
                int next = (corner + 1) % rimX.length;
                buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, centerAlpha);
                buffer.addVertex(pose, rimX[corner], length, rimZ[corner]).setColor(1.0F, 0.0F, 1.0F, 0.0F);
                buffer.addVertex(pose, rimX[next], length, rimZ[next]).setColor(1.0F, 0.0F, 1.0F, 0.0F);
            }
        }
    }

    private static Quaternionf orientation(int fan) {
        float first = unit(fan, SALT_X);
        float angleY = Mth.TWO_PI * unit(fan, SALT_Y);
        float angleZ = Mth.TWO_PI * unit(fan, SALT_Z);
        float outer = Mth.sqrt(1.0F - first);
        float inner = Mth.sqrt(first);
        return new Quaternionf(outer * Mth.sin(angleY), outer * Mth.cos(angleY), inner * Mth.sin(angleZ), inner * Mth.cos(angleZ));
    }

    private static float unit(int index, int salt) {
        return (Mth.murmurHash3Mixer(Mth.murmurHash3Mixer(index * SALT_STRIDE + salt)) >>> HASH_SHIFT) / HASH_RANGE;
    }
}
