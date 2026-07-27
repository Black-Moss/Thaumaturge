package com.leclowndu93150.thaumaturge.client.model.mesh;

import java.util.List;

public record TCMesh(List<TCMeshPart> parts) {
    public static final TCMesh EMPTY = new TCMesh(List.of());
}
