package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthLandmarks;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.StateOrientation;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public record LockMarker(Direction facing) implements LabyrinthMarker {
    public static final MapCodec<LockMarker> CODEC = Direction.CODEC.fieldOf("facing").xmap(LockMarker::new, LockMarker::facing);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TCLabyrinthMarkers.LOCK.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public Optional<Identifier> landmark() {
        return Optional.of(LabyrinthLandmarks.BOSS_DOOR);
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        context.placeEntity(pos, StateOrientation.face(TCBlocks.ELDRITCH_LOCK.get().defaultBlockState(), context.orient(facing)), TCBlockEntities.ELDRITCH_LOCK.get())
                .ifPresent(lock -> lock.bind(context.maze()));
    }
}
