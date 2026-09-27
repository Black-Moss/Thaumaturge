package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3fc;

public final class ModelPartGeometry {
    private ModelPartGeometry() {}

    public static void emit(ModelPart part, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        PoseStack stack = new PoseStack();
        stack.last().set(pose);
        part.visit(stack, (partPose, path, index, cube) -> emitCube(cube, partPose, buffer, light, overlay, color));
    }

    private static void emitCube(ModelPart.Cube cube, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        for (ModelPart.Polygon polygon : cube.polygons) {
            Vector3fc normal = polygon.normal();
            for (ModelPart.Vertex vertex : polygon.vertices()) {
                buffer.addVertex(pose, vertex.worldX(), vertex.worldY(), vertex.worldZ()).setColor(color).setUv(vertex.u(), vertex.v()).setOverlay(overlay).setLight(light).setNormal(pose, normal.x(),
                        normal.y(), normal.z());
            }
        }
    }
}
