package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<BlockEntityFocalManipulator, FocalManipulatorRenderState> {
    private static final int VIEW_DISTANCE = 32;
    private static final double BOUNDS_HEIGHT = 2.0;
    private static final float CENTER = 0.5F;
    private static final float FULL_TURN = 360.0F;

    private static final float FOCUS_ANCHOR_Y = 0.8F;
    private static final float FOCUS_SCALE = 1.0F;
    private static final float FOCUS_SPIN_PER_TICK = 1.0F;
    private static final float FOCUS_HOVER_HEIGHT = 0.1F;
    private static final float FOCUS_HOVER_BASE = 0.2F;
    private static final float FOCUS_HOVER_SWING = 0.2F;
    private static final float FOCUS_HOVER_TIME_DIVISOR = 14.0F;

    private static final float ORBIT_RADIUS = 0.4F;
    private static final float ORBIT_PERIOD_TICKS = 720.0F;
    private static final float ORBIT_DEGREES_PER_TICK = 0.5F;
    private static final float CRYSTAL_RING_Y = 1.05F;
    private static final float CRYSTAL_SCALE = 0.5F;
    private static final float CRYSTAL_BOB_BASE = 0.02F;
    private static final float CRYSTAL_BOB_HEIGHT = 0.02F;
    private static final float CRYSTAL_BOB_TIME_DIVISOR = 12.0F;
    private static final float CRYSTAL_PHASE_STEP_TICKS = 10.0F;

    private static final float GLINT_CENTRE_Y = 1.3F;
    private static final float GLINT_SIZE = 0.35F;
    private static final float GLINT_HALF = 0.5F;
    private static final float GLINT_ALPHA = 0.66F;

    private static final int RAY_SECOND_FACTOR = 5;
    private static final long RAY_SEED_BASE = 187L;
    private static final float RAY_ORIGIN_RISE = 0.2375F;
    private static final float RAY_SCALE = 0.5F;
    private static final float RAY_ALPHA = 0.66F;
    private static final float RAY_BASE_ALPHA = 0.0F;
    private static final float RAY_RAMP_TICKS = 10.0F;
    private static final float RAY_LENGTH_BASE = 10.0F;
    private static final float RAY_LENGTH_RANGE = 20.0F;
    private static final float RAY_WIDTH_BASE = 6.0F;
    private static final float RAY_WIDTH_RANGE = 4.0F;
    private static final float RAY_UNIT_DIVISOR = 30.0F;
    private static final float RAY_APERTURE_AMPLITUDE = 2.0F;
    private static final float RAY_APERTURE_TIME_DIVISOR = 14.0F;
    private static final float RAY_PAN_DEGREES = 15.0F;
    private static final float RAY_PAN_TIME_DIVISOR = 15.0F;
    private static final float RAY_PHASE_STEP_TICKS = 10.0F;
    private static final float RAY_AXIS_TURN_DEGREES = -90.0F;
    private static final float RAY_BASE_SIDE = 0.8F;
    private static final float RAY_BASE_BACK = 0.5F;

    private final ItemModelResolver itemModelResolver;
    private final RandomSource rayRandom = RandomSource.create(0L);

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public FocalManipulatorRenderState createRenderState() {
        return new FocalManipulatorRenderState();
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityFocalManipulator table) {
        BlockPos pos = table.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + 1.0);
    }

    @Override
    public void extractRenderState(BlockEntityFocalManipulator table, FocalManipulatorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTicks, cameraPosition, breakProgress);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
        state.seed = Long.hashCode(table.getBlockPos().asLong());
        state.focus = null;
        state.crystals.clear();
        state.crystalColors.clear();
        ItemStack focus = table.focusStack();
        if (!focus.isEmpty()) {
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(item, focus, ItemDisplayContext.GROUND, table.getLevel(), null, state.seed);
            state.focus = item;
            state.focusLift = LegacyItemLift.centerLift(item);
        }
        if (!table.inscribing()) {
            return;
        }
        for (AspectInstance cost : table.crystals().entries()) {
            ItemStackRenderState crystal = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(crystal, EssentiaCrystalFactory.of(cost.aspect()), ItemDisplayContext.GROUND, table.getLevel(), null, state.seed);
            state.crystals.add(crystal);
            state.crystalColors.add(cost.aspect().value().color());
        }
        if (!state.crystals.isEmpty()) {
            state.crystalLift = LegacyItemLift.centerLift(state.crystals.getFirst());
        }
    }

    @Override
    public void submit(FocalManipulatorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.focus != null) {
            float hover = FOCUS_HOVER_HEIGHT * Mth.sin(FOCUS_HOVER_BASE + FOCUS_HOVER_SWING * Mth.sin(state.ticks / FOCUS_HOVER_TIME_DIVISOR));
            poseStack.pushPose();
            poseStack.translate(CENTER, FOCUS_ANCHOR_Y + hover, CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * FOCUS_SPIN_PER_TICK % FULL_TURN));
            poseStack.scale(FOCUS_SCALE, FOCUS_SCALE, FOCUS_SCALE);
            poseStack.translate(0.0F, state.focusLift, 0.0F);
            state.focus.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        int count = state.crystals.size();
        for (int index = 0; index < count; index++) {
            submitCrystal(state, index, count, poseStack, collector, camera);
        }
    }

    private void submitCrystal(FocalManipulatorRenderState state, int index, int count, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float orbitDegrees = state.ticks % ORBIT_PERIOD_TICKS * ORBIT_DEGREES_PER_TICK + index * FULL_TURN / count;
        float radians = orbitDegrees * Mth.DEG_TO_RAD;
        float bob = CRYSTAL_BOB_BASE + CRYSTAL_BOB_HEIGHT * Mth.sin((state.ticks + CRYSTAL_PHASE_STEP_TICKS * index) / CRYSTAL_BOB_TIME_DIVISOR);
        float orbitX = CENTER + Mth.sin(radians) * ORBIT_RADIUS;
        float orbitZ = CENTER + Mth.cos(radians) * ORBIT_RADIUS;
        int color = state.crystalColors.getInt(index);

        poseStack.pushPose();
        poseStack.translate(orbitX, CRYSTAL_RING_Y + bob, orbitZ);
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        poseStack.translate(0.0F, state.crystalLift, 0.0F);
        state.crystals.get(index).submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(CENTER, CRYSTAL_RING_Y + bob, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(orbitDegrees));
        poseStack.translate(0.0F, RAY_ORIGIN_RISE, ORBIT_RADIUS);
        submitRay(state, index, orbitDegrees, color, poseStack, collector);
        submitRay(state, (index + 1) * RAY_SECOND_FACTOR, orbitDegrees, color, poseStack, collector);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(orbitX, GLINT_CENTRE_Y + bob, orbitZ);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(GLINT_SIZE, GLINT_SIZE, GLINT_SIZE);
        int frame = Math.floorMod((int) state.ticks, ParticleTextures.STAR_GLINT_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES);
        int tint = ARGB.color(GLINT_ALPHA, color);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.STAR_GLINT), (pose, buffer) -> writeGlint(buffer, pose.pose(), u0, u1, tint));
        poseStack.popPose();
    }

    private void submitRay(FocalManipulatorRenderState state, int num, float orbitDegrees, int color, PoseStack poseStack, SubmitNodeCollector collector) {
        rayRandom.setSeed(RAY_SEED_BASE + (long) num * num);
        float sweep = rayRandom.nextFloat();
        float lengthUnit = rayRandom.nextFloat();
        float widthUnit = rayRandom.nextFloat();
        float phaseTicks = state.ticks + RAY_PHASE_STEP_TICKS * num;
        float ramp = Math.min(state.ticks, RAY_RAMP_TICKS) / RAY_RAMP_TICKS;
        float length = (lengthUnit * RAY_LENGTH_RANGE + RAY_LENGTH_BASE) / RAY_UNIT_DIVISOR * ramp;
        float aperture = RAY_APERTURE_AMPLITUDE * Mth.sin(phaseTicks / RAY_APERTURE_TIME_DIVISOR);
        float width = (widthUnit * RAY_WIDTH_RANGE + RAY_WIDTH_BASE + aperture) / RAY_UNIT_DIVISOR * ramp;
        float pan = RAY_PAN_DEGREES * Mth.sin(phaseTicks / RAY_PAN_TIME_DIVISOR);
        float psi = orbitDegrees + sweep * FULL_TURN;
        int apex = ARGB.color(RAY_ALPHA, color);
        int base = ARGB.color(RAY_BASE_ALPHA, color);
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(RAY_AXIS_TURN_DEGREES));
        poseStack.mulPose(Axis.YP.rotationDegrees(psi));
        poseStack.mulPose(Axis.XP.rotationDegrees(pan));
        poseStack.scale(RAY_SCALE, RAY_SCALE, RAY_SCALE);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeRay(buffer, pose.pose(), length, width, apex, base));
        poseStack.popPose();
    }

    private static void writeGlint(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -GLINT_HALF, -GLINT_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, -GLINT_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, GLINT_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -GLINT_HALF, GLINT_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static void writeRay(VertexConsumer buffer, Matrix4fc matrix, float length, float width, int apex, int base) {
        float sideX = RAY_BASE_SIDE * width;
        float backZ = -RAY_BASE_BACK * width;
        writeTriangle(buffer, matrix, apex, base, -sideX, length, backZ, sideX, length, backZ);
        writeTriangle(buffer, matrix, apex, base, sideX, length, backZ, 0.0F, length, width);
        writeTriangle(buffer, matrix, apex, base, 0.0F, length, width, -sideX, length, backZ);
    }

    private static void writeTriangle(VertexConsumer buffer, Matrix4fc matrix, int apex, int base, float firstX, float firstY, float firstZ, float secondX, float secondY, float secondZ) {
        buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(apex);
        buffer.addVertex(matrix, firstX, firstY, firstZ).setColor(base);
        buffer.addVertex(matrix, secondX, secondY, secondZ).setColor(base);
    }
}
