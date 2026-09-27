package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.client.model.TCModelsHandlers;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import org.jspecify.annotations.Nullable;

public final class TCMeshLoaderBuilder extends CustomLoaderBuilder {
    private @Nullable Identifier mesh;
    private boolean flipV;
    private boolean cornerSpace;

    public TCMeshLoaderBuilder() {
        super(TCModelsHandlers.MESH_LOADER_ID, false);
    }

    public TCMeshLoaderBuilder mesh(Identifier mesh) {
        this.mesh = Objects.requireNonNull(mesh);
        return this;
    }

    public TCMeshLoaderBuilder flipV(boolean flipV) {
        this.flipV = flipV;
        return this;
    }

    public TCMeshLoaderBuilder cornerSpace(boolean cornerSpace) {
        this.cornerSpace = cornerSpace;
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        TCMeshLoaderBuilder copy = new TCMeshLoaderBuilder();
        copy.mesh = mesh;
        copy.flipV = flipV;
        copy.cornerSpace = cornerSpace;
        return copy;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject result = super.toJson(json);
        result.addProperty("model", Objects.requireNonNull(mesh, "mesh must be set").toString());
        if (flipV) {
            result.addProperty("flip_v", true);
        }
        if (cornerSpace) {
            result.addProperty("corner_space", true);
        }
        return result;
    }
}
