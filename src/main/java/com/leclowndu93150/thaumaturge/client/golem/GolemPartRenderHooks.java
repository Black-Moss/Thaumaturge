package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class GolemPartRenderHooks {
    private static final Identifier CLAWS_MESH = TTIds.rl("models/mesh/golem_arms_claws.ttmesh");
    private static final Identifier BREAKERS_MESH = TTIds.rl("models/mesh/golem_arms_breakers.ttmesh");
    private static final Identifier DARTS_MESH = TTIds.rl("models/mesh/golem_arms_darter.ttmesh");
    private static final Identifier WHEEL_MESH = TTIds.rl("models/mesh/golem_legs_wheel.ttmesh");
    private static final Identifier HAULER_MESH = TTIds.rl("models/mesh/golem_hauler.ttmesh");
    private static final Identifier PAULDRONS_MESH = TTIds.rl("models/mesh/golem_armor_pauldrons.ttmesh");

    private static final Map<Identifier, GolemPartRenderHook> HOOKS = Map.of(CLAWS_MESH, new ClawJaws(), BREAKERS_MESH, new GrinderSpin(), DARTS_MESH, new LauncherAim(), WHEEL_MESH, new WheelRoll(),
            HAULER_MESH, new BasketCargo(), PAULDRONS_MESH, new ShoulderMirror());

    private GolemPartRenderHooks() {}

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        return HOOKS.getOrDefault(model.objModel(), GolemPartRenderHook.NONE);
    }

    private static void rotateAboutX(PoseStack pose, float pivotY, float pivotZ, float degrees) {
        pose.translate(0.0F, pivotY, pivotZ);
        pose.mulPose(Axis.XP.rotationDegrees(degrees));
        pose.translate(0.0F, -pivotY, -pivotZ);
    }

    private static final class ClawJaws implements GolemPartRenderHook {
        private static final String FRONT_HALF = "claw001";
        private static final String BACK_HALF = "claw002";
        private static final float HINGE_Y = 0.01F;
        private static final float HINGE_Z = 0.0F;
        private static final float MAX_OPEN_DEGREES = 34.0F;

        @Override
        public void posePiece(String piece, GolemRenderState state, PoseStack pose, GolemPartModel.LimbSide side) {
            float direction = FRONT_HALF.equals(piece) ? 1.0F : BACK_HALF.equals(piece) ? -1.0F : 0.0F;
            if (direction == 0.0F || state.attackTime <= 0.0F) {
                return;
            }
            float swing = state.attackTime * state.attackTime;
            float open = MAX_OPEN_DEGREES * Mth.sin(swing * Mth.PI);
            rotateAboutX(pose, HINGE_Y, HINGE_Z, direction * open);
        }
    }

    private static final class GrinderSpin implements GolemPartRenderHook {
        private static final String GRINDER = "grinder";
        private static final float HUB_Y = 0.0F;
        private static final float HUB_Z = 0.0F;
        private static final float LEFT_LAG_DEGREES = 25.0F;

        @Override
        public void posePiece(String piece, GolemRenderState state, PoseStack pose, GolemPartModel.LimbSide side) {
            if (!GRINDER.equals(piece)) {
                return;
            }
            float angle = side == GolemPartModel.LimbSide.LEFT ? -(state.grinderRot + LEFT_LAG_DEGREES) : state.grinderRot;
            rotateAboutX(pose, HUB_Y, HUB_Z, angle);
        }
    }

    private static final class LauncherAim implements GolemPartRenderHook {
        private static final float LEVEL_AIM = -Mth.HALF_PI;
        private static final float SWAY_KEPT = 0.12F;

        @Override
        public float aimArmPitch(GolemRenderState state, GolemPartModel.LimbSide side, float rigPitch) {
            return state.combat ? LEVEL_AIM + state.xRot * Mth.DEG_TO_RAD + rigPitch * SWAY_KEPT : rigPitch;
        }

        @Override
        public float aimArmYaw(GolemRenderState state, GolemPartModel.LimbSide side, float rigYaw) {
            return state.combat ? rigYaw * SWAY_KEPT : rigYaw;
        }

        @Override
        public float aimArmRoll(GolemRenderState state, GolemPartModel.LimbSide side, float rigRoll) {
            return state.combat ? rigRoll * SWAY_KEPT : rigRoll;
        }
    }

    private static final class WheelRoll implements GolemPartRenderHook {
        private static final String WHEEL = "wheel";
        private static final float AXLE_Y = 0.0F;
        private static final float AXLE_Z = 0.0F;

        @Override
        public void posePiece(String piece, GolemRenderState state, PoseStack pose, GolemPartModel.LimbSide side) {
            if (WHEEL.equals(piece)) {
                rotateAboutX(pose, AXLE_Y, AXLE_Z, -state.wheelRotation);
            }
        }
    }

    private static final class BasketCargo implements GolemPartRenderHook {
        private static final String BASKET = "basket";
        private static final float SEAT_Z = 0.38F;
        private static final float BLOCK_SEAT_Y = 0.15F;
        private static final float BLOCK_SCALE = 0.5F;
        private static final float FLAT_SEAT_Y = 0.07F;
        private static final float FLAT_SCALE = 0.6F;
        private static final float LAY_FLAT_DEGREES = 90.0F;

        @Override
        public void decoratePiece(String piece, GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {
            if (!BASKET.equals(piece) || !state.haulingItem) {
                return;
            }
            pose.pushPose();
            if (state.haulerItemIsBlock) {
                pose.translate(0.0F, BLOCK_SEAT_Y, SEAT_Z);
                pose.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
            } else {
                pose.translate(0.0F, FLAT_SEAT_Y, SEAT_Z);
                pose.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
                pose.scale(FLAT_SCALE, FLAT_SCALE, FLAT_SCALE);
            }
            state.haulerItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }
    }

    private static final class ShoulderMirror implements GolemPartRenderHook {
        private static final float HALF_TURN_DEGREES = 180.0F;

        @Override
        public void posePiece(String piece, GolemRenderState state, PoseStack pose, GolemPartModel.LimbSide side) {
            if (side == GolemPartModel.LimbSide.LEFT) {
                pose.mulPose(Axis.YP.rotationDegrees(HALF_TURN_DEGREES));
            }
        }
    }
}
