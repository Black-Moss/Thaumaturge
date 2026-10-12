package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

public final class GrappleRenderer extends EntityRenderer<EntityGrapple, GrappleRenderer.State> {
    private static final Identifier MODEL_TEXTURE = TTIds.rl("textures/entity/grappler.png");
    private static final Identifier ROPE_TEXTURE = TTIds.rl("textures/misc/rope.png");
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float GLOW_HALF = 0.5F;
    private static final int MIN_ROPE_POINTS = 2;
    private static final float ROPE_V_STEP = 0.5F;
    private static final int ROPE_TINT = ARGB.white(1.0F);
    private static final float VERTICAL_THRESHOLD = 0.9F;
    private static final int COMPONENTS = 3;
    private static final float MODEL_YAW_OFFSET = 90.0F;
    private static final float SAMPLES_PER_BLOCK = 5.0F;
    private static final int MIN_ROPE_SAMPLES = 2;
    private static final double WAVE_DIVISOR_X = 10.0;
    private static final double WAVE_DIVISOR_Y = 8.0;
    private static final double WAVE_DIVISOR_Z = 6.0;
    private static final double DAMP_FACTOR = 1.25;
    private static final float ROPE_RADIUS = 0.022F;
    private static final int ROPE_SIDES = 6;
    private static final int RING_STRIDE = ROPE_SIDES * COMPONENTS;
    private static final float GLOW_SCALE_BASE = 1.2F;
    private static final float GLOW_PULSE_SCALE = 0.2F;
    private static final float GLOW_PULSE_DIVISOR = 5.0F;
    private static final int GLOW_TINT = ARGB.colorFromFloat(0.21F, 1.0F, 1.0F, 1.0F);
    private static final int GLOW_LIGHT = 0x00F000DC;
    private static final double HAND_SIDE = 0.1;
    private static final float HAND_YAW_BIAS = 0.5F;
    private static final double HAND_LOOK_DIVISOR = 5.0;
    private static final double HAND_EYE_DIVISOR = 2.6;
    private static final double BODY_MIDDLE = 0.5;
    private static final double TANGENT_EPSILON = 1.0E-8;

    private final GrapplerModel model;

