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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
    private static final int SATELLITE_COUNT = 8;

    private static final float WOBBLE_SCALE = 0.01F;
    private static final float INSTABILITY_BASE = 1.0F;
    private static final float UNSTABLE_GAIN = 0.66F;
    private static final float INSTABILITY_CAP = 6.0F;
    private static final float INSTABILITY_RAMP_TICKS = 50.0F;
    private static final float WOBBLE_PHASE_STEP = 10.0F;
    private static final float WOBBLE_DIVISOR_X = 15.0F;
    private static final float WOBBLE_DIVISOR_Y = 14.0F;
    private static final float WOBBLE_DIVISOR_Z = 13.0F;

    private static final int GLOW_RGB = 0xCC19FF;
    private static final float GLOW_BASE = 0.2F;
    private static final float GLOW_SWING = 0.1F;
    private static final float GLOW_TIME_DIVISOR = 4.0F;
    private static final int GLOW_PHASE_X = 2;
    private static final int GLOW_PHASE_Y = 3;
    private static final int GLOW_PHASE_Z = 4;

    private static final int FANCY_SPIKES = 20;
    private static final int PLAIN_SPIKES = 10;
    private static final long HALO_SEED = 245L;
    private static final float HALO_FADE_TICKS = 500.0F;
    private static final float HALO_GROW_TICKS = 50.0F;
    private static final int HALO_TIP_RGB = 0xFF00FF;
    private static final float SPIKE_LENGTH_MIN = 5.0F;
    private static final float SPIKE_LENGTH_SPREAD = 20.0F;
    private static final float SPIKE_WIDTH_MIN = 1.0F;
    private static final float SPIKE_WIDTH_SPREAD = 2.0F;
    private static final float SPIKE_SIZE_DIVISOR = 20.0F;
    private static final float BASE_SIDE = 0.866F;
    private static final float BASE_HALF = 0.5F;

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
        float wobble = state.active ? WOBBLE_SCALE * startUp * instability(state) : 0.0F;
        poseStack.pushPose();
        poseStack.translate(CENTER, lift(state), CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees((time % FULL_TURN) * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees((time / PRECESSION_DIVISOR % FULL_TURN) * startUp));
        poseStack.mulPose(Axis.XP.rotationDegrees(TILT * startUp));
        poseStack.mulPose(Axis.ZP.rotationDegrees(TILT * startUp));
        poseStack.scale(CLUSTER_SCALE, CLUSTER_SCALE, CLUSTER_SCALE);
        submitCube(state, poseStack, collector, 0.0F, 0.0F, 0.0F, 0, 0, 0, CORE_SCALE, 0);
        for (int corner = 0; corner < SATELLITE_COUNT; corner++) {
            submitSatellite(state, poseStack, collector, time, wobble, (corner >> 2) & 1, (corner >> 1) & 1, corner & 1);
        }
        poseStack.popPose();
        if (state.crafting) {
            submitHalo(state, poseStack, collector);
        }
    }

    private static float lift(InfusionMatrixRenderState state) {
        return CENTER + (HOVER_BASE + bob(state.animationTime, state.craftTicks)) * state.startUp;
    }

    private static float instability(InfusionMatrixRenderState state) {
        float multiplier = state.stability >= 0.0F ? 1.0F : -state.stability * UNSTABLE_GAIN;
        float ramp = Math.min(state.craftTicks, INSTABILITY_RAMP_TICKS) / INSTABILITY_RAMP_TICKS;
        return Math.min(INSTABILITY_CAP, INSTABILITY_BASE + multiplier * ramp);
    }

    private void submitSatellite(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float time, float wobble, int fx, int fy, int fz) {
        float x = offset(fx) + wobble * Mth.sin((time + WOBBLE_PHASE_STEP * fx) / WOBBLE_DIVISOR_X);
        float y = offset(fy) + wobble * Mth.sin((time + WOBBLE_PHASE_STEP * fy) / WOBBLE_DIVISOR_Y);
        float z = offset(fz) + wobble * Mth.sin((time + WOBBLE_PHASE_STEP * fz) / WOBBLE_DIVISOR_Z);
        int glowPhase = GLOW_PHASE_X * fx + GLOW_PHASE_Y * fy + GLOW_PHASE_Z * fz;
        submitCube(state, poseStack, collector, x, y, z, fx, fy, fz, SATELLITE_SCALE, glowPhase);
    }

    private void submitCube(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float x, float y, float z, int turnsX, int turnsY, int turnsZ, float scale, int glowPhase) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(turnsX * QUARTER_TURN));
        poseStack.mulPose(Axis.YP.rotationDegrees(turnsY * QUARTER_TURN));
        poseStack.mulPose(Axis.ZP.rotationDegrees(turnsZ * QUARTER_TURN));
        poseStack.scale(scale, scale, scale);
        collector.submitModelPart(model.cube, poseStack, RenderTypes.entityCutout(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        if (state.active) {
            float glow = (Mth.sin((state.animationTime + glowPhase) / GLOW_TIME_DIVISOR) * GLOW_SWING + GLOW_BASE) * state.startUp;
            int tint = ARGB.color(Math.clamp(glow, 0.0F, 1.0F), GLOW_RGB);
            collector.submitModelPart(model.glow, poseStack, TTFXRenderTypes.entityAdditiveUnlit(state.texture), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, tint, null);
        }
        poseStack.popPose();
    }

    private static void submitHalo(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float fade = state.craftTicks / HALO_FADE_TICKS;
        float centerAlpha = Math.max(0.0F, 1.0F - fade);
        float grow = Math.min(state.craftTicks, HALO_GROW_TICKS) / HALO_GROW_TICKS;
        if (centerAlpha <= 0.0F || grow <= 0.0F) {
            return;
        }
        int spikes = state.fancyGraphics ? FANCY_SPIKES : PLAIN_SPIKES;
        int centerColor = ARGB.white(Math.clamp(centerAlpha, 0.0F, 1.0F));
        int tipColor = ARGB.color(0, HALO_TIP_RGB);
        float twist = fade * FULL_TURN * Mth.DEG_TO_RAD;
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER, CENTER);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeSpikes(buffer, pose.pose(), spikes, grow, twist, centerColor, tipColor));
        poseStack.popPose();
    }

    private static void writeSpikes(VertexConsumer buffer, Matrix4fc origin, int spikes, float grow, float twist, int centerColor, int tipColor) {
        RandomSource random = RandomSource.create(HALO_SEED);
        Matrix4f running = new Matrix4f(origin);
        for (int spike = 0; spike < spikes; spike++) {
            running.rotateX(angle(random)).rotateY(angle(random)).rotateZ(angle(random)).rotateX(angle(random)).rotateY(angle(random)).rotateZ(angle(random) + twist);
            float length = (random.nextFloat() * SPIKE_LENGTH_SPREAD + SPIKE_LENGTH_MIN) / SPIKE_SIZE_DIVISOR * grow;
            float width = (random.nextFloat() * SPIKE_WIDTH_SPREAD + SPIKE_WIDTH_MIN) / SPIKE_SIZE_DIVISOR * grow;
            float sideX = BASE_SIDE * width;
            float backZ = -BASE_HALF * width;
            spikeFace(buffer, running, -sideX, backZ, sideX, backZ, length, centerColor, tipColor);
            spikeFace(buffer, running, sideX, backZ, 0.0F, width, length, centerColor, tipColor);
            spikeFace(buffer, running, 0.0F, width, -sideX, backZ, length, centerColor, tipColor);
        }
    }

    private static void spikeFace(VertexConsumer buffer, Matrix4fc matrix, float firstX, float firstZ, float secondX, float secondZ, float length, int centerColor, int tipColor) {
        buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(centerColor);
        buffer.addVertex(matrix, firstX, length, firstZ).setColor(tipColor);
        buffer.addVertex(matrix, secondX, length, secondZ).setColor(tipColor);
    }

    private static float angle(RandomSource random) {
        return random.nextFloat() * FULL_TURN * Mth.DEG_TO_RAD;
    }

    private static float bob(float time, int craftTicks) {
        float idle = cycle(time, IDLE_WAVE_PERIOD) / IDLE_BOB_DIVISOR;
        float crafting = cycle(time, CRAFT_WAVE_PERIOD) / CRAFT_BOB_DIVISOR;
        return Mth.lerp(Math.min(craftTicks, BOB_BLEND_TICKS) / BOB_BLEND_TICKS, idle, crafting);
    }

    private static float cycle(float time, float period) {
        return Mth.sin(Mth.TWO_PI * (time % period) / period);
    }

    private static float offset(int flag) {
        return flag == 1 ? SATELLITE_OFFSET : -SATELLITE_OFFSET;
    }
}
