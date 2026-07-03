package com.leclowndu93150.thaumcraft.client.model.obj;

import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@OnlyIn(Dist.CLIENT)
public final class MeshQuadBaker {
    private MeshQuadBaker() {}

    public static void bakePart(MeshModel mesh, MeshPart part, Material.Baked baked, int tintIndex,
                                Matrix4f transform, BakedQuad.MaterialInfo info, List<BakedQuad> output) {
        bakePart(mesh, part, baked, tintIndex, transform, info, false, output);
    }

    public static void bakePart(MeshModel mesh, MeshPart part, Material.Baked baked, int tintIndex,
                                Matrix4f transform, BakedQuad.MaterialInfo info, boolean flipV, List<BakedQuad> output) {
        for (int i = 0; i + 3 < part.indices().size(); i += 4) {
            BakedQuad quad = bakeQuad(mesh, part, i, baked, transform, info, flipV);
            if (quad != null) {
                output.add(quad);
            }
        }
    }

    private static BakedQuad bakeQuad(MeshModel mesh, MeshPart part, int start, Material.Baked baked,
                                      Matrix4f transform, BakedQuad.MaterialInfo info, boolean flipV) {
        Vector3fc[] positions = new Vector3fc[4];
        long[] packedUvs = new long[4];
        for (int i = 0; i < 4; i++) {
            int[] vertex = part.indices().get(start + i);
            Vector3f pos = new Vector3f();
            Vector2f tex = new Vector2f();
            int slot = 0;
            if (mesh.positions() != null && vertex.length > slot && vertex[slot] >= 0) {
                pos.set(mesh.positions().get(vertex[slot]));
            }
            slot++;
            if (mesh.texCoords() != null && vertex.length > slot && vertex[slot] >= 0) {
                tex.set(mesh.texCoords().get(vertex[slot]));
            }
            pos.mulPosition(transform);
            positions[i] = pos;
            float u = baked.sprite().getU(tex.x);
            float v = baked.sprite().getV(flipV ? 1.0F - tex.y : tex.y);
            packedUvs[i] = UVPair.pack(u, v);
        }
        Direction facing = computeFaceDirection(positions);
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3],
                facing,
                info);
    }

    public static Direction computeFaceDirection(Vector3fc[] positions) {
        Vector3f edge1 = new Vector3f(positions[1]).sub(positions[0]);
        Vector3f edge2 = new Vector3f(positions[2]).sub(positions[0]);
        Vector3f normal = new Vector3f();
        edge1.cross(edge2, normal);
        if (normal.lengthSquared() < 1.0E-6F) {
            return Direction.UP;
        }
        normal.normalize();
        return Direction.getApproximateNearest(normal.x, normal.y, normal.z);
    }
}
