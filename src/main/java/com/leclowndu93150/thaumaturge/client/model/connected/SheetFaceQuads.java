package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.Map;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

public final class SheetFaceQuads implements ConnectedFaceQuads {
    private static final int CORNERS = 4;
    private static final int TOP_LEFT_CORNER = 0;
    private static final int TOP_RIGHT_CORNER = 1;
    private static final int BOTTOM_LEFT_CORNER = 2;
    private static final int BOTTOM_RIGHT_CORNER = 3;
    private static final int STATES = 5;
    private static final int UNCONNECTED = 0;
    private static final int VERTICAL = 1;
    private static final int HORIZONTAL = 2;
    private static final int INNER_CORNER = 3;
    private static final int SURROUNDED = 4;
    private static final float HALF = ConnectedQuadBaker.FACE_SIZE / 2.0F;
    private static final float SHEET_TILE = HALF;
    private static final float SHEET_QUARTER = SHEET_TILE / 2.0F;
    private static final int[] SHEET_COLUMN = {0, 1, 0, 1, 0};
    private static final int[] SHEET_ROW = {0, 0, 1, 1, 0};
    private static final int[] CORNER_VERTICAL = {FaceConnections.TOP, FaceConnections.TOP, FaceConnections.BOTTOM, FaceConnections.BOTTOM};
    private static final int[] CORNER_HORIZONTAL = {FaceConnections.LEFT, FaceConnections.RIGHT, FaceConnections.LEFT, FaceConnections.RIGHT};
    private static final int[] CORNER_DIAGONAL = {FaceConnections.TOP_LEFT, FaceConnections.TOP_RIGHT, FaceConnections.BOTTOM_LEFT, FaceConnections.BOTTOM_RIGHT};
    private static final Direction[] FACES = Direction.values();

    private final BakedQuad[][] whole;
    private final BakedQuad[][][] corners;
    private final boolean[] culled;
    private final int flags;

    private SheetFaceQuads(BakedQuad[][] whole, BakedQuad[][][] corners, boolean[] culled, int flags) {
        this.whole = whole;
        this.corners = corners;
        this.culled = culled;
        this.flags = flags;
    }

    public static SheetFaceQuads bake(ModelBaker baker, Map<Direction, ConnectedTexture> faces, float[] insets) {
        BakedQuad[][] whole = new BakedQuad[FACES.length][];
        BakedQuad[][][] corners = new BakedQuad[FACES.length][][];
        boolean[] culled = new boolean[FACES.length];
        int flags = 0;
        for (Map.Entry<Direction, ConnectedTexture> entry : faces.entrySet()) {
            Direction face = entry.getKey();
            ConnectedTexture spec = entry.getValue();
            Material.Baked texture = ConnectedQuadBaker.material(baker, spec.texture());
            Material.Baked sheet = ConnectedQuadBaker.material(baker, spec.sheet());
            Material.Baked unconnected = ConnectedQuadBaker.material(baker, spec.unconnectedCorner());
            float inset = insets[face.ordinal()];
            BakedQuad[] faceWhole = new BakedQuad[STATES];
            BakedQuad[][] faceCorners = new BakedQuad[CORNERS][STATES];
            for (int state = 0; state < STATES; state++) {
                faceWhole[state] = ConnectedQuadBaker.bakeWhole(baker, state == UNCONNECTED ? texture : sheet, face, inset, wholeUvs(state));
                flags |= faceWhole[state].materialInfo().flags();
                for (int corner = 0; corner < CORNERS; corner++) {
                    float minU = HALF * column(corner);
                    float minV = HALF * row(corner);
                    faceCorners[corner][state] = ConnectedQuadBaker.bake(baker, state == UNCONNECTED ? unconnected : sheet, face, inset, minU, minV, minU + HALF, minV + HALF,
                            cornerUvs(state, corner));
                    flags |= faceCorners[corner][state].materialInfo().flags();
                }
            }
            whole[face.ordinal()] = faceWhole;
            corners[face.ordinal()] = faceCorners;
            culled[face.ordinal()] = inset <= 0.0F;
        }
        return new SheetFaceQuads(whole, corners, culled, flags);
    }

    @Override
    public boolean connects(Direction face) {
        return whole[face.ordinal()] != null;
    }

    @Override
    public void addFace(Direction face, int connections, QuadCollection.Builder builder) {
        int topLeft = state(connections, TOP_LEFT_CORNER);
        int topRight = state(connections, TOP_RIGHT_CORNER);
        int bottomLeft = state(connections, BOTTOM_LEFT_CORNER);
        int bottomRight = state(connections, BOTTOM_RIGHT_CORNER);
        if (topLeft == topRight && topLeft == bottomLeft && topLeft == bottomRight) {
            add(face, whole[face.ordinal()][topLeft], builder);
            return;
        }
        BakedQuad[][] faceCorners = corners[face.ordinal()];
        add(face, faceCorners[TOP_LEFT_CORNER][topLeft], builder);
        add(face, faceCorners[TOP_RIGHT_CORNER][topRight], builder);
        add(face, faceCorners[BOTTOM_LEFT_CORNER][bottomLeft], builder);
        add(face, faceCorners[BOTTOM_RIGHT_CORNER][bottomRight], builder);
    }

    @Override
    public int materialFlags() {
        return flags;
    }

    private void add(Direction face, BakedQuad quad, QuadCollection.Builder builder) {
        if (culled[face.ordinal()]) {
            builder.addCulledFace(face, quad);
        } else {
            builder.addUnculledFace(quad);
        }
    }

    private static int state(int connections, int corner) {
        boolean vertical = FaceConnections.has(connections, CORNER_VERTICAL[corner]);
        boolean horizontal = FaceConnections.has(connections, CORNER_HORIZONTAL[corner]);
        if (vertical && horizontal) {
            return FaceConnections.has(connections, CORNER_DIAGONAL[corner]) ? SURROUNDED : INNER_CORNER;
        }
        if (vertical) {
            return VERTICAL;
        }
        return horizontal ? HORIZONTAL : UNCONNECTED;
    }

    private static CuboidFace.UVs wholeUvs(int state) {
        if (state == UNCONNECTED) {
            return new CuboidFace.UVs(0.0F, 0.0F, ConnectedQuadBaker.FACE_SIZE, ConnectedQuadBaker.FACE_SIZE);
        }
        float minU = SHEET_TILE * SHEET_COLUMN[state];
        float minV = SHEET_TILE * SHEET_ROW[state];
        return new CuboidFace.UVs(minU, minV, minU + SHEET_TILE, minV + SHEET_TILE);
    }

    private static CuboidFace.UVs cornerUvs(int state, int corner) {
        if (state == UNCONNECTED) {
            float minU = HALF * column(corner);
            float minV = HALF * row(corner);
            return new CuboidFace.UVs(minU, minV, minU + HALF, minV + HALF);
        }
        float minU = SHEET_TILE * SHEET_COLUMN[state] + SHEET_QUARTER * column(corner);
        float minV = SHEET_TILE * SHEET_ROW[state] + SHEET_QUARTER * row(corner);
        return new CuboidFace.UVs(minU, minV, minU + SHEET_QUARTER, minV + SHEET_QUARTER);
    }

    private static int column(int corner) {
        return corner & 1;
    }

    private static int row(int corner) {
        return corner >> 1;
    }
}