    public GrappleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new GrapplerModel(context.bakeLayer(TTModelLayers.GRAPPLER));
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGrapple entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.ticks = entity.tickCount;
        state.points.clear();
        Entity owner = entity.getOwner();
        if (owner == null) {
            return;
        }
        Vec3 end = ropeEnd(entity, owner, partialTicks).subtract(state.x, state.y, state.z);
        fillRope(state.points, end, entity.ampl);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw - MODEL_YAW_OFFSET));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.pitch));
        collector.submitModelPart(model.root, poseStack, RenderTypes.entityCutout(MODEL_TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        submitGlow(state, poseStack, collector, camera);
        if (state.points.size() > MIN_ROPE_POINTS) {
            submitRope(state, poseStack, collector);
        }
    }

    private static Vec3 ropeEnd(EntityGrapple grapple, Entity owner, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (owner == minecraft.getCameraEntity() && minecraft.options.getCameraType().isFirstPerson()) {
            Vec3 feet = owner.getPosition(partialTicks);
            Vec3 look = owner.getLookAngle();
            double theta = Math.toRadians(Mth.rotLerp(partialTicks, owner.yRotO, owner.getYRot()) - HAND_YAW_BIAS);
            double side = grapple.getHand() == InteractionHand.MAIN_HAND ? HAND_SIDE : -HAND_SIDE;
            return new Vec3(
                    feet.x - Math.cos(theta) * side + look.x / HAND_LOOK_DIVISOR,
                    feet.y + owner.getEyeHeight() / HAND_EYE_DIVISOR + owner.getBbHeight() * BODY_MIDDLE + look.y / HAND_LOOK_DIVISOR,
                    feet.z - Math.sin(theta) * side + look.z / HAND_LOOK_DIVISOR);
        }
        return owner.getPosition(partialTicks).add(0.0, owner.getBbHeight() * BODY_MIDDLE, 0.0);
    }

    private static void fillRope(List<Vec3> points, Vec3 end, float amplitude) {
        float length = (float) end.length() * SAMPLES_PER_BLOCK;
        int count = (int) length;
        points.add(Vec3.ZERO);
        for (int k = 1; k <= count - MIN_ROPE_SAMPLES; k++) {
            double distance = k * (length / count);
            double damp = 1.0 - k / (DAMP_FACTOR * count);
            double along = k / (double) count;
            double x = Math.sin(distance / WAVE_DIVISOR_X) * amplitude * damp;
            double y = Math.sin(distance / WAVE_DIVISOR_Y) * amplitude * damp;
            double z = Math.sin(distance / WAVE_DIVISOR_Z) * amplitude * damp;
            points.add(new Vec3(end.x * along + x, end.y * along + y, end.z * along + z));
        }
        points.add(end);
    }

    private static void submitGlow(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int frame = Math.floorMod(state.ticks, ParticleTextures.GOLEM_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float scale = GLOW_SCALE_BASE + GLOW_PULSE_SCALE * Mth.sin(state.ticks / GLOW_PULSE_DIVISOR);
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        poseStack.scale(scale, scale, scale);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.GOLEM_ORB_BLUE), (pose, buffer) -> writeGlow(buffer, pose.pose(), u0, u1));
        poseStack.popPose();
    }

    private static void writeGlow(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1) {
        buffer.addVertex(matrix, -GLOW_HALF, -GLOW_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, GLOW_HALF, -GLOW_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, GLOW_HALF, GLOW_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, -GLOW_HALF, GLOW_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
    }

    private static void submitRope(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        float[] points = new float[state.points.size() * COMPONENTS];
        for (int i = 0; i < state.points.size(); i++) {
            Vec3 point = state.points.get(i);
            points[i * COMPONENTS] = (float) point.x;
            points[i * COMPONENTS + 1] = (float) point.y;
            points[i * COMPONENTS + 2] = (float) point.z;
        }
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(ROPE_TEXTURE), (pose, buffer) -> writeRope(buffer, pose.pose(), points, light));
    }

    private static void writeRope(VertexConsumer buffer, Matrix4fc matrix, float[] points, int light) {
        int count = points.length / COMPONENTS;
        float[] ring = buildRings(points, count);
        for (int i = 0; i < count - 1; i++) {
            float v0 = i * ROPE_V_STEP;
            float v1 = (i + 1) * ROPE_V_STEP;
            for (int side = 0; side < ROPE_SIDES; side++) {
                int next = (side + 1) % ROPE_SIDES;
                float u0 = side / (float) ROPE_SIDES;
                float u1 = (side + 1) / (float) ROPE_SIDES;
                ropeVertex(buffer, matrix, ring, i, side, u0, v0, light);
                ropeVertex(buffer, matrix, ring, i + 1, side, u0, v1, light);
                ropeVertex(buffer, matrix, ring, i + 1, next, u1, v1, light);
                ropeVertex(buffer, matrix, ring, i, next, u1, v0, light);
            }
        }
    }

    private static float[] buildRings(float[] points, int count) {
        float[] ring = new float[count * RING_STRIDE];
        double nx = 0.0;
        double ny = 0.0;
        double nz = 0.0;
        double tx = 0.0;
        double ty = 1.0;
        double tz = 0.0;
        for (int i = 0; i < count; i++) {
            int before = Math.max(0, i - 1) * COMPONENTS;
            int after = Math.min(count - 1, i + 1) * COMPONENTS;
            double dx = points[after] - points[before];
            double dy = points[after + 1] - points[before + 1];
            double dz = points[after + 2] - points[before + 2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length > TANGENT_EPSILON) {
                tx = dx / length;
                ty = dy / length;
                tz = dz / length;
            }
            boolean steep = Math.abs(ty) > VERTICAL_THRESHOLD;
            if (i == 0) {
                nx = steep ? 1.0 : 0.0;
                ny = steep ? 0.0 : 1.0;
                nz = 0.0;
            }
            double onTangent = nx * tx + ny * ty + nz * tz;
            nx -= onTangent * tx;
            ny -= onTangent * ty;
            nz -= onTangent * tz;
            double normal = Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (normal < TANGENT_EPSILON) {
                nx = steep ? 1.0 - tx * tx : -ty * tx;
                ny = steep ? -tx * ty : 1.0 - ty * ty;
                nz = steep ? -tx * tz : -ty * tz;
                normal = Math.sqrt(nx * nx + ny * ny + nz * nz);
            }
            nx /= normal;
            ny /= normal;
            nz /= normal;
            double bx = ty * nz - tz * ny;
            double by = tz * nx - tx * nz;
            double bz = tx * ny - ty * nx;
            int point = i * COMPONENTS;
            for (int side = 0; side < ROPE_SIDES; side++) {
                double angle = side * Mth.TWO_PI / ROPE_SIDES;
                double cos = Math.cos(angle) * ROPE_RADIUS;
                double sin = Math.sin(angle) * ROPE_RADIUS;
                int at = i * RING_STRIDE + side * COMPONENTS;
                ring[at] = (float) (points[point] + nx * cos + bx * sin);
                ring[at + 1] = (float) (points[point + 1] + ny * cos + by * sin);
                ring[at + 2] = (float) (points[point + 2] + nz * cos + bz * sin);
            }
        }
        return ring;
    }

    private static void ropeVertex(VertexConsumer buffer, Matrix4fc matrix, float[] ring, int index, int side, float u, float v, int light) {
        int base = (index * ROPE_SIDES + side) * COMPONENTS;
        buffer.addVertex(matrix, ring[base], ring[base + 1], ring[base + 2]).setUv(u, v).setColor(ROPE_TINT).setLight(light);
    }

    public static final class State extends EntityRenderState {
        public float yaw;
        public float pitch;
        public int ticks;
        public final List<Vec3> points = new ArrayList<>();
    }
}
