package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.taint.TaintSporeRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public final class TaintSporeModel extends EntityModel<TaintSporeRenderState> {
    private static final int TEXTURE_SIZE = 64;
    private static final float CALM_WOBBLE = 0.02F;
    private static final float HURT_WOBBLE = 0.04F;
    private static final float WOBBLE_X_RATE = 0.05F;
    private static final float WOBBLE_Z_RATE = 0.1F;

    private final ModelPart cube;

    public TaintSporeModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.cube = root.getChild("cube");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 12.0F, 12.0F).texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void setupAnim(TaintSporeRenderState state) {
        super.setupAnim(state);
        float wobble = state.hasRedOverlay ? HURT_WOBBLE : CALM_WOBBLE;
        cube.xRot = wobble * Mth.sin(state.ageInTicks * WOBBLE_X_RATE);
        cube.zRot = wobble * Mth.sin(state.ageInTicks * WOBBLE_Z_RATE);
    }
}
