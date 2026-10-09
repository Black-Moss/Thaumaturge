package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

final class OverlayQuad {
    static final float HALF = 0.5F;
    static final float WHITE = 1.0F;

    private static final float[] QUAD_X = {-HALF, HALF, HALF, -HALF};
    private static final float[] QUAD_Y = {HALF, HALF, -HALF, -HALF};
    private static final float[] QUAD_U = {1.0F, 1.0F, 0.0F, 0.0F};
    private static final float[] QUAD_V = {1.0F, 0.0F, 0.0F, 1.0F};

    private OverlayQuad() {}

    static void emit(VertexConsumer buffer, Matrix4f pose, float originX, float originY, float originZ, Direction xAxis, Direction yAxis, float degrees, float red, float green, float blue, float alpha) {
        float cos = Mth.cos(degrees * Mth.DEG_TO_RAD);
        float sin = Mth.sin(degrees * Mth.DEG_TO_RAD);
        for (int vertex = 0; vertex < QUAD_X.length; vertex++) {
            float localX = QUAD_X[vertex] * cos - QUAD_Y[vertex] * sin;
            float localY = QUAD_X[vertex] * sin + QUAD_Y[vertex] * cos;
            buffer.addVertex(pose, originX + localX * xAxis.getStepX() + localY * yAxis.getStepX(), originY + localX * xAxis.getStepY() + localY * yAxis.getStepY(),
                    originZ + localX * xAxis.getStepZ() + localY * yAxis.getStepZ()).setUv(QUAD_U[vertex], QUAD_V[vertex]).setColor(red, green, blue, alpha);
        }
    }
}
