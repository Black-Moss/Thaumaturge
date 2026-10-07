package com.leclowndu93150.thaumaturge.client.warding;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

public final class WardedGlassBakedModel implements DynamicBlockStateModel {
    public static final int TILE_COUNT = 47;

    private static final int TOP = 1;
    private static final int RIGHT = 2;
    private static final int BOTTOM = 4;
    private static final int LEFT = 8;
    private static final int TOP_LEFT = 1;
    private static final int TOP_RIGHT = 2;
    private static final int BOTTOM_RIGHT = 4;
    private static final int BOTTOM_LEFT = 8;
    private static final int ALL_SIDES = TOP | RIGHT | BOTTOM | LEFT;
    private static final int ALL_CORNERS = TOP_LEFT | TOP_RIGHT | BOTTOM_RIGHT | BOTTOM_LEFT;
    private static final int NOTCH_SHIFT = 4;
    private static final int QUARTER_TURNS = 4;
    private static final Direction[] FACES = Direction.values();
    private static final int[] TILES = buildTiles();

    private final BakedQuad[][] quads;
    private final Material.Baked particle;
    private final int flags;

    public WardedGlassBakedModel(BakedQuad[][] quads, Material.Baked particle, int flags) {
        this.quads = quads;
        this.particle = particle;
        this.flags = flags;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (Direction face : FACES) {
            builder.addCulledFace(face, quads[face.ordinal()][TILES[pattern(level, pos, state, face)]]);
        }
        parts.add(new SimpleModelWrapper(builder.build(), true, particle));
    }

    private static int pattern(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction face) {
        Direction up = textureUp(face);
        Direction left = textureLeft(face);
        boolean upGlass = connects(level, pos.relative(up), state);
        boolean downGlass = connects(level, pos.relative(up.getOpposite()), state);
        boolean leftGlass = connects(level, pos.relative(left), state);
        boolean rightGlass = connects(level, pos.relative(left.getOpposite()), state);
        int sides = (upGlass ? 0 : TOP) | (rightGlass ? 0 : RIGHT) | (downGlass ? 0 : BOTTOM) | (leftGlass ? 0 : LEFT);
        int notches = 0;
        if (upGlass && leftGlass && !connects(level, pos.relative(up).relative(left), state)) {
            notches |= TOP_LEFT;
        }
        if (upGlass && rightGlass && !connects(level, pos.relative(up).relative(left.getOpposite()), state)) {
            notches |= TOP_RIGHT;
        }
        if (downGlass && rightGlass && !connects(level, pos.relative(up.getOpposite()).relative(left.getOpposite()), state)) {
            notches |= BOTTOM_RIGHT;
        }
        if (downGlass && leftGlass && !connects(level, pos.relative(up.getOpposite()).relative(left), state)) {
            notches |= BOTTOM_LEFT;
        }
        return sides | notches << NOTCH_SHIFT;
    }

    private static boolean connects(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos).is(state.getBlock());
    }

    private static Direction textureUp(Direction face) {
        return switch (face) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.SOUTH;
            default -> Direction.UP;
        };
    }

    private static Direction textureLeft(Direction face) {
        return switch (face) {
            case UP, DOWN, SOUTH -> Direction.WEST;
            case NORTH -> Direction.EAST;
            case WEST -> Direction.NORTH;
            case EAST -> Direction.SOUTH;
        };
    }

    private static int[] buildTiles() {
        int[] tiles = new int[1 << (2 * NOTCH_SHIFT)];
        int[][] files = {{ALL_SIDES, 0}, {TOP | BOTTOM | LEFT, 0}, {TOP | BOTTOM, 0}, {TOP | BOTTOM | RIGHT, 0}, {TOP | LEFT | RIGHT, 0}, {LEFT | RIGHT, 0}, {BOTTOM | LEFT | RIGHT, 0},
                {TOP | LEFT, 0}, {TOP, 0}, {TOP | RIGHT, 0}, {LEFT, 0}, {0, 0}, {RIGHT, 0}, {BOTTOM | LEFT, 0}, {BOTTOM, 0}, {BOTTOM | RIGHT, 0}, {TOP | LEFT, BOTTOM_RIGHT},
                {TOP | RIGHT, BOTTOM_LEFT}, {BOTTOM | LEFT, TOP_RIGHT}, {BOTTOM | RIGHT, TOP_LEFT}};
        int next = 0;
        for (int[] file : files) {
            tiles[file[0] | file[1] << NOTCH_SHIFT] = next++;
        }
        for (int[] start : new int[][]{{LEFT, TOP_RIGHT | BOTTOM_RIGHT}, {LEFT, TOP_RIGHT}, {LEFT, BOTTOM_RIGHT}}) {
            for (int turn = 0; turn < QUARTER_TURNS; turn++) {
                tiles[rotate(start[0], turn) | rotate(start[1], turn) << NOTCH_SHIFT] = next++;
            }
        }
        for (int[] start : new int[][]{{BOTTOM_RIGHT}, {TOP_LEFT | TOP_RIGHT}}) {
            for (int turn = 0; turn < QUARTER_TURNS; turn++) {
                tiles[rotate(start[0], turn) << NOTCH_SHIFT] = next++;
            }
        }
        tiles[(TOP_LEFT | BOTTOM_RIGHT) << NOTCH_SHIFT] = next++;
        tiles[(TOP_RIGHT | BOTTOM_LEFT) << NOTCH_SHIFT] = next++;
        for (int turn = 0; turn < QUARTER_TURNS; turn++) {
            tiles[(ALL_CORNERS & ~rotate(TOP_LEFT, turn)) << NOTCH_SHIFT] = next++;
        }
        tiles[ALL_CORNERS << NOTCH_SHIFT] = next;
        return tiles;
    }

    private static int rotate(int bits, int turns) {
        int shifted = bits << turns;
        return (shifted | shifted >> QUARTER_TURNS) & ALL_SIDES;
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return flags;
    }
}
