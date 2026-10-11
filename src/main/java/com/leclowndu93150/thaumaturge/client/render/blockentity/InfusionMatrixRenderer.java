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
    private static final int SALT_X = 0;
    private static final int SALT_Y = 1;
    private static final int SALT_Z = 2;
    private static final int SALT_LENGTH = 3;
    private static final int SALT_WIDTH = 4;
    private static final int SALT_STRIDE = 8;
    private static final float HASH_RANGE = 1 << 24;
    private static final int HASH_SHIFT = 8;
    private static final int SALT_PHASE = 5;
    private static final int QUARTER_TURNS = 4;
    private static final float CUBE_SCALE = 1.0F;
    private static final int GLOW_INDEX_BASE = 1;

    private static final float JITTER_SCALE = 0.025F;
    private static final float CRAFT_SWAY = 2.2F;
    private static final float UNSTABLE_GAIN = 0.12F;
    private static final float INSTABILITY_CAP = 8.0F;
    private static final float INSTABILITY_RAMP_TICKS = 50.0F;
    private static final int DRIFT_INDEX_BASE = 8;
    private static final float DRIFT_PERIOD_MIN = 28.0F;
    private static final float DRIFT_PERIOD_SPREAD = 22.0F;

    private static final int GLOW_RGB = 0xC54CFF;
    private static final float GLOW_BASE = 0.32F;
    private static final float GLOW_SWING = 0.14F;
    private static final float GLOW_PERIOD = 56.0F;
    private static final float GLOW_PHASE_STEP = 0.8F;

    private static final int FANCY_SPIKES = 40;
    private static final int PLAIN_SPIKES = 14;
    private static final int SPIKE_INDEX_BASE = 16;
    private static final int SPIKE_CORNERS = 3;
    private static final int AXES = 3;
    private static final int SPIKE_STRIDE = AXES + AXES * SPIKE_CORNERS;
    private static final float SPIKE_REACH = 1.6F;
    private static final float SPIKE_SHORTEST = 0.45F;
    private static final float SPIKE_WIDTH_MIN = 0.025F;
    private static final float SPIKE_WIDTH_SPREAD = 0.035F;
    private static final float VERTICAL_AXIS_LIMIT = 0.9F;
    private static final float HALO_GROW_TICKS = 80.0F;
    private static final float HALO_TURN_TICKS = 1600.0F;
    private static final float HALO_CORE_ALPHA = 0.85F;
    private static final int HALO_TIP_RGB = 0xE23CC9;
    private static final float[] SPIKES = buildSpikes();

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
        poseStack.translate(CENTER, lift(state), CENTER);
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

    private static float lift(InfusionMatrixRenderState state) {
        return CENTER + (HOVER_BASE + bob(state.animationTime, state.craftTicks)) * state.startUp;
    }

    private static float instability(InfusionMatrixRenderState state) {
        float target = state.stability >= 0.0F ? CRAFT_SWAY : Math.min(INSTABILITY_CAP, CRAFT_SWAY - state.stability * UNSTABLE_GAIN);
        float ramp = Math.min(state.craftTicks, INSTABILITY_RAMP_TICKS) / INSTABILITY_RAMP_TICKS;
        return Mth.lerp(ramp, 1.0F, target);
    }

    private void submitSatellite(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float time, float jitter, int fx, int fy, int fz) {
        int index = fx << 2 | fy << 1 | fz;
        float x = offset(fx) * (1.0F + jitter * drift(time, index, SALT_X));
        float y = offset(fy) * (1.0F + jitter * drift(time, index, SALT_Y));
        float z = offset(fz) * (1.0F + jitter * drift(time, index, SALT_Z));
        submitCube(state, poseStack, collector, x, y, z, quarterTurns(index, SALT_X), quarterTurns(index, SALT_Y), quarterTurns(index, SALT_Z), CUBE_SCALE, index + GLOW_INDEX_BASE);
    }

    private static float drift(float time, int index, int salt) {
        int key = DRIFT_INDEX_BASE + index;
        float period = DRIFT_PERIOD_MIN + DRIFT_PERIOD_SPREAD * unit(key, salt);
        return Mth.sin(Mth.TWO_PI * (time / period + unit(key, SALT_PHASE + salt)));
    }

    private static int quarterTurns(int index, int salt) {
        return (int) (unit(index, salt) * QUARTER_TURNS);
    }

    private void submitCube(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector, float x, float y, float z, int turnsX, int turnsY, int turnsZ, float scale, int index) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(turnsX * QUARTER_TURN));
        poseStack.mulPose(Axis.YP.rotationDegrees(turnsY * QUARTER_TURN));
        poseStack.mulPose(Axis.ZP.rotationDegrees(turnsZ * QUARTER_TURN));
        float size = SATELLITE_SCALE * scale;
        poseStack.scale(size, size, size);
        collector.submitModelPart(model.cube, poseStack, RenderTypes.entityCutout(state.texture), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        float glow = state.startUp * (GLOW_BASE + GLOW_SWING * Mth.sin(Mth.TWO_PI * state.animationTime / GLOW_PERIOD + index * GLOW_PHASE_STEP));
        if (glow > 0.0F) {
            int tint = ARGB.color(Math.clamp(glow, 0.0F, 1.0F), GLOW_RGB);
            collector.submitModelPart(model.glow, poseStack, TTFXRenderTypes.entityAdditive(state.texture), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, null, tint, null);
        }
        poseStack.popPose();
    }

    private static void submitHalo(InfusionMatrixRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float coreFade = Math.max(0.0F, 1.0F - state.craftTicks / HALO_TURN_TICKS);
        float coreAlpha = HALO_CORE_ALPHA * coreFade * state.startUp;
        float grow = Math.min(state.craftTicks, HALO_GROW_TICKS) / HALO_GROW_TICKS;
        if (coreAlpha <= 0.0F || grow <= 0.0F) {
            return;
        }
        int spikes = state.fancyGraphics ? FANCY_SPIKES : PLAIN_SPIKES;
        int coreColor = ARGB.white(Math.clamp(coreAlpha, 0.0F, 1.0F));
        int tipColor = ARGB.color(0, HALO_TIP_RGB);
        float reach = SPIKE_REACH * grow;
        poseStack.pushPose();
        poseStack.translate(CENTER, lift(state), CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.craftTicks / HALO_TURN_TICKS * FULL_TURN));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeSpikes(buffer, pose.pose(), spikes, reach, coreColor, tipColor));
        poseStack.popPose();
    }

    private static void writeSpikes(VertexConsumer buffer, Matrix4fc matrix, int spikes, float reach, int coreColor, int tipColor) {
        for (int spike = 0; spike < spikes; spike++) {
            int base = spike * SPIKE_STRIDE + AXES;
            for (int corner = 0; corner < SPIKE_CORNERS; corner++) {
                int from = base + corner * AXES;
                int to = base + (corner + 1) % SPIKE_CORNERS * AXES;
                buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor);
                buffer.addVertex(matrix, SPIKES[to] * reach, SPIKES[to + 1] * reach, SPIKES[to + 2] * reach).setColor(tipColor);
                buffer.addVertex(matrix, SPIKES[from] * reach, SPIKES[from + 1] * reach, SPIKES[from + 2] * reach).setColor(tipColor);
            }
        }
    }

    private static float[] buildSpikes() {
        float[] spikes = new float[FANCY_SPIKES * SPIKE_STRIDE];
        for (int spike = 0; spike < FANCY_SPIKES; spike++) {
            int key = SPIKE_INDEX_BASE + spike;
            float dy = 1.0F - 2.0F * unit(key, SALT_Y);
            float ring = Mth.sqrt(Math.max(0.0F, 1.0F - dy * dy));
            float heading = Mth.TWO_PI * unit(key, SALT_X);
            float dx = Mth.cos(heading) * ring;
            float dz = Mth.sin(heading) * ring;
            float length = Mth.lerp(unit(key, SALT_LENGTH), SPIKE_SHORTEST, 1.0F);
            float width = (SPIKE_WIDTH_MIN + SPIKE_WIDTH_SPREAD * unit(key, SALT_WIDTH)) / SPIKE_REACH;
            boolean steep = Math.abs(dy) > VERTICAL_AXIS_LIMIT;
            float helperX = steep ? 1.0F : 0.0F;
            float helperY = steep ? 0.0F : 1.0F;
            float px = helperY * dz;
            float py = -helperX * dz;
            float pz = helperX * dy - helperY * dx;
            float pLength = Mth.sqrt(px * px + py * py + pz * pz);
            px /= pLength;
            py /= pLength;
            pz /= pLength;
            float qx = dy * pz - dz * py;
            float qy = dz * px - dx * pz;
            float qz = dx * py - dy * px;
            int base = spike * SPIKE_STRIDE;
            spikes[base] = dx;
            spikes[base + 1] = dy;
            spikes[base + 2] = dz;
            for (int corner = 0; corner < SPIKE_CORNERS; corner++) {
                float turn = Mth.TWO_PI * corner / SPIKE_CORNERS;
                float across = Mth.cos(turn) * width;
                float along = Mth.sin(turn) * width;
                int at = base + AXES + corner * AXES;
                spikes[at] = dx * length + px * across + qx * along;
                spikes[at + 1] = dy * length + py * across + qy * along;
                spikes[at + 2] = dz * length + pz * across + qz * along;
            }
        }
        return spikes;
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

    private static float unit(int index, int salt) {
        return (Mth.murmurHash3Mixer(Mth.murmurHash3Mixer(index * SALT_STRIDE + salt)) >>> HASH_SHIFT) / HASH_RANGE;
    }
}
