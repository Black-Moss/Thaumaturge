package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class CrimsonBootsModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static final CubeDeformation FIT = new CubeDeformation(0.25F);

    public CrimsonBootsModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createFeet() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, FIT), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, FIT), PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
