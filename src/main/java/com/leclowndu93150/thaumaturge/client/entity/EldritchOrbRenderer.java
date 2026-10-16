package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb, EntityRenderState> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final float CORE_HALF = 0.375F;
    private static final float CORE_FLICKER_BASE = 0.8F;
    private static final float CORE_FLICKER_DEPTH = 0.2F;
    private static final float CORE_FLICKER_RATE = 1.7F;
    private static final int RAYS = 12;
    private static final long RAY_SEED = 187L;
    private static final float GROW_TICKS = 10.0F;
    private static final float SPIN_TICKS_PER_TURN = 80.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float LENGTH_MIN = 5.0F;
    private static final float LENGTH_SPREAD = 20.0F;
    private static final float WIDTH_MIN = 1.0F;
    private static final float WIDTH_SPREAD = 2.0F;
    private static final float RAY_SCALE_DIVISOR = 30.0F;
    private static final float CORNER_X = 0.866F;
    private static final float CORNER_Z = 0.5F;
    private static final int APEX_COLOR = ARGB.color(255, 255, 255, 255);
    private static final int BASE_COLOR = ARGB.color(0, 191, 191, 191);

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        float age = state.ageInTicks;
        int frame = Math.floorMod((int) Math.floor(age), ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.ELDRITCH_ORB_FRAMES);
        int tint = ARGB.white(Mth.clamp(CORE_FLICKER_BASE + CORE_FLICKER_DEPTH * Mth.sin(age * CORE_FLICKER_RATE), 0.0F, 1.0F));
        float ramp = Math.min(age, GROW_TICKS) / GROW_TICKS;
        float spin = age / SPIN_TICKS_PER_TURN * FULL_TURN;
        poseStack.pushPose();
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeBurst(buffer, pose.pose(), ramp, spin));
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.ELDRITCH_ORB), (pose, buffer) -> writeCore(buffer, pose.pose(), u0, u1, tint));
        poseStack.popPose();
    }

    private static void writeBurst(VertexConsumer buffer, Matrix4fc matrix, float ramp, float spin) {
        RandomSource random = RandomSource.create(RAY_SEED);
        Matrix4f pose = new Matrix4f(matrix);
        for (int ray = 0; ray < RAYS; ray++) {
            float angle1 = random.nextFloat() * FULL_TURN;
            float angle2 = random.nextFloat() * FULL_TURN;
            float angle3 = random.nextFloat() * FULL_TURN;
            float angle4 = random.nextFloat() * FULL_TURN;
            float angle5 = random.nextFloat() * FULL_TURN;
            float angle6 = random.nextFloat() * FULL_TURN;
            float length = (random.nextFloat() * LENGTH_SPREAD + LENGTH_MIN) / RAY_SCALE_DIVISOR * ramp;
            float width = (random.nextFloat() * WIDTH_SPREAD + WIDTH_MIN) / RAY_SCALE_DIVISOR * ramp;
            pose.rotateX(angle1 * Mth.DEG_TO_RAD);
            pose.rotateY(angle2 * Mth.DEG_TO_RAD);
            pose.rotateZ(angle3 * Mth.DEG_TO_RAD);
            pose.rotateX(angle4 * Mth.DEG_TO_RAD);
            pose.rotateY(angle5 * Mth.DEG_TO_RAD);
            pose.rotateZ((angle6 + spin) * Mth.DEG_TO_RAD);
            writeRay(buffer, pose, length, width);
        }
    }

    private static void writeRay(VertexConsumer buffer, Matrix4fc matrix, float length, float width) {
        float cornerX = CORNER_X * width;
        float cornerZ = CORNER_Z * width;
        writeFace(buffer, matrix, -cornerX, length, -cornerZ, cornerX, length, -cornerZ);
        writeFace(buffer, matrix, cornerX, length, -cornerZ, 0.0F, length, width);
        writeFace(buffer, matrix, 0.0F, length, width, -cornerX, length, -cornerZ);
    }

    private static void writeFace(VertexConsumer buffer, Matrix4fc matrix, float x1, float y1, float z1, float x2, float y2, float z2) {
        buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(APEX_COLOR);
        buffer.addVertex(matrix, x1, y1, z1).setColor(BASE_COLOR);
        buffer.addVertex(matrix, x2, y2, z2).setColor(BASE_COLOR);
    }

    private static void writeCore(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -CORE_HALF, -CORE_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, CORE_HALF, -CORE_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, CORE_HALF, CORE_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -CORE_HALF, CORE_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
