package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class MatrixCubeModel {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;
    private static final int GLOW_V = 32;
    private static final float EDGE = 16.0F;
    private static final float HALF = EDGE / 2.0F;

    public final ModelPart cube;
    public final ModelPart glow;

    public MatrixCubeModel(ModelPart root) {
        this.cube = root.getChild("cube");
        this.glow = root.getChild("glow");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        attachFace(mesh.getRoot(), "cube", 0);
        attachFace(mesh.getRoot(), "glow", GLOW_V);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static void attachFace(PartDefinition parent, String name, int v) {
        CubeListBuilder shape = CubeListBuilder.create().mirror().texOffs(0, v);
        shape.addBox(-HALF, -HALF, -HALF, EDGE, EDGE, EDGE);
        parent.addOrReplaceChild(name, shape, PartPose.ZERO);
    }
}
