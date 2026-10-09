package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class MultiblockMatcher {
    private static final List<Direction> ORIENTATIONS = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final List<Direction> MATCH_ORDER = List.of(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final int NO_INDEX = 0;
    private static final int NO_TURNS = 0;
    private static final int NORTH_TURNS = 1;
    private static final int WEST_TURNS = 2;
    private static final int SOUTH_TURNS = 3;

    private MultiblockMatcher() {}

    public static @Nullable DustTriggerPlacement find(Level level, BlockPos clicked, Blueprint blueprint) {
        List<List<BlueprintCell>> cellsByOrientation = new ArrayList<>(MATCH_ORDER.size());
        for (Direction orientation : MATCH_ORDER) {
            cellsByOrientation.add(new RotatedBlueprint(blueprint, rotationsFor(orientation)).cells());
        }
        int span = Math.max(blueprint.xSize(), blueprint.zSize());
        MutableBlockPos origin = new MutableBlockPos();
        MutableBlockPos probe = new MutableBlockPos();
        for (int dy = -blueprint.ySize(); dy <= 0; dy++) {
            for (int dx = -span; dx <= 0; dx++) {
                for (int dz = -span; dz <= 0; dz++) {
                    origin.set(clicked.getX() + dx, clicked.getY() + dy, clicked.getZ() + dz);
                    for (int index = 0; index < MATCH_ORDER.size(); index++) {
                        if (fits(level, origin, cellsByOrientation.get(index), probe)) {
                            return new DustTriggerPlacement(dx, dy, dz, MATCH_ORDER.get(index));
                        }
                    }
                }
            }
        }
        return null;
    }

    public static int horizontalIndex(Direction direction) {
        int index = ORIENTATIONS.indexOf(direction);
        return index < 0 ? NO_INDEX : index;
    }

    public static int rotationsFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_TURNS;
            case WEST -> WEST_TURNS;
            case SOUTH -> SOUTH_TURNS;
            case EAST, UP, DOWN -> NO_TURNS;
        };
    }

    private static boolean fits(Level level, BlockPos origin, List<BlueprintCell> cells, MutableBlockPos probe) {
        for (BlueprintCell cell : cells) {
            probe.setWithOffset(origin, cell.offset());
            if (!cell.part().source().matches(level.getBlockState(probe))) {
                return false;
            }
        }
        return true;
    }
}
