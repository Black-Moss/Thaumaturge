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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<BlockEntityFocalManipulator, FocalManipulatorRenderState> {
    private static final int VIEW_DISTANCE = 64;
    private static final double BOUNDS_HEIGHT = 2.0;
    private static final float CENTER = 0.5F;
    private static final float FULL_TURN = 360.0F;
    private static final float HALF_TURN = 180.0F;

    private static final float FOCUS_CENTRE_Y = 1.32F;
    private static final float FOCUS_SCALE = 1.0F;
    private static final float FOCUS_SPIN_PER_TICK = 1.6F;
    private static final float FOCUS_BOB_HEIGHT = 0.035F;
    private static final float FOCUS_BOB_PERIOD_TICKS = 70.0F;

    private static final float ORBIT_RADIUS = 0.36F;
    private static final float ORBIT_SPIN_RATIO = 0.5F;
    private static final float CRYSTAL_SCALE = 0.45F;
    private static final float CRYSTAL_BOB_HEIGHT = 0.025F;
    private static final float CRYSTAL_BOB_PERIOD_TICKS = 46.0F;
    private static final float CRYSTAL_BOB_PHASE_STEP = 1.3F;

    private static final float GLINT_RISE = 0.17F;
    private static final float GLINT_SIZE = 0.2F;
    private static final float GLINT_HALF = 0.5F;
    private static final float GLINT_ALPHA = 0.85F;
    private static final float GLINT_TICKS_PER_FRAME = 2.0F;
    private static final int GLINT_FRAME_STEP = 5;

    private static final int BEAM_COUNT = 2;
    private static final float BEAM_START = 0.03F;
    private static final float BEAM_SPREAD_DEGREES = 22.0F;
    private static final float BEAM_SWAY_DEGREES = 14.0F;
    private static final float BEAM_SWAY_PERIOD_TICKS = 90.0F;
    private static final float BEAM_TILT_DEGREES = 10.0F;
    private static final float BEAM_TILT_PERIOD_TICKS = 130.0F;
    private static final float BEAM_BASE_HALF = 0.012F;
    private static final float BEAM_TIP_HALF = 0.07F;
    private static final float BEAM_WIDTH_SWING = 0.35F;
    private static final float BEAM_WIDTH_PERIOD_TICKS = 60.0F;
    private static final float BEAM_MIN_LENGTH = 0.22F;
    private static final float BEAM_LENGTH_RANGE = 0.16F;
    private static final float BEAM_ALPHA = 0.5F;
    private static final int FADED = 0;
    private static final float FLAT_UV = 0.5F;

    private static final int PROPERTY_LENGTH = 0;
    private static final int PROPERTY_ROLL = 1;
    private static final int PROPERTY_PHASE = 2;
    private static final int PROPERTY_COUNT = 3;
    private static final int UNIT_SHIFT = 8;
    private static final float UNIT_SCALE = 1.0F / (1 << 24);

    private final ItemModelResolver itemModelResolver;

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
            float bob = Mth.sin(state.ticks * Mth.TWO_PI / FOCUS_BOB_PERIOD_TICKS) * FOCUS_BOB_HEIGHT;
            poseStack.pushPose();
            poseStack.translate(CENTER, FOCUS_CENTRE_Y + bob - LegacyItemLift.LEGACY_CENTER_Y * FOCUS_SCALE, CENTER);
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

    private static void submitCrystal(FocalManipulatorRenderState state, int index, int count, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float orbitDegrees = (state.ticks * FOCUS_SPIN_PER_TICK * ORBIT_SPIN_RATIO + index * FULL_TURN / count) % FULL_TURN;
        float radians = orbitDegrees * Mth.DEG_TO_RAD;
        float y = FOCUS_CENTRE_Y + Mth.sin(state.ticks * Mth.TWO_PI / CRYSTAL_BOB_PERIOD_TICKS + index * CRYSTAL_BOB_PHASE_STEP) * CRYSTAL_BOB_HEIGHT;
        int color = state.crystalColors.getInt(index);

        poseStack.pushPose();
        poseStack.translate(CENTER, y, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(orbitDegrees));
        poseStack.translate(ORBIT_RADIUS, 0.0F, 0.0F);
        poseStack.pushPose();
        poseStack.translate(0.0F, -LegacyItemLift.LEGACY_CENTER_Y * CRYSTAL_SCALE, 0.0F);
        poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
        poseStack.translate(0.0F, state.crystalLift, 0.0F);
        state.crystals.get(index).submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
        for (int beam = 0; beam < BEAM_COUNT; beam++) {
            submitBeam(state, index, beam, color, poseStack, collector);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(CENTER + Mth.cos(radians) * ORBIT_RADIUS, y + GLINT_RISE, CENTER - Mth.sin(radians) * ORBIT_RADIUS);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(GLINT_SIZE, GLINT_SIZE, GLINT_SIZE);
        int frame = Math.floorMod((int) (state.ticks / GLINT_TICKS_PER_FRAME) + index * GLINT_FRAME_STEP, ParticleTextures.STAR_GLINT_FRAMES);
        float u0 = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float u1 = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES);
        int tint = ARGB.color(GLINT_ALPHA, color);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.STAR_GLINT), (pose, buffer) -> writeGlint(buffer, pose.pose(), u0, u1, tint));
        poseStack.popPose();
    }

    private static void submitBeam(FocalManipulatorRenderState state, int crystal, int beam, int color, PoseStack poseStack, SubmitNodeCollector collector) {
        float side = beam == 0 ? -1.0F : 1.0F;
        float phase = stable(state.seed, crystal, beam, PROPERTY_PHASE) * Mth.TWO_PI;
        float yaw = side * BEAM_SPREAD_DEGREES + Mth.sin(state.ticks * Mth.TWO_PI / BEAM_SWAY_PERIOD_TICKS + phase) * BEAM_SWAY_DEGREES;
        float tilt = Mth.cos(state.ticks * Mth.TWO_PI / BEAM_TILT_PERIOD_TICKS + phase) * BEAM_TILT_DEGREES;
        float roll = stable(state.seed, crystal, beam, PROPERTY_ROLL) * HALF_TURN;
        float length = BEAM_MIN_LENGTH + stable(state.seed, crystal, beam, PROPERTY_LENGTH) * BEAM_LENGTH_RANGE;
        float tipHalf = BEAM_TIP_HALF * (1.0F + Mth.sin(state.ticks * Mth.TWO_PI / BEAM_WIDTH_PERIOD_TICKS - phase) * BEAM_WIDTH_SWING);
        int base = ARGB.color(BEAM_ALPHA, color);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
        poseStack.mulPose(Axis.XP.rotationDegrees(roll));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.WHITE), (pose, buffer) -> writeFan(buffer, pose.pose(), length, tipHalf, base));
        poseStack.popPose();
    }

    private static float stable(int seed, int crystal, int beam, int property) {
        int key = (crystal * BEAM_COUNT + beam) * PROPERTY_COUNT + property;
        return (Mth.murmurHash3Mixer(seed + Mth.murmurHash3Mixer(key)) >>> UNIT_SHIFT) * UNIT_SCALE;
    }

    private static void writeGlint(VertexConsumer buffer, Matrix4fc matrix, float u0, float u1, int tint) {
        buffer.addVertex(matrix, -GLINT_HALF, -GLINT_HALF, 0.0F).setUv(u0, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, -GLINT_HALF, 0.0F).setUv(u1, StripUv.V1).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, GLINT_HALF, GLINT_HALF, 0.0F).setUv(u1, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
        buffer.addVertex(matrix, -GLINT_HALF, GLINT_HALF, 0.0F).setUv(u0, StripUv.V0).setColor(tint).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    private static void writeFan(VertexConsumer buffer, Matrix4fc matrix, float length, float tipHalf, int base) {
        float tip = BEAM_START + length;
        fanVertex(buffer, matrix, BEAM_START, -BEAM_BASE_HALF, 0.0F, base);
        fanVertex(buffer, matrix, BEAM_START, BEAM_BASE_HALF, 0.0F, base);
        fanVertex(buffer, matrix, tip, tipHalf, 0.0F, FADED);
        fanVertex(buffer, matrix, tip, -tipHalf, 0.0F, FADED);
        fanVertex(buffer, matrix, BEAM_START, 0.0F, -BEAM_BASE_HALF, base);
        fanVertex(buffer, matrix, BEAM_START, 0.0F, BEAM_BASE_HALF, base);
        fanVertex(buffer, matrix, tip, 0.0F, tipHalf, FADED);
        fanVertex(buffer, matrix, tip, 0.0F, -tipHalf, FADED);
    }

    private static void fanVertex(VertexConsumer buffer, Matrix4fc matrix, float x, float y, float z, int color) {
        buffer.addVertex(matrix, x, y, z).setUv(FLAT_UV, FLAT_UV).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
