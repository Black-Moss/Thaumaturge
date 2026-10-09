package com.leclowndu93150.thaumaturge.client.casters.architect;

import net.minecraft.core.Direction;

public final class FaceFrame {
    private static final Direction[] AXIS_X = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.DOWN};
    private static final Direction[] AXIS_Y = {Direction.EAST, Direction.EAST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH};
    private static final int QUADRANT_COUNT = 4;
    private static final int ART_QUADRANT = 3;
    private static final float QUADRANT_DEGREES = 90.0F;
    private static final FaceFrame[] FRAMES = build();

    private final Direction face;
    private final Direction xAxis;
    private final Direction yAxis;
    private final CubeCorner[] corners;
    private final float[] cornerDegrees;

    private FaceFrame(Direction face, Direction xAxis, Direction yAxis) {
        this.face = face;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        int count = 0;
        CubeCorner[] found = new CubeCorner[CubeCorner.COUNT];
        float[] degrees = new float[CubeCorner.COUNT];
        for (int index = 0; index < CubeCorner.COUNT; index++) {
            CubeCorner corner = CubeCorner.get(index);
            if (corner.signAlong(face) > 0) {
                found[count] = corner;
                degrees[count] = rotationFor(corner.signAlong(xAxis), corner.signAlong(yAxis));
                count++;
            }
        }
        this.corners = new CubeCorner[count];
        this.cornerDegrees = new float[count];
        System.arraycopy(found, 0, corners, 0, count);
        System.arraycopy(degrees, 0, cornerDegrees, 0, count);
    }

    private static FaceFrame[] build() {
        Direction[] faces = Direction.values();
        FaceFrame[] frames = new FaceFrame[faces.length];
        for (Direction face : faces) {
            frames[face.ordinal()] = new FaceFrame(face, AXIS_X[face.ordinal()], AXIS_Y[face.ordinal()]);
        }
        return frames;
    }

    private static int quadrant(int u, int v) {
        if (v > 0) {
            return u > 0 ? 0 : 1;
        }
        return u < 0 ? 2 : 3;
    }

    private static float rotationFor(int u, int v) {
        return Math.floorMod(quadrant(u, v) - ART_QUADRANT, QUADRANT_COUNT) * QUADRANT_DEGREES;
    }

    public static FaceFrame of(Direction face) {
        return FRAMES[face.ordinal()];
    }

    public Direction face() {
        return face;
    }

    public Direction xAxis() {
        return xAxis;
    }

    public Direction yAxis() {
        return yAxis;
    }

    public int cornerCount() {
        return corners.length;
    }

    public CubeCorner corner(int slot) {
        return corners[slot];
    }

    public float cornerRotation(int slot) {
        return cornerDegrees[slot];
    }
}
