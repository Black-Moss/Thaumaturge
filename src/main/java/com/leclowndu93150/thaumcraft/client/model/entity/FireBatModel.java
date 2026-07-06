package com.leclowndu93150.thaumcraft.client.model.entity;

import com.leclowndu93150.thaumcraft.client.entity.FireBatRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import java.util.function.Function;

public final class FireBatModel extends EntityModel<FireBatRenderState> {
    private static final int TEXTURE_SIZE = 64;
    private static final float HANGING_WING_X = (float) (-Math.PI / 20);
    private static final float HANGING_WING_Y = (float) (-Math.PI * 2.0 / 5.0);
    private static final float HANGING_OUTER_WING_Y = -1.7278761F;
    private static final float FLYING_BODY_X = (float) (Math.PI / 4);

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWingTip;

    public FireBatModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
    }

    public FireBatModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWingTip = this.rightWing.getChild("right_wing_tip");
        this.leftWingTip = this.leftWing.getChild("left_wing_tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(24, 0).addBox(-4.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(24, 0).mirror().addBox(1.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                        .texOffs(0, 34).addBox(-5.0F, 16.0F, 0.0F, 10.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(42, 0).addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F),
                PartPose.ZERO);
        rightWing.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create().texOffs(24, 16).addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F),
                PartPose.offset(-12.0F, 1.0F, 1.5F));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(42, 0).mirror().addBox(2.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F),
                PartPose.ZERO);
        leftWing.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create().texOffs(24, 16).mirror().addBox(0.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F),
                PartPose.offset(12.0F, 1.0F, 1.5F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void setupAnim(FireBatRenderState state) {
        super.setupAnim(state);
        if (state.hanging) {
            this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);
            this.head.yRot = (float) Math.PI - state.yRot * ((float) Math.PI / 180.0F);
            this.head.zRot = (float) Math.PI;
            this.head.setPos(0.0F, -2.0F, 0.0F);
            this.rightWing.setPos(-3.0F, 0.0F, 3.0F);
            this.leftWing.setPos(3.0F, 0.0F, 3.0F);
            this.body.xRot = (float) Math.PI;
            this.rightWing.xRot = HANGING_WING_X;
            this.rightWing.yRot = HANGING_WING_Y;
            this.rightWingTip.yRot = HANGING_OUTER_WING_Y;
            this.leftWing.xRot = HANGING_WING_X;
            this.leftWing.yRot = -HANGING_WING_Y;
            this.leftWingTip.yRot = -HANGING_OUTER_WING_Y;
        } else {
            this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);
            this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
            this.head.zRot = 0.0F;
            this.head.setPos(0.0F, 0.0F, 0.0F);
            this.rightWing.setPos(0.0F, 0.0F, 0.0F);
            this.leftWing.setPos(0.0F, 0.0F, 0.0F);
            this.body.xRot = FLYING_BODY_X + Mth.cos(state.ageInTicks * 0.1F) * 0.15F;
            this.body.yRot = 0.0F;
            this.rightWing.yRot = Mth.cos(state.ageInTicks * 1.3F) * (float) Math.PI * 0.25F;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.rightWingTip.yRot = this.rightWing.yRot * 0.5F;
            this.leftWingTip.yRot = -this.rightWing.yRot * 0.5F;
        }
    }
}
