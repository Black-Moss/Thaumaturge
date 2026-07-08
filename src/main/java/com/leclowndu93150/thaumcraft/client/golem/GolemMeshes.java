package com.leclowndu93150.thaumcraft.client.golem;

import com.leclowndu93150.thaumcraft.Thaumcraft;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshModel;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshPart;
import com.leclowndu93150.thaumcraft.client.model.obj.TCObjLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class GolemMeshes {
    private static final Map<Identifier, MeshModel> CACHE = new ConcurrentHashMap<>();
    private static final Vector3f DEFAULT_NORMAL = new Vector3f(0.0F, 1.0F, 0.0F);

    private GolemMeshes() {}

    public static MeshModel get(Identifier objLocation) {
        return CACHE.computeIfAbsent(objLocation, location -> {
            try {
                return TCObjLoader.load(Minecraft.getInstance().getResourceManager(), location);
            } catch (IOException e) {
                Thaumcraft.LOGGER.error("Failed to load golem obj model {}", location, e);
                return new MeshModel();
            }
        });
    }

    public static void renderPart(MeshModel mesh, MeshPart part, PoseStack.Pose pose, VertexConsumer buffer,
                                  int light, int color) {
        List<int[]> indices = part.indices();
        for (int quad = 0; quad + 3 < indices.size(); quad += 4) {
            for (int corner = 0; corner < 4; corner++) {
                int[] vertex = indices.get(quad + corner);
                Vector3f position = mesh.positions().get(vertex[0]);
                float u = 0.0F;
                float v = 0.0F;
                if (vertex.length > 1 && vertex[1] >= 0 && vertex[1] < mesh.texCoords().size()) {
                    Vector2f tex = mesh.texCoords().get(vertex[1]);
                    u = tex.x;
                    v = 1.0F - tex.y;
                }
                Vector3f normal = DEFAULT_NORMAL;
                if (vertex.length > 2 && vertex[2] >= 0 && vertex[2] < mesh.normals().size()) {
                    normal = mesh.normals().get(vertex[2]);
                }
                buffer.addVertex(pose, position.x, position.y, position.z)
                        .setColor(color)
                        .setUv(u, v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, normal.x, normal.y, normal.z);
            }
        }
    }
}
