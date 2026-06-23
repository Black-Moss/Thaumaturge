package com.leclowndu93150.thaumcraft.client.model.obj;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class TCObjBakedModel {
    private final MeshModel model;

    public TCObjBakedModel(MeshModel model) {
        this.model = model;
    }

    public MeshModel model() {
        return model;
    }

    public List<BakedQuad> bake(TextureAtlasSprite sprite, RenderType itemRenderType) {
        List<BakedQuad> result = new ArrayList<>();
        for (MeshPart part : model.parts()) {
            int color = -1;
            if (part.material() != null && part.material().diffuseColor() != null) {
                color = colorOf(part.material().diffuseColor());
            }
            for (int i = 0; i + 3 < part.indices().size(); i += 4) {
                BakedQuad quad = bakeQuad(part, i, sprite, itemRenderType, color);
                if (quad != null) {
                    result.add(quad);
                }
            }
        }
        return result;
    }

    private BakedQuad bakeQuad(MeshPart part, int startIndex, TextureAtlasSprite sprite, RenderType itemRenderType, int color) {
        Vector3fc[] positions = new Vector3fc[4];
        long[] packedUvs = new long[4];
        for (int i = 0; i < 4; i++) {
            int[] indices = part.indices().get(startIndex + i);
            Vector3f pos = new Vector3f();
            Vector2f tex = new Vector2f();
            int slot = 0;
            if (model.positions() != null && indices.length > slot && indices[slot] >= 0) {
                Vector3f source = model.positions().get(indices[slot]);
                pos.set(source);
            }
            slot++;
            if (model.texCoords() != null && indices.length > slot && indices[slot] >= 0) {
                Vector2f source = model.texCoords().get(indices[slot]);
                tex.set(source);
            }
            positions[i] = pos;
            float u = sprite != null ? sprite.getU(tex.x) : tex.x;
            float v = sprite != null ? sprite.getV(tex.y) : tex.y;
            packedUvs[i] = UVPair.pack(u, v);
        }
        Direction facing = computeFaceDirection(positions);
        int tintIndex = part.name() != null && part.name().contains("focus") ? 1 : part.tintIndex();
        BakedQuad.MaterialInfo info = new BakedQuad.MaterialInfo(
                sprite,
                ChunkSectionLayer.CUTOUT,
                itemRenderType,
                tintIndex,
                true,
                color);
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

    private static int colorOf(Vector3f color) {
        int r = (int) color.x;
        int g = (int) color.y;
        int b = (int) color.z;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
