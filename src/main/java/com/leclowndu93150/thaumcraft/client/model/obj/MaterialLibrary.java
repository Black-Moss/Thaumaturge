package com.leclowndu93150.thaumcraft.client.model.obj;

import com.leclowndu93150.thaumcraft.Thaumcraft;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;

public final class MaterialLibrary {
    private static final Set<String> UNKNOWN_COMMANDS = new HashSet<>();

    private final Map<String, Material> materials = new HashMap<>();
    private Material currentMaterial;

    public int size() {
        return this.materials.size();
    }

    public boolean isEmpty() {
        return this.materials.isEmpty();
    }

    public Material get(String key) {
        return this.materials.get(key);
    }

    public Map<String, Material> all() {
        return this.materials;
    }

    public void loadFromResource(ResourceManager resources, Identifier location) throws IOException {
        Resource resource = resources.getResourceOrThrow(location);
        try (InputStream stream = resource.open()) {
            loadFromStream(stream);
        }
    }

    public void loadFromStream(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.length() == 0 || currentLine.startsWith("#")) {
                    continue;
                }
                String[] fields = currentLine.split(" ", 2);
                if (fields.length < 2) {
                    continue;
                }
                String keyword = fields[0];
                String data = fields[1];
                if (keyword.equalsIgnoreCase("newmtl")) {
                    pushMaterial(data);
                } else if (keyword.equalsIgnoreCase("Ka")) {
                    this.currentMaterial.setAmbientColor(parseVector3f(data));
                } else if (keyword.equalsIgnoreCase("Kd")) {
                    this.currentMaterial.setDiffuseColor(parseVector3f(data));
                } else if (keyword.equalsIgnoreCase("Ks")) {
                    this.currentMaterial.setSpecularColor(parseVector3f(data));
                } else if (keyword.equalsIgnoreCase("Ns")) {
                    this.currentMaterial.setSpecularCoefficient(parseFloat(data));
                } else if (keyword.equalsIgnoreCase("Tr")) {
                    this.currentMaterial.setTransparency(parseFloat(data));
                } else if (keyword.equalsIgnoreCase("illum")) {
                    this.currentMaterial.setIlluminationModel(parseInt(data));
                } else if (keyword.equalsIgnoreCase("map_Ka")) {
                    this.currentMaterial.setAmbientTextureMap(data);
                } else if (keyword.equalsIgnoreCase("map_Kd")) {
                    this.currentMaterial.setDiffuseTextureMap(data);
                } else if (keyword.equalsIgnoreCase("map_Ks")) {
                    this.currentMaterial.setSpecularTextureMap(data);
                } else if (keyword.equalsIgnoreCase("map_Ns")) {
                    this.currentMaterial.setSpecularHighlightTextureMap(data);
                } else if (keyword.equalsIgnoreCase("map_d")) {
                    this.currentMaterial.setAlphaTextureMap(data);
                } else if (keyword.equalsIgnoreCase("map_bump") || keyword.equalsIgnoreCase("bump")) {
                    this.currentMaterial.setBumpMap(data);
                } else if (keyword.equalsIgnoreCase("disp")) {
                    this.currentMaterial.setDisplacementMap(data);
                } else if (keyword.equalsIgnoreCase("decal")) {
                    this.currentMaterial.setStencilDecalMap(data);
                } else if (UNKNOWN_COMMANDS.add(keyword)) {
                    Thaumcraft.LOGGER.debug("Unknown MTL command: {}", keyword);
                }
            }
        }
    }

    private float parseFloat(String data) {
        return Float.parseFloat(data);
    }

    private int parseInt(String data) {
        return Integer.parseInt(data);
    }

    private Vector3f parseVector3f(String data) {
        String[] parts = data.split(" ");
        return new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
    }

    private void pushMaterial(String materialName) {
        this.currentMaterial = new Material(materialName);
        this.materials.put(materialName, this.currentMaterial);
    }
}
