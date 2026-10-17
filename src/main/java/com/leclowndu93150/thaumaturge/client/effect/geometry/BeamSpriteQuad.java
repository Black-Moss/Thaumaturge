package com.leclowndu93150.thaumaturge.client.effect.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BeamSpriteQuad {
    private static final int SHEET_CELLS = 32;

    private BeamSpriteQuad() {}

    public static void draw(PoseStack poseStack, VertexConsumer consumer, Camera camera, Vec3 centre, float halfSize, int row, int frame, Mirroring mirroring, float red, float green, float blue, float alpha) {
        Vec3 cameraPos = camera.position();
        float u0 = (float) frame / SHEET_CELLS;
        float u1 = u0 + 1.0F / SHEET_CELLS;
        float v0 = (float) row / SHEET_CELLS;
        float v1 = (float) (row + 1) / SHEET_CELLS;
        float leftU = mirroring == Mirroring.HORIZONTAL ? u1 : u0;
        float rightU = mirroring == Mirroring.HORIZONTAL ? u0 : u1;
        poseStack.pushPose();
        poseStack.translate(centre.x - cameraPos.x, centre.y - cameraPos.y, centre.z - cameraPos.z);
        poseStack.mulPose(camera.rotation());
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, matrix, -halfSize, -halfSize, leftU, v1, red, green, blue, alpha);
        vertex(consumer, matrix, -halfSize, halfSize, leftU, v0, red, green, blue, alpha);
        vertex(consumer, matrix, halfSize, halfSize, rightU, v0, red, green, blue, alpha);
        vertex(consumer, matrix, halfSize, -halfSize, rightU, v1, red, green, blue, alpha);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix, x, y, 0.0F).setUv(u, v).setColor(red, green, blue, alpha);
    }

    public enum Mirroring {
        NONE, HORIZONTAL
    }
}
