package com.leclowndu93150.thaumcraft.client.model.obj;

import java.io.IOException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TCObjLoader {
    private TCObjLoader() {}

    public static MeshModel load(ResourceManager resources, Identifier location) throws IOException {
        return WavefrontObject.load(resources, location).model();
    }
}
