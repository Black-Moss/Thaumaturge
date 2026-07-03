package com.leclowndu93150.thaumcraft.client.model.obj;

import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
    private static final float CONTRACT_EPS = 1.0F / 256.0F;
    private static final float CONTRACT_DIVISOR = 256.0F;

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
        float[] us = new float[4];
        float[] vs = new float[4];
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
            us[i] = baked.sprite().getU(tex.x);
            vs[i] = baked.sprite().getV(flipV ? 1.0F - tex.y : tex.y);
        }
        contractUvs(us, vs, baked.sprite());
        long[] packedUvs = new long[4];
        for (int i = 0; i < 4; i++) {
            packedUvs[i] = UVPair.pack(us[i], vs[i]);
        }
        Direction facing = computeFaceDirection(positions);
        return new BakedQuad(
                positions[0], positions[1], positions[2], positions[3],
                packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3],
                facing,
                info);
    }

    private static void contractUvs(float[] us, float[] vs, TextureAtlasSprite sprite) {
        float texelsU = sprite.contents().width() / (sprite.getU1() - sprite.getU0());
        float texelsV = sprite.contents().height() / (sprite.getV1() - sprite.getV0());
        float texels = Math.max(texelsU, texelsV);
        float minShift = 1.0F / (texels * CONTRACT_DIVISOR);
        float centerU = (us[0] + us[1] + us[2] + us[3]) / 4.0F;
        float centerV = (vs[0] + vs[1] + vs[2] + vs[3]) / 4.0F;
        for (int i = 0; i < 4; i++) {
            us[i] = contract(us[i], centerU, minShift);
            vs[i] = contract(vs[i], centerV, minShift);
        }
    }

    private static float contract(float value, float center, float minShift) {
        float contracted = value * (1.0F - CONTRACT_EPS) + center * CONTRACT_EPS;
        if (Math.abs(value - contracted) >= minShift) {
            return contracted;
        }
        float toCenter = center - value;
        if (Math.abs(toCenter) < minShift) {
            return (value + center) / 2.0F;
        }
        return value + (toCenter < 0.0F ? -minShift : minShift);
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
