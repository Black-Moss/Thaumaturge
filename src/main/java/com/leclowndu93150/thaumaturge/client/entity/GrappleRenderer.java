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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class GrappleRenderer extends EntityRenderer<EntityGrapple, GrappleRenderer.State> {
    private static final Identifier MODEL_TEXTURE = TTIds.rl("textures/entity/grappler.png");
    private static final Identifier ROPE_TEXTURE = TTIds.rl("textures/misc/rope.png");
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float MODEL_YAW_OFFSET = 90.0F;
    private static final float GLOW_PULSE_DIVISOR = 5.0F;
    private static final float GLOW_PULSE_AMPLITUDE = 0.2F;
    private static final float GLOW_PULSE_OFFSET = 0.2F;
    private static final float GLOW_HALF = 0.5F;
    private static final int GLOW_TINT = ARGB.white(0.21F);
    private static final int GLOW_LIGHT = 0x00F000DC;
    private static final float SAMPLES_PER_BLOCK = 5.0F;
    private static final int MIN_SEGMENTS = 2;
    private static final float SAG_X_PERIOD = 10.0F;
    private static final float SAG_Y_PERIOD = 8.0F;
    private static final float SAG_Z_PERIOD = 6.0F;
    private static final float SAG_DAMPING_DIVISOR = 1.25F;
    private static final float HAND_SIDE = 0.1F;
    private static final float HAND_FORWARD = 0.3F;
    private static final float HAND_DROP = 0.1F;
    private static final float ROPE_RADIUS = 0.025F;
    private static final int ROPE_SIDES = 4;
    private static final float ROPE_V_STEP = 0.5F;
    private static final int ROPE_TINT = ARGB.white(1.0F);
    private static final float VERTICAL_THRESHOLD = 0.9F;
    private static final int COMPONENTS = 3;

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

    private static Vec3 ropeEnd(EntityGrapple entity, Entity owner, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (owner == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            Player player = minecraft.player;
            float yaw = player.getViewYRot(partialTicks) * Mth.DEG_TO_RAD;
            float side = entity.getHand() == InteractionHand.MAIN_HAND ? HAND_SIDE : -HAND_SIDE;
            double forwardX = -Mth.sin(yaw);
            double forwardZ = Mth.cos(yaw);
            double rightX = -Mth.cos(yaw);
            double rightZ = -Mth.sin(yaw);
            Vec3 eye = player.getEyePosition(partialTicks);
            return new Vec3(eye.x + rightX * side + forwardX * HAND_FORWARD, eye.y - HAND_DROP, eye.z + rightZ * side + forwardZ * HAND_FORWARD);
        }
        return new Vec3(Mth.lerp(partialTicks, owner.xOld, owner.getX()), Mth.lerp(partialTicks, owner.yOld, owner.getY()) + owner.getBbHeight() / 2.0F,
                Mth.lerp(partialTicks, owner.zOld, owner.getZ()));
    }

    private static void fillRope(List<Vec3> points, Vec3 end, float amplitude) {
        float length = (float) end.length();
        int segments = (int) (length * SAMPLES_PER_BLOCK);
        if (segments < MIN_SEGMENTS) {
            return;
        }
        points.add(Vec3.ZERO);
        for (int i = 1; i < segments; i++) {
            float fraction = i / (float) segments;
            float distance = fraction * length;
            float damping = 1.0F - fraction / SAG_DAMPING_DIVISOR;
            points.add(new Vec3(end.x * fraction + Mth.sin(distance / SAG_X_PERIOD) * amplitude * damping, end.y * fraction + Mth.sin(distance / SAG_Y_PERIOD) * amplitude * damping,
                    end.z * fraction + Mth.sin(distance / SAG_Z_PERIOD) * amplitude * damping));
        }
        points.add(end);
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
        if (state.points.size() > MIN_SEGMENTS) {
            submitRope(state, poseStack, collector);
        }
    }

    private static void submitGlow(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int frame = Math.floorMod(state.ticks, ParticleTextures.GOLEM_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.GOLEM_ORB_FRAMES);
        float half = GLOW_HALF * (1.0F + (Mth.sin(state.ticks / GLOW_PULSE_DIVISOR) * GLOW_PULSE_AMPLITUDE + GLOW_PULSE_OFFSET));
        poseStack.pushPose();
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.GOLEM_ORB_BLUE), (pose, buffer) -> writeGlow(buffer, pose.pose(), half, u0, u1));
        poseStack.popPose();
    }

    private static void writeGlow(VertexConsumer buffer, Matrix4fc matrix, float half, float u0, float u1) {
        buffer.addVertex(matrix, -half, -half, 0.0F).setUv(u0, StripUv.V1).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, half, -half, 0.0F).setUv(u1, StripUv.V1).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, half, half, 0.0F).setUv(u1, StripUv.V0).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
        buffer.addVertex(matrix, -half, half, 0.0F).setUv(u0, StripUv.V0).setColor(GLOW_TINT).setLight(GLOW_LIGHT);
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
        float[] ring = new float[count * ROPE_SIDES * COMPONENTS];
        Vector3f tangent = new Vector3f();
        Vector3f reference = new Vector3f();
        Vector3f across = new Vector3f();
        Vector3f along = new Vector3f();
        for (int i = 0; i < count; i++) {
            int previous = Math.max(i - 1, 0);
            int next = Math.min(i + 1, count - 1);
            tangent.set(points[next * COMPONENTS] - points[previous * COMPONENTS], points[next * COMPONENTS + 1] - points[previous * COMPONENTS + 1],
                    points[next * COMPONENTS + 2] - points[previous * COMPONENTS + 2]);
            if (tangent.lengthSquared() == 0.0F) {
                tangent.set(0.0F, 1.0F, 0.0F);
            }
            tangent.normalize();
            boolean vertical = Math.abs(tangent.y) > VERTICAL_THRESHOLD;
            reference.set(vertical ? 1.0F : 0.0F, vertical ? 0.0F : 1.0F, 0.0F);
            reference.cross(tangent, across).normalize();
            tangent.cross(across, along);
            for (int side = 0; side < ROPE_SIDES; side++) {
                float angle = Mth.HALF_PI * side;
                float cos = Mth.cos(angle) * ROPE_RADIUS;
                float sin = Mth.sin(angle) * ROPE_RADIUS;
                int base = (i * ROPE_SIDES + side) * COMPONENTS;
                ring[base] = points[i * COMPONENTS] + across.x * cos + along.x * sin;
                ring[base + 1] = points[i * COMPONENTS + 1] + across.y * cos + along.y * sin;
                ring[base + 2] = points[i * COMPONENTS + 2] + across.z * cos + along.z * sin;
            }
        }
        for (int i = 0; i < count - 1; i++) {
            for (int side = 0; side < ROPE_SIDES; side++) {
                int nextSide = (side + 1) % ROPE_SIDES;
                float u0 = side / (float) ROPE_SIDES;
                float u1 = (side + 1) / (float) ROPE_SIDES;
                ropeVertex(buffer, matrix, ring, i, side, u0, i * ROPE_V_STEP, light);
                ropeVertex(buffer, matrix, ring, i, nextSide, u1, i * ROPE_V_STEP, light);
                ropeVertex(buffer, matrix, ring, i + 1, nextSide, u1, (i + 1) * ROPE_V_STEP, light);
                ropeVertex(buffer, matrix, ring, i + 1, side, u0, (i + 1) * ROPE_V_STEP, light);
            }
        }
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
