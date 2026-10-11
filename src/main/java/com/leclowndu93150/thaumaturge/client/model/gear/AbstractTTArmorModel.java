package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.Rotations;

public abstract class AbstractTTArmorModel extends HumanoidModel<HumanoidRenderState> {
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

    protected AbstractTTArmorModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ZombieRenderState zombie) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive, zombie);
        }
        if (state instanceof ArmorStandRenderState stand) {
            applyStandPose(stand);
        }
    }

    protected static MeshDefinition emptyMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    protected static PartDefinition emptyChain(PartDefinition parent, String... names) {
        PartDefinition part = parent;
        for (String name : names) {
            part = part.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
        }
        return part;
    }

    private void applyStandPose(ArmorStandRenderState stand) {
        setRotation(this.head, stand.headPose);
        setRotation(this.body, stand.bodyPose);
        setRotation(this.leftArm, stand.leftArmPose);
        setRotation(this.rightArm, stand.rightArmPose);
        setRotation(this.leftLeg, stand.leftLegPose);
        setRotation(this.rightLeg, stand.rightLegPose);
    }

    private static void setRotation(ModelPart part, Rotations rotations) {
        part.xRot = DEGREES_TO_RADIANS * rotations.x();
        part.yRot = DEGREES_TO_RADIANS * rotations.y();
        part.zRot = DEGREES_TO_RADIANS * rotations.z();
    }
}
