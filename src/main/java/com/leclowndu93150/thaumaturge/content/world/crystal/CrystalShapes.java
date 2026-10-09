package com.leclowndu93150.thaumaturge.content.world.crystal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class CrystalShapes {
    private static final int MAX_CACHED_SHAPES = 4096;
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final double GRID = 32.0;
    private static final double HALF = 0.5;
    private static final double[][][] SHARD_BOXES = {{{6, 0, 5.5, 10, 7.5, 9}, {7, 0, 9, 9.5, 6, 10}, {7, 6.5, 7, 9, 8, 8.5}},
            {{12, 0, 5.5, 15, 4.5, 7.5}, {13, 0, 7.5, 14, 3.5, 8}, {13, 2, 5, 13.5, 3, 5.5}}, {{2, 0, 10.5, 4.5, 3.5, 12}, {2, 0, 12, 4.5, 3, 12.5}, {2.5, 3, 11, 4, 4.5, 12.5}},
            {{9, 0, 1.5, 12, 6.5, 3.5}, {10, 0, 3.5, 11.5, 5, 4.5}, {10, 2.5, 1, 10.5, 5, 1.5}}, {{4.5, 0, 2, 7.5, 4.5, 3.5}, {5, 0, 1, 7, 4, 2}, {5, 0, 3, 7, 4, 4}},
            {{11.5, 0, 9.5, 14, 5, 12.5}, {12.5, 5, 10.5, 13.5, 6, 11.5}, {14, 2.5, 10, 14.5, 4, 10.5}}, {{6.5, 0, 12, 9.5, 4, 14.5}, {7, 3.5, 12.5, 9, 5.5, 14.5}, {7.5, 1.5, 14.5, 9, 4.5, 15}},
            {{1, 0, 5, 4, 6, 7}, {1.5, 1.5, 4.5, 3, 4, 5}, {2, 0.5, 7, 3.5, 4, 7.5}}};
    private static final Direction[] FACES = Direction.values();
    private static final VoxelShape[][] SHARD_SHAPES = buildShardShapes();
    private static final Map<Long, VoxelShape> CACHE = new ConcurrentHashMap<>();

    private CrystalShapes() {}

    static VoxelShape shapeFor(BlockState state, BlockGetter level, BlockPos pos) {
        long seed = CrystalShards.seed(state, pos);
        int perFace = state.getValue(BlockCrystal.SIZE) + 1;
        long key = 0L;
        boolean supported = false;
        for (Direction face : FACES) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            supported = true;
            List<Integer> order = CrystalShards.order(face, seed);
            for (int index = 0; index < perFace; index++) {
                key |= bit(face, order.get(index));
            }
        }
        if (!supported) {
            key = bit(Direction.DOWN, CrystalShards.unsupported(seed));
        }
        VoxelShape cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        VoxelShape built = compose(key);
        if (CACHE.size() < MAX_CACHED_SHAPES) {
            CACHE.putIfAbsent(key, built);
        }
        return built;
    }

    private static long bit(Direction face, int shard) {
        return 1L << (face.ordinal() * CrystalShards.COUNT + shard);
    }

    private static VoxelShape compose(long key) {
        VoxelShape shape = Shapes.empty();
        for (Direction face : FACES) {
            for (int shard = 0; shard < CrystalShards.COUNT; shard++) {
                if ((key & bit(face, shard)) != 0L) {
                    shape = Shapes.joinUnoptimized(shape, SHARD_SHAPES[face.ordinal()][shard], BooleanOp.OR);
                }
            }
        }
        return shape.optimize();
    }

    private static VoxelShape[][] buildShardShapes() {
        VoxelShape[][] shapes = new VoxelShape[FACES.length][CrystalShards.COUNT];
        for (Direction face : FACES) {
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            for (int shard = 0; shard < CrystalShards.COUNT; shard++) {
                VoxelShape union = Shapes.empty();
                for (double[] box : SHARD_BOXES[shard]) {
                    union = Shapes.joinUnoptimized(union, transformedBox(box, transform), BooleanOp.OR);
                }
                shapes[face.ordinal()][shard] = union.optimize();
            }
        }
        return shapes;
    }

    private static VoxelShape transformedBox(double[] box, Matrix4f transform) {
        Vector3f low = transform.transformPosition(scaled(box[0], box[1], box[2]), new Vector3f());
        Vector3f high = transform.transformPosition(scaled(box[3], box[4], box[5]), new Vector3f());
        return Shapes.box(snap(Math.min(low.x, high.x)), snap(Math.min(low.y, high.y)), snap(Math.min(low.z, high.z)), snap(Math.max(low.x, high.x)), snap(Math.max(low.y, high.y)),
                snap(Math.max(low.z, high.z)));
    }

    private static Vector3f scaled(double x, double y, double z) {
        return new Vector3f((float) x / PIXELS_PER_BLOCK, (float) y / PIXELS_PER_BLOCK, (float) z / PIXELS_PER_BLOCK);
    }

    private static double snap(float value) {
        return Math.floor(value * GRID + HALF) / GRID;
    }
}
