package com.leclowndu93150.thaumcraft.client.render.crystal;

import com.leclowndu93150.thaumcraft.client.model.obj.MeshModel;
import com.leclowndu93150.thaumcraft.client.model.obj.MeshPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class CrystalQuadBaker {
    private CrystalQuadBaker() {}

    public static void bakePart(MeshModel mesh, MeshPart part, Baked baked, int tintIndex, Matrix4f transform, java.util.List<BakedQuad> output) {
        BakedQuad.MaterialInfo info = new BakedQuad.MaterialInfo(
                baked.sprite(),
                ChunkSectionLayer.CUTOUT,
                Sheets.cutoutBlockItemSheet(),
                tintIndex,
                true,
                0);
        for (int i = 0; i + 3 < part.indices().size(); i += 4) {
            BakedQuad q = bakeQuad(mesh, part, i, baked, transform, info);
            if (q != null) output.add(q);
        }
    }

    private static BakedQuad bakeQuad(MeshModel mesh, MeshPart part, int start, Baked baked, Matrix4f transform, BakedQuad.MaterialInfo info) {
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
            float v = baked.sprite().getV(tex.y);
            packedUvs[i] = UVPair.pack(u, v);
        }
        Direction facing = computeFaceDirection(positions);
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3],
                facing,
                info);
    }

    private static Direction computeFaceDirection(Vector3fc[] positions) {
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
