package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;

public final class LegacyFacingPose {
    private static final float QUARTER = 90.0F;
    private static final float HALF = 180.0F;
    private static final float THREE_QUARTERS = 270.0F;

    private LegacyFacingPose() {}

    public static void apply(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot(facing)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot(facing)));
        poseStack.translate(0.0F, 0.0F, -0.5F);
    }

    private static float xRot(Direction facing) {
        return switch (facing) {
            case DOWN -> QUARTER;
            case UP -> THREE_QUARTERS;
            default -> 0.0F;
        };
    }

    private static float yRot(Direction facing) {
        return switch (facing) {
            case EAST -> QUARTER;
            case SOUTH -> HALF;
            case WEST -> THREE_QUARTERS;
            default -> 0.0F;
        };
    }
}
