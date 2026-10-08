package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public final class CornerFaceQuads implements ConnectedFaceQuads {
    private static final float HALF = ConnectedQuadBaker.FACE_SIZE / 2.0F;
    private static final CuboidFace.UVs FULL_UVS = new CuboidFace.UVs(0.0F, 0.0F, ConnectedQuadBaker.FACE_SIZE, ConnectedQuadBaker.FACE_SIZE);
    private static final Direction[] FACES = Direction.values();

    private final BakedQuad[][] quads;
    private final int flags;

    private CornerFaceQuads(BakedQuad[][] quads, int flags) {
        this.quads = quads;
        this.flags = flags;
    }

    public static CornerFaceQuads bake(ModelBaker baker, Identifier directory) {
        Identifier[] sprites = FaceCorners.sprites(directory);
        Material.Baked[] materials = new Material.Baked[sprites.length];
        for (int slot = 0; slot < sprites.length; slot++) {
            materials[slot] = ConnectedQuadBaker.material(baker, sprites[slot]);
        }
        BakedQuad[][] quads = new BakedQuad[FACES.length][sprites.length];
        int flags = 0;
        for (Direction face : FACES) {
            for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
                float minU = HALF * FaceCorners.column(corner);
                float minV = HALF * FaceCorners.row(corner);
                for (int state = 0; state < FaceCorners.STATES; state++) {
                    int slot = FaceCorners.slot(corner, state);
                    BakedQuad quad = ConnectedQuadBaker.bake(baker, materials[slot], face, 0.0F, minU, minV, minU + HALF, minV + HALF, FULL_UVS);
                    quads[face.ordinal()][slot] = quad;
                    flags |= quad.materialInfo().flags();
                }
            }
        }
        return new CornerFaceQuads(quads, flags);
    }

    @Override
    public boolean connects(Direction face) {
        return true;
    }

    @Override
    public void addFace(Direction face, int connections, QuadCollection.Builder builder) {
        BakedQuad[] faceQuads = quads[face.ordinal()];
        for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
            builder.addCulledFace(face, faceQuads[FaceCorners.slot(corner, FaceCorners.state(connections, corner))]);
        }
    }

    @Override
    public int materialFlags() {
        return flags;
    }
}
