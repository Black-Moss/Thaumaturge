package com.leclowndu93150.thaumcraft.client.model.obj;

import java.util.ArrayList;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class MeshModel {
    private List<Vector3f> positions;
    private List<Vector3f> normals;
    private List<Vector2f> texCoords;
    private final List<MeshPart> parts = new ArrayList<>();

    public List<Vector3f> positions() { return positions; }
    public List<Vector3f> normals() { return normals; }
    public List<Vector2f> texCoords() { return texCoords; }
    public List<MeshPart> parts() { return parts; }

    public void addPosition(float x, float y, float z) {
        if (this.positions == null) {
            this.positions = new ArrayList<>();
        }
        this.positions.add(new Vector3f(x, y, z));
    }

    public void addNormal(float x, float y, float z) {
        if (this.normals == null) {
            this.normals = new ArrayList<>();
        }
        this.normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoords(float x, float y) {
        if (this.texCoords == null) {
            this.texCoords = new ArrayList<>();
        }
        this.texCoords.add(new Vector2f(x, y));
    }

    public void addPart(MeshPart part) {
        this.parts.add(part);
    }

    public void addPart(MeshPart part, int tintIndex) {
        this.parts.add(new MeshPart(part, tintIndex));
    }

    public MeshModel copy() {
        MeshModel mm = new MeshModel();
        for (MeshPart mp : this.parts) {
            mm.parts.add(mp);
        }
        if (this.positions != null) {
            mm.positions = new ArrayList<>();
            for (Vector3f mp : this.positions) {
                mm.positions.add(new Vector3f(mp));
            }
        }
        if (this.normals != null) {
            mm.normals = new ArrayList<>();
            for (Vector3f mp : this.normals) {
                mm.normals.add(new Vector3f(mp));
            }
        }
        if (this.texCoords != null) {
            mm.texCoords = new ArrayList<>();
            for (Vector2f mp : this.texCoords) {
                mm.texCoords.add(new Vector2f(mp));
            }
        }
        return mm;
    }

    public void rotate(double angle, Vector3fc axis, Vector3fc offset) {
        if (this.positions == null) return;
        Quaternionf rotation = new Quaternionf().fromAxisAngleRad(axis.x(), axis.y(), axis.z(), (float) angle);
        List<Vector3f> rotated = new ArrayList<>(this.positions.size());
        for (Vector3f v : this.positions) {
            Vector3f r = new Vector3f(v);
            rotation.transform(r);
            r.add(offset.x(), offset.y(), offset.z());
            rotated.add(r);
        }
        this.positions = rotated;
    }
}
