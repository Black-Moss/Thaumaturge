package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.taint.TaintSporeRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class TaintSporeSwarmerModel extends EntityModel<TaintSporeRenderState> {
    private static final int TEXTURE_SIZE = 64;

    public TaintSporeSwarmerModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    public static LayerDefinition createCoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
