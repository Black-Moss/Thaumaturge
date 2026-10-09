package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.Nullable;

public final class GolemPartRenderHooks {
    private static final String WHEEL_GROUP = "wheel";
    private static final String CLAW_PREFIX = "claw";
    private static final String FIRST_CLAW_SUFFIX = "1";
    private static final String GRINDER_GROUP = "grinder";
    private static final String CARGO_GROUP = "cargo";
    private static final String OUTER_SUFFIX = "_outer";

    private static final float PIXEL = 1.0F / 16.0F;
    private static final float WHEEL_PIVOT_DROP = 2.5F * PIXEL;
    private static final float CLAW_PIVOT_DROP = 1.5F * PIXEL;
    private static final float GRINDER_PIVOT_DROP = PIXEL;
    private static final float CLAW_OPENING_FACTOR = 4.1F;
    private static final float GRINDER_BASE_DEGREES_PER_TICK = 0.5F;
    private static final float LEFT_GRINDER_PHASE = 22.0F;
    private static final float DART_AIM_BASE = 90.0F;
    private static final float DART_INPUT_DIVISOR = 10.0F;
    private static final float HALF_TURN = 180.0F;
    private static final float CARGO_LIFT = 3.0F * PIXEL;
    private static final float CARGO_FORWARD = 6.5F * PIXEL;
    private static final float CARGO_SCALE = 0.45F;

    private static final Map<GolemPartModel, GolemPartRenderHook> HOOKS = new IdentityHashMap<>();
    private static boolean filled;

    private GolemPartRenderHooks() {}

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        if (!filled) {
            fill();
        }
        return HOOKS.getOrDefault(model, GolemPartRenderHook.NONE);
    }

    private static void fill() {
        register(TTGolemParts.LEGS_ROLLER.get(), null, new WheelHook());
        register(TTGolemParts.ARMS_CLAWS.get(), null, new ClawsHook());
        register(TTGolemParts.ARMS_BREAKERS.get(), null, new BreakersHook());
        register(TTGolemParts.ARMS_DARTS.get(), null, new DartsHook());
        register(TTGolemParts.ADDON_HAULER.get(), null, new HaulerHook());
        register(TTGolemParts.ADDON_ARMORED.get(), GolemPartModel.AttachPoint.ARMS, new PauldronHook());
        filled = true;
    }

    private static void register(GolemPart part, GolemPartModel.@Nullable AttachPoint only, GolemPartRenderHook hook) {
        for (GolemPartModel model : part.models()) {
            if (only == null || model.attachPoint() == only) {
                HOOKS.put(model, hook);
            }
        }
    }

    private static void spinAboutX(PoseStack pose, float pivotDrop, float degrees) {
        pose.translate(0.0F, -pivotDrop, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(degrees));
    }

    static final class WheelHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (WHEEL_GROUP.equals(partName)) {
                spinAboutX(poseStack, WHEEL_PIVOT_DROP, -state.wheelRotation);
            }
        }
    }

    static final class ClawsHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (!partName.startsWith(CLAW_PREFIX)) {
                return;
            }
            float swing = CLAW_OPENING_FACTOR * state.attackTime;
            float opening = swing * swing;
            spinAboutX(poseStack, CLAW_PIVOT_DROP, partName.endsWith(FIRST_CLAW_SUFFIX) ? opening : -opening);
        }
    }

    static final class BreakersHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (!GRINDER_GROUP.equals(partName)) {
                return;
            }
            float spin = state.grinderRot + GRINDER_BASE_DEGREES_PER_TICK * state.ageInTicks;
            spinAboutX(poseStack, GRINDER_PIVOT_DROP, side == GolemPartModel.LimbSide.LEFT ? -(spin + LEFT_GRINDER_PHASE) : spin);
        }
    }

    static final class DartsHook implements GolemPartRenderHook {
        @Override
        public float armRotationX(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? DART_AIM_BASE - state.pitch + inputRot / DART_INPUT_DIVISOR : inputRot;
        }

        @Override
        public float armRotationY(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? inputRot / DART_INPUT_DIVISOR : inputRot;
        }

        @Override
        public float armRotationZ(GolemRenderState state, GolemPartModel.LimbSide side, float inputRot) {
            return state.combat ? inputRot / DART_INPUT_DIVISOR : inputRot;
        }
    }

    static final class HaulerHook implements GolemPartRenderHook {
        @Override
        public void postRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {
            if (!CARGO_GROUP.equals(partName) || !state.haulingItem) {
                return;
            }
            poseStack.pushPose();
            poseStack.translate(0.0F, CARGO_LIFT, CARGO_FORWARD);
            poseStack.scale(CARGO_SCALE, CARGO_SCALE, CARGO_SCALE);
            state.haulerItem.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }

    static final class PauldronHook implements GolemPartRenderHook {
        @Override
        public void preRenderObjectPart(String partName, GolemRenderState state, PoseStack poseStack, GolemPartModel.LimbSide side, float partialTick) {
            if (side == GolemPartModel.LimbSide.LEFT && partName.endsWith(OUTER_SUFFIX)) {
                poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN));
            }
        }
    }
}
