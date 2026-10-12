package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderView;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemPartRenderHooksEvent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTGolemPartRenderHooks {
    private static final Identifier CLAWS_MESH = TTIds.rl("models/mesh/golem_arms_claws.ttmesh");
    private static final Identifier BREAKERS_MESH = TTIds.rl("models/mesh/golem_arms_breakers.ttmesh");
    private static final Identifier DARTS_MESH = TTIds.rl("models/mesh/golem_arms_darter.ttmesh");
    private static final Identifier WHEEL_MESH = TTIds.rl("models/mesh/golem_legs_wheel.ttmesh");
    private static final Identifier HAULER_MESH = TTIds.rl("models/mesh/golem_hauler.ttmesh");
    private static final Identifier PAULDRONS_MESH = TTIds.rl("models/mesh/golem_armor_pauldrons.ttmesh");

    private TTGolemPartRenderHooks() {}

    @SubscribeEvent
    public static void onRegisterHooks(RegisterGolemPartRenderHooksEvent event) {
        event.register(CLAWS_MESH, new ClawJaws());
        event.register(BREAKERS_MESH, new GrinderSpin());
        event.register(DARTS_MESH, new LauncherAim());
        event.register(WHEEL_MESH, new WheelRoll());
        event.register(HAULER_MESH, new BasketCargo());
        event.register(PAULDRONS_MESH, new ShoulderMirror());
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
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            float direction = FRONT_HALF.equals(piece) ? 1.0F : BACK_HALF.equals(piece) ? -1.0F : 0.0F;
            float swingProgress = golem.swingProgress();
            if (direction == 0.0F || swingProgress <= 0.0F) {
                return;
            }
            float swing = swingProgress * swingProgress;
            float open = MAX_OPEN_DEGREES * Mth.sin(swing * Mth.PI);
            rotateAboutX(pose, HINGE_Y, HINGE_Z, direction * open);
        }
    }

    private static final class GrinderSpin implements GolemPartRenderHook {
        private static final String GRINDER = "grinder";
        private static final float HUB_Y = 0.0F;
        private static final float HUB_Z = 0.0F;
        private static final float LEFT_LAG_DEGREES = 25.0F;
        private static final float IDLE_DEGREES_PER_TICK = 2.5F;
        private static final float SWING_DEGREES = 720.0F;

        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if (!GRINDER.equals(piece)) {
                return;
            }
            float spin = spin(golem);
            float angle = side == GolemPartModel.LimbSide.LEFT ? -(spin + LEFT_LAG_DEGREES) : spin;
            rotateAboutX(pose, HUB_Y, HUB_Z, angle);
        }

        private static float spin(GolemPartRenderView golem) {
            float swingLeft = 1.0F - golem.swingProgress();
            return IDLE_DEGREES_PER_TICK * golem.ageInTicks() + SWING_DEGREES * (1.0F - swingLeft * swingLeft * swingLeft);
        }
    }

    private static final class LauncherAim implements GolemPartRenderHook {
        private static final float LEVEL_AIM = -Mth.HALF_PI;
        private static final float SWAY_KEPT = 0.12F;

        @Override
        public float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
            return golem.fighting() ? LEVEL_AIM + golem.headPitch() * Mth.DEG_TO_RAD + rigPitch * SWAY_KEPT : rigPitch;
        }

        @Override
        public float aimArmYaw(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigYaw) {
            return golem.fighting() ? rigYaw * SWAY_KEPT : rigYaw;
        }

        @Override
        public float aimArmRoll(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigRoll) {
            return golem.fighting() ? rigRoll * SWAY_KEPT : rigRoll;
        }
    }

    private static final class WheelRoll implements GolemPartRenderHook {
        private static final String WHEEL = "wheel";
        private static final float AXLE_Y = 0.0F;
        private static final float AXLE_Z = 0.0F;

        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if (WHEEL.equals(piece)) {
                rotateAboutX(pose, AXLE_Y, AXLE_Z, -golem.wheelRotation());
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
        public void decoratePiece(String piece, GolemPartRenderView golem, PoseStack pose, SubmitNodeCollector collector, GolemPartModel.LimbSide side) {
            if (!BASKET.equals(piece) || !golem.hasSecondHandItem()) {
                return;
            }
            pose.pushPose();
            if (golem.secondHandItemIsBlock()) {
                pose.translate(0.0F, BLOCK_SEAT_Y, SEAT_Z);
                pose.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
            } else {
                pose.translate(0.0F, FLAT_SEAT_Y, SEAT_Z);
                pose.mulPose(Axis.XP.rotationDegrees(LAY_FLAT_DEGREES));
                pose.scale(FLAT_SCALE, FLAT_SCALE, FLAT_SCALE);
            }
            golem.secondHandItem().submit(pose, collector, golem.lightCoords(), OverlayTexture.NO_OVERLAY, golem.outlineColor());
            pose.popPose();
        }
    }

    private static final class ShoulderMirror implements GolemPartRenderHook {
        private static final float HALF_TURN_DEGREES = 180.0F;

        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if (side == GolemPartModel.LimbSide.LEFT) {
                pose.mulPose(Axis.YP.rotationDegrees(HALF_TURN_DEGREES));
            }
        }
    }
}
