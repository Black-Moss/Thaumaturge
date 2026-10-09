package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class FocalManipulatorRenderer implements BlockEntityRenderer<BlockEntityFocalManipulator, FocalManipulatorRenderState> {
    private static final float CENTER = 0.5F;
    private static final float FOCUS_HEIGHT = 0.8F;
    private static final float FOCUS_BOB_AMPLITUDE = 0.1F;
    private static final float FOCUS_BOB_PERIOD = 14.0F;
    private static final float FOCUS_BOB_SWAY = 0.2F;
    private static final float FULL_TURN = 360.0F;
    private static final float RING_TURN = 720.0F;
    private static final float RING_RADIUS = 0.4F;
    private static final float CRYSTAL_BOB_AMPLITUDE = 0.02F;
    private static final float CRYSTAL_BOB_PERIOD = 12.0F;
    private static final float CRYSTAL_BOB_PHASE = 10.0F;
    private static final float GLOW_HEIGHT = 1.3F;
    private static final float GLOW_HALF = 0.175F;
    private static final float GLOW_ALPHA = 0.66F;
    private static final int GLOW_FRAMES = 16;
    private static final float CRYSTAL_HEIGHT = 1.05F;
    private static final float CRYSTAL_SCALE = 0.5F;
    private static final float RAY_APEX_OFFSET = 0.475F;
    private static final float RAY_SCALE = 0.5F;
    private static final int RAY_SECOND_STRIDE = 5;
    private static final float RAY_PAN_AMPLITUDE = 15.0F;
    private static final float RAY_PAN_PERIOD = 15.0F;
    private static final float RAY_APERTURE_AMPLITUDE = 2.0F;
    private static final float RAY_APERTURE_PERIOD = 14.0F;
    private static final float RAY_RAMP_TICKS = 10.0F;
    private static final float RAY_LENGTH_BASE = 10.0F;
    private static final float RAY_LENGTH_RANGE = 20.0F;
    private static final float RAY_WIDTH_BASE = 6.0F;
    private static final float RAY_WIDTH_RANGE = 4.0F;
    private static final float RAY_DIVISOR = 30.0F;
    private static final float RAY_SPREAD_X = 0.8F;
    private static final float RAY_SPREAD_Z = 0.5F;
    private static final float[] GLOW_CORNER_X = {-1.0F, -1.0F, 1.0F, 1.0F};
    private static final float[] GLOW_CORNER_Y = {-1.0F, 1.0F, 1.0F, -1.0F};
    private static final float[] RAY_BASE_X = {-RAY_SPREAD_X, RAY_SPREAD_X, 0.0F};
    private static final float[] RAY_BASE_Z = {-RAY_SPREAD_Z, -RAY_SPREAD_Z, 1.0F};
    private static final int VIEW_DISTANCE = 32;
    private static final int SALT_ROLL = 0;
    private static final int SALT_LENGTH = 1;
    private static final int SALT_WIDTH = 2;
    private static final int SALT_STRIDE = 4;
    private static final float HASH_RANGE = 1 << 24;
    private static final int HASH_SHIFT = 8;

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
    public void extractRenderState(BlockEntityFocalManipulator manipulator, FocalManipulatorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(manipulator, state, partialTicks, cameraPosition, breakProgress);
        Entity viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partialTicks;
        int seed = Long.hashCode(manipulator.getBlockPos().asLong());
        extractFocus(manipulator, state, seed);
        extractCrystals(manipulator, state, seed);
    }

    private void extractFocus(BlockEntityFocalManipulator manipulator, FocalManipulatorRenderState state, int seed) {
        ItemStack stack = manipulator.focusStack();
        if (stack.isEmpty()) {
            state.focus = null;
            return;
        }
        ItemStackRenderState target = state.focus == null ? new ItemStackRenderState() : state.focus;
        itemModelResolver.updateForTopItem(target, stack, ItemDisplayContext.GROUND, manipulator.getLevel(), null, seed);
        state.focus = target;
        state.focusLift = LegacyItemLift.centerLift(target);
    }

    private void extractCrystals(BlockEntityFocalManipulator manipulator, FocalManipulatorRenderState state, int seed) {
        List<AspectInstance> entries = manipulator.crystals().entries();
        if (state.crystals.size() > entries.size()) {
            state.crystals.subList(entries.size(), state.crystals.size()).clear();
        }
        state.crystalColors.clear();
        for (int index = 0; index < entries.size(); index++) {
            Holder<IAspect> aspect = entries.get(index).aspect();
            if (index == state.crystals.size()) {
                state.crystals.add(new ItemStackRenderState());
            }
            ItemStackRenderState slot = state.crystals.get(index);
            itemModelResolver.updateForTopItem(slot, EssentiaCrystalFactory.of(aspect), ItemDisplayContext.GROUND, manipulator.getLevel(), null, seed);
            state.crystalColors.add(aspect.value().color());
            state.crystalLift = LegacyItemLift.centerLift(slot);
        }
    }

    @Override
    public void submit(FocalManipulatorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        float time = state.ticks;
        if (state.focus != null) {
            float bob = FOCUS_BOB_AMPLITUDE * Mth.sin(FOCUS_BOB_SWAY * Mth.sin(time / FOCUS_BOB_PERIOD) + FOCUS_BOB_SWAY);
            poseStack.pushPose();
            poseStack.translate(CENTER, FOCUS_HEIGHT + bob, CENTER);
            poseStack.mulPose(Axis.YP.rotationDegrees(time % FULL_TURN));
            poseStack.translate(0.0F, state.focusLift, 0.0F);
            state.focus.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        int count = state.crystals.size();
        float ring = time % RING_TURN / 2.0F;
        for (int index = 0; index < count; index++) {
            float angle = ring + index * (FULL_TURN / count);
            float sin = Mth.sin(angle * Mth.DEG_TO_RAD);
            float cos = Mth.cos(angle * Mth.DEG_TO_RAD);
            float x = CENTER + RING_RADIUS * sin;
            float z = CENTER + RING_RADIUS * cos;
            float bob = CRYSTAL_BOB_AMPLITUDE * Mth.sin((time + CRYSTAL_BOB_PHASE * index) / CRYSTAL_BOB_PERIOD) + CRYSTAL_BOB_AMPLITUDE;
            int color = state.crystalColors.get(index);
            submitGlow(poseStack, collector, camera, x, GLOW_HEIGHT + bob, z, time, color);
            poseStack.pushPose();
            poseStack.translate(x, CRYSTAL_HEIGHT + bob + CRYSTAL_SCALE * state.crystalLift, z);
            poseStack.scale(CRYSTAL_SCALE, CRYSTAL_SCALE, CRYSTAL_SCALE);
            state.crystals.get(index).submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            float apexY = CRYSTAL_HEIGHT + bob + CRYSTAL_SCALE * (RAY_APEX_OFFSET + state.crystalLift);
            submitRay(poseStack, collector, x, apexY, z, -sin, -cos, angle, index, time, color);
            submitRay(poseStack, collector, x, apexY, z, -sin, -cos, angle, (index + 1) * RAY_SECOND_STRIDE, time, color);
        }
    }

    private static void submitGlow(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, float x, float y, float z, float time, int color) {
        int frame = (int) (time % GLOW_FRAMES);
        float left = StripUv.u0(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float right = StripUv.u1(frame, ParticleTextures.STAR_GLINT_FRAMES);
        float red = ARGB.redFloat(color);
        float green = ARGB.greenFloat(color);
        float blue = ARGB.blueFloat(color);
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.additive(ParticleTextures.STAR_GLINT), (pose, buffer) -> {
            for (int corner = 0; corner < GLOW_CORNER_X.length; corner++) {
                float cornerX = GLOW_CORNER_X[corner];
                float cornerY = GLOW_CORNER_Y[corner];
                buffer.addVertex(pose, cornerX * GLOW_HALF, cornerY * GLOW_HALF, 0.0F).setUv(cornerX < 0.0F ? right : left, cornerY < 0.0F ? StripUv.V1 : StripUv.V0)
                        .setColor(red, green, blue, GLOW_ALPHA).setLight(LightCoordsUtil.FULL_BRIGHT);
            }
        });
        poseStack.popPose();
    }

    private static void submitRay(PoseStack poseStack, SubmitNodeCollector collector, float x, float y, float z, float inwardX, float inwardZ, float ringAngle, int ray, float time, int color) {
        float phase = time + CRYSTAL_BOB_PHASE * ray;
        float ramp = Math.min(time, RAY_RAMP_TICKS) / RAY_RAMP_TICKS;
        float pan = RAY_PAN_AMPLITUDE * Mth.sin(phase / RAY_PAN_PERIOD);
        float aperture = RAY_APERTURE_AMPLITUDE * Mth.sin(phase / RAY_APERTURE_PERIOD);
        float reach = RAY_SCALE * (unit(ray, SALT_LENGTH) * RAY_LENGTH_RANGE + RAY_LENGTH_BASE) / RAY_DIVISOR * ramp;
        float girth = RAY_SCALE * (unit(ray, SALT_WIDTH) * RAY_WIDTH_RANGE + RAY_WIDTH_BASE + aperture) / RAY_DIVISOR * ramp;
        float spin = ringAngle + unit(ray, SALT_ROLL) * FULL_TURN;
        float lean = pan * Mth.DEG_TO_RAD;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(rayAim(inwardX, inwardZ, lean));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.SPARKLE, (pose, buffer) -> writeRayFan(pose, buffer, color, girth, reach));
        poseStack.popPose();
    }

    private static Quaternionf rayAim(float inwardX, float inwardZ, float lean) {
        float flatten = Mth.cos(lean);
        return new Quaternionf().rotationTo(0.0F, 1.0F, 0.0F, inwardX * flatten, Mth.sin(lean), inwardZ * flatten);
    }

    private static void writeRayFan(PoseStack.Pose pose, VertexConsumer buffer, int color, float girth, float reach) {
        float red = ARGB.redFloat(color);
        float green = ARGB.greenFloat(color);
        float blue = ARGB.blueFloat(color);
        for (int edge = 0; edge < RAY_BASE_X.length; edge++) {
            int next = (edge + 1) % RAY_BASE_X.length;
            buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(red, green, blue, GLOW_ALPHA);
            buffer.addVertex(pose, RAY_BASE_X[edge] * girth, reach, RAY_BASE_Z[edge] * girth).setColor(red, green, blue, 0.0F);
            buffer.addVertex(pose, RAY_BASE_X[next] * girth, reach, RAY_BASE_Z[next] * girth).setColor(red, green, blue, 0.0F);
        }
    }

    private static float unit(int index, int salt) {
        return (Mth.murmurHash3Mixer(Mth.murmurHash3Mixer(index * SALT_STRIDE + salt)) >>> HASH_SHIFT) / HASH_RANGE;
    }
}
