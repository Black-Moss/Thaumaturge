package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class JarBrineModel {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static final int BRINE_U = 0;
    private static final int BRINE_V = 0;
    private static final float BRINE_HALF_WIDTH = 4.0F;
    private static final float BRINE_TOP = -11.6F;
    private static final float BRINE_WIDTH = 8.0F;
    private static final float BRINE_HEIGHT = 11.0F;

    public final ModelPart root;

    public JarBrineModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("brine", CubeListBuilder.create().texOffs(BRINE_U, BRINE_V).addBox(-BRINE_HALF_WIDTH, BRINE_TOP, -BRINE_HALF_WIDTH, BRINE_WIDTH, BRINE_HEIGHT, BRINE_WIDTH),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
