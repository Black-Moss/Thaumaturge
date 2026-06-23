package com.leclowndu93150.thaumcraft.client.model.obj;

import java.util.ArrayList;
import java.util.List;

public final class MeshPart {
    private String name;
    private Material material;
    private final List<int[]> indices;
    private int tintIndex = -1;

    public MeshPart() {
        this.indices = new ArrayList<>();
    }

    public MeshPart(MeshPart source, int tintIndex) {
        this.name = source.name;
        this.material = source.material;
        this.indices = source.indices;
        this.tintIndex = tintIndex;
    }

    public String name() { return name; }
    public Material material() { return material; }
    public List<int[]> indices() { return indices; }
    public int tintIndex() { return tintIndex; }

    public void setName(String name) { this.name = name; }
    public void setMaterial(Material material) { this.material = material; }
    public void setTintIndex(int tintIndex) { this.tintIndex = tintIndex; }

    public void addTriangleFace(int[] a, int[] b, int[] c) {
        this.indices.add(a);
        this.indices.add(b);
        this.indices.add(c);
        this.indices.add(c);
    }

    public void addQuadFace(int[] a, int[] b, int[] c, int[] d) {
        this.indices.add(a);
        this.indices.add(b);
        this.indices.add(c);
        this.indices.add(d);
    }
}
