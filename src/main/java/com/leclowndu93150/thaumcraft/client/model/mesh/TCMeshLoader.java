package com.leclowndu93150.thaumcraft.client.model.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public final class TCMeshLoader {
    private static final String FORMAT_NAME = "thaumcraft_mesh";
    private static final int FORMAT_VERSION = 1;
    private static final int CORNERS_PER_QUAD = 4;
    private static final int POSITION_SIZE = 3;
    private static final int UV_SIZE = 2;
    private static final int NORMAL_SIZE = 3;

    private TCMeshLoader() {}

    public static TCMesh load(ResourceManager resources, Identifier location) throws IOException {
        JsonObject root;
        try (BufferedReader reader = resources.openAsReader(location)) {
            root = GsonHelper.parse(reader);
        } catch (JsonParseException e) {
            throw new IOException("Malformed mesh JSON in " + location, e);
        }
        try {
            return parse(root);
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("Invalid " + FORMAT_NAME + " file " + location, e);
        }
    }

    private static TCMesh parse(JsonObject root) {
        String format = GsonHelper.getAsString(root, "format");
        if (!FORMAT_NAME.equals(format)) {
            throw new IllegalStateException("Unknown mesh format '" + format + "'");
        }
        int version = GsonHelper.getAsInt(root, "version");
        if (version != FORMAT_VERSION) {
            throw new IllegalStateException("Unsupported " + FORMAT_NAME + " version " + version);
        }
        JsonArray partArray = GsonHelper.getAsJsonArray(root, "parts");
        List<TCMeshPart> parts = new ArrayList<>(partArray.size());
        for (int p = 0; p < partArray.size(); p++) {
            parts.add(parsePart(GsonHelper.convertToJsonObject(partArray.get(p), "part")));
        }
        return new TCMesh(List.copyOf(parts));
    }

    private static TCMeshPart parsePart(JsonObject part) {
        String name = GsonHelper.getAsString(part, "name", "");
        String slot = GsonHelper.getAsString(part, "texture_slot", "");
        JsonArray attributes = GsonHelper.getAsJsonArray(part, "attributes");
        int positionOffset = -1;
        int uvOffset = -1;
        int normalOffset = -1;
        int stride = 0;
        for (int i = 0; i < attributes.size(); i++) {
            String attribute = GsonHelper.convertToString(attributes.get(i), "attribute");
            switch (attribute) {
                case "position" -> {
                    positionOffset = stride;
                    stride += POSITION_SIZE;
                }
                case "uv" -> {
                    uvOffset = stride;
                    stride += UV_SIZE;
                }
                case "normal" -> {
                    normalOffset = stride;
                    stride += NORMAL_SIZE;
                }
                default -> throw new IllegalStateException("Unknown mesh attribute '" + attribute + "'");
            }
        }
        if (positionOffset < 0) {
            throw new IllegalStateException("Mesh part '" + name + "' has no position attribute");
        }
        JsonArray quads = GsonHelper.getAsJsonArray(part, "quads");
        int quadCount = quads.size();
        int vertexCount = quadCount * CORNERS_PER_QUAD;
        float[] positions = new float[vertexCount * POSITION_SIZE];
        float[] uvs = uvOffset >= 0 ? new float[vertexCount * UV_SIZE] : null;
        float[] normals = normalOffset >= 0 ? new float[vertexCount * NORMAL_SIZE] : null;
        int floatsPerQuad = stride * CORNERS_PER_QUAD;
        for (int q = 0; q < quadCount; q++) {
            JsonArray values = GsonHelper.convertToJsonArray(quads.get(q), "quad");
            if (values.size() != floatsPerQuad) {
                throw new IllegalStateException(
                        "Mesh part '" + name + "' quad " + q + " has " + values.size() + " floats, expected " + floatsPerQuad);
            }
            for (int corner = 0; corner < CORNERS_PER_QUAD; corner++) {
                int vertex = q * CORNERS_PER_QUAD + corner;
                int base = corner * stride;
                for (int c = 0; c < POSITION_SIZE; c++) {
                    positions[vertex * POSITION_SIZE + c] = values.get(base + positionOffset + c).getAsFloat();
                }
                if (uvs != null) {
                    for (int c = 0; c < UV_SIZE; c++) {
                        uvs[vertex * UV_SIZE + c] = values.get(base + uvOffset + c).getAsFloat();
                    }
                }
                if (normals != null) {
                    for (int c = 0; c < NORMAL_SIZE; c++) {
                        normals[vertex * NORMAL_SIZE + c] = values.get(base + normalOffset + c).getAsFloat();
                    }
                }
            }
        }
        return new TCMeshPart(name, slot, quadCount, positions, uvs, normals);
    }
}
