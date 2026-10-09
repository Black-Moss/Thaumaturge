package com.leclowndu93150.thaumaturge.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;

/**
 * Geometry of a seal's work area.
 *
 * <p>The area starts at the block in front of the seal. Along the axis the seal faces it runs as deep as the area size on
 * that axis; along the other two axes it spreads {@code size - 1} blocks to each side of the seal's block.
 *
 * @since 1.0.0
 */
public final class SealArea {
    private static final int SIDE_SPREAD_FACTOR = 2;
    private static final int FIRST_STEP_OFFSET = 1;

    private SealArea() {}

    /**
     * Maps a scan counter to a cell of the area. The counter wraps with a period equal to the cell count; the Z offset
     * changes fastest, then X, then Y.
     *
     * @param seal  the seal
     * @param index the scan counter, zero or greater
     * @return the cell, as a position in the level
     */
    public static BlockPos cell(ISealEntity seal, int index) {
        SealPos placement = seal.pos();
        BlockPos size = seal.area();
        Axis faceAxis = placement.face().getAxis();
        int spanX = span(size, faceAxis, Axis.X);
        int spanY = span(size, faceAxis, Axis.Y);
        int spanZ = span(size, faceAxis, Axis.Z);
        int wrapped = Math.floorMod(index, spanX * spanY * spanZ);
        int stepZ = wrapped % spanZ;
        int stepX = wrapped / spanZ % spanX;
        int stepY = wrapped / (spanZ * spanX) % spanY;
        return placement.pos().offset(offset(size, placement.face(), Axis.X, stepX), offset(size, placement.face(), Axis.Y, stepY), offset(size, placement.face(), Axis.Z, stepZ));
    }

    /**
     * Computes the box that encloses every cell of the area.
     *
     * @param seal the seal
     * @return the box, in level coordinates
     */
    public static AABB bounds(ISealEntity seal) {
        SealPos placement = seal.pos();
        BlockPos size = seal.area();
        BlockPos origin = placement.pos();
        Direction face = placement.face();
        return new AABB(origin.getX() + low(size, face, Axis.X), origin.getY() + low(size, face, Axis.Y), origin.getZ() + low(size, face, Axis.Z), origin.getX() + high(size, face, Axis.X),
                origin.getY() + high(size, face, Axis.Y), origin.getZ() + high(size, face, Axis.Z));
    }

    private static int span(BlockPos size, Axis faceAxis, Axis axis) {
        int extent = size.get(axis);
        return axis == faceAxis ? extent : SIDE_SPREAD_FACTOR * extent - 1;
    }

    private static int offset(BlockPos size, Direction face, Axis axis, int step) {
        if (axis == face.getAxis()) {
            return face.getAxisDirection().getStep() * (step + FIRST_STEP_OFFSET);
        }
        return step - (size.get(axis) - 1);
    }

    private static int low(BlockPos size, Direction face, Axis axis) {
        if (axis != face.getAxis()) {
            return -(size.get(axis) - 1);
        }
        return face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? FIRST_STEP_OFFSET : -size.get(axis);
    }

    private static int high(BlockPos size, Direction face, Axis axis) {
        if (axis != face.getAxis()) {
            return size.get(axis);
        }
        return face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? size.get(axis) + FIRST_STEP_OFFSET : 0;
    }
}
