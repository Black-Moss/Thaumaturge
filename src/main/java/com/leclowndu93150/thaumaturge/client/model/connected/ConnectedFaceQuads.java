package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;

public interface ConnectedFaceQuads {
    boolean connects(Direction face);

    void addFace(Direction face, int connections, QuadCollection.Builder builder);

    @BakedQuad.MaterialFlags
    int materialFlags();
}
