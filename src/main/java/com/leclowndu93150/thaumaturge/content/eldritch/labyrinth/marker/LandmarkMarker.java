package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record LandmarkMarker(Identifier id) implements LabyrinthMarker {
    public static final MapCodec<LandmarkMarker> CODEC = Identifier.CODEC.fieldOf("id").xmap(LandmarkMarker::new, LandmarkMarker::id);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.LANDMARK.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.LANDMARK;
    }

    @Override
    public Optional<Identifier> landmark() {
        return Optional.of(id);
    }
}
