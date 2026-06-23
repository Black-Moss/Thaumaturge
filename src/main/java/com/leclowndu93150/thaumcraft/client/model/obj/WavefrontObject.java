package com.leclowndu93150.thaumcraft.client.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class WavefrontObject {
    private final MeshModel model = new MeshModel();
    private final MaterialLibrary materialLibrary = new MaterialLibrary();
    private MeshPart currentPart;
    private String lastObjectName;
    private final String fileName;

    private WavefrontObject(String fileName) {
        this.fileName = fileName;
    }

    public static WavefrontObject load(ResourceManager resources, Identifier location) throws IOException {
        WavefrontObject result = new WavefrontObject(location.toString());
        Resource resource = resources.getResourceOrThrow(location);
        try (InputStream stream = resource.open()) {
            result.parse(resources, location, stream);
        }
        return result;
    }

    public static WavefrontObject load(String fileName, InputStream stream) throws IOException {
        WavefrontObject result = new WavefrontObject(fileName);
        result.parse(null, null, stream);
        return result;
    }

    public MeshModel model() {
        return model;
    }

    public MaterialLibrary materialLibrary() {
        return materialLibrary;
    }

    public String fileName() {
        return fileName;
    }

    private void parse(ResourceManager resources, Identifier objLocation, InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.length() == 0 || currentLine.startsWith("#")) {
                    continue;
                }
                if (currentLine.startsWith("v  ")) {
                    currentLine = currentLine.replaceFirst("v  ", "v ");
                }
                String[] fields = currentLine.split(" ", 2);
                if (fields.length < 2) {
                    continue;
                }
                String keyword = fields[0];
                String data = fields[1];
                if (keyword.equalsIgnoreCase("o") || keyword.equalsIgnoreCase("g")) {
                    this.lastObjectName = data;
                } else if (keyword.equalsIgnoreCase("mtllib")) {
                    if (resources != null && objLocation != null) {
                        loadMaterialLibrary(resources, objLocation, data);
                    }
                } else if (keyword.equalsIgnoreCase("usemtl")) {
                    useMaterial(data);
                } else if (keyword.equalsIgnoreCase("v")) {
                    addPosition(data);
                } else if (keyword.equalsIgnoreCase("vn")) {
                    addNormal(data);
                } else if (keyword.equalsIgnoreCase("vt")) {
                    addTexCoord(data);
                } else if (keyword.equalsIgnoreCase("f")) {
                    addFace(data);
                }
            }
        }
    }

    private void addPosition(String line) {
        String[] args = line.split(" ");
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = Float.parseFloat(args[2]);
        this.model.addPosition(x, y, z);
    }

    private void addNormal(String line) {
        String[] args = line.split(" ");
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = args[2].equals("\\\\") ? (float) Math.sqrt(1.0F - x * x - y * y) : Float.parseFloat(args[2]);
        this.model.addNormal(x, y, z);
    }

    private void addTexCoord(String line) {
        String[] args = line.split(" ");
        float u = Float.parseFloat(args[0]);
        float v = Float.parseFloat(args[1]);
        this.model.addTexCoords(u, v);
    }

    private void addFace(String line) {
        String[] args = line.split(" ");
        if (args.length < 3 || args.length > 4) {
            throw new IllegalStateException("OBJ face with unsupported vertex count " + args.length + " in " + fileName);
        }
        ensurePart();
        int[] v1 = parseIndices(args[0].split("/"));
        int[] v2 = parseIndices(args[1].split("/"));
        int[] v3 = parseIndices(args[2].split("/"));
        if (args.length == 3) {
            this.currentPart.addTriangleFace(v1, v2, v3);
        } else {
            int[] v4 = parseIndices(args[3].split("/"));
            this.currentPart.addQuadFace(v1, v2, v3, v4);
        }
    }

    private int[] parseIndices(String[] tokens) {
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = tokens[i].isEmpty() ? -1 : Integer.parseInt(tokens[i]) - 1;
        }
        return indices;
    }

    private void useMaterial(String matName) {
        Material mat = this.materialLibrary.get(matName);
        this.currentPart = new MeshPart();
        this.currentPart.setName(this.lastObjectName);
        this.currentPart.setMaterial(mat);
        this.model.addPart(this.currentPart);
    }

    private void ensurePart() {
        if (this.currentPart == null) {
            this.currentPart = new MeshPart();
            this.currentPart.setName(this.lastObjectName);
            this.model.addPart(this.currentPart);
        }
    }

    private void loadMaterialLibrary(ResourceManager resources, Identifier parent, String path) throws IOException {
        String prefix = parent.getPath();
        int slash = prefix.lastIndexOf('/');
        prefix = slash >= 0 ? prefix.substring(0, slash + 1) : "";
        Identifier mtlLocation = Identifier.fromNamespaceAndPath(parent.getNamespace(), prefix + path);
        this.materialLibrary.loadFromResource(resources, mtlLocation);
    }
}
