package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class SpriteVertex {
    private SpriteVertex() {}

    public static void add(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, int colour, float u, float v, int overlay, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z);
        buffer.setColor(colour);
        buffer.setUv(u, v);
        buffer.setOverlay(overlay);
        buffer.setLight(light);
        buffer.setNormal(pose, nx, ny, nz);
    }
}
