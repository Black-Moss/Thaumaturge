package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public final class ClericArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart frontFlap;
    private final ModelPart frontFlapHem;
    private final ModelPart backFlap;
    private final ModelPart backFlapHem;

    public ClericArmorModel(ModelPart root) {
        super(root);
        ModelPart robe = body.getChild("robe");
        frontFlap = robe.getChild("front_flap_upper");
        frontFlapHem = frontFlap.getChild("front_flap_lower");
        backFlap = robe.getChild("back_flap_upper");
        backFlapHem = backFlap.getChild("back_flap_lower");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        ArmorCloth.Motion motion = ArmorCloth.sample(state);
        float forwardLeg = Math.min(ArmorCloth.forwardSwing(rightLeg, body), ArmorCloth.forwardSwing(leftLeg, body));
        float backwardLeg = Math.max(ArmorCloth.backwardSwing(rightLeg, body), ArmorCloth.backwardSwing(leftLeg, body));
        ArmorCloth.swayFront(frontFlap, frontFlapHem, body, forwardLeg, motion.flutter(), motion);
        ArmorCloth.swayBack(backFlap, backFlapHem, backwardLeg, ArmorCloth.FLIPPED, motion);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition hood = head.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(5, 47).addBox(-4.5F, -5.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        hood.addOrReplaceChild("hood_r1", CubeListBuilder.create().texOffs(16, 73).addBox(-4.0F, -5.0F, -1.0F, 4.0F, 4.0F, 3.0F), PartPose.offsetAndRotation(2.0F, 3.0F, 4.5F, -0.48F, 0.0F, 0.0F));
        hood.addOrReplaceChild("hood_r2", CubeListBuilder.create().texOffs(15, 65).addBox(-4.0F, -5.0F, -1.0F, 5.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(1.5F, 1.0F, 4.0F, -0.2618F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition robe = body.addOrReplaceChild("robe",
                CubeListBuilder.create().texOffs(64, 33).addBox(-4.0F, -9.0F, -6.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)).texOffs(0, 5)
                        .addBox(-4.0F, -7.5F, -8.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(-0.25F)).texOffs(0, 21).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(-0.25F))
                        .texOffs(22, 17).addBox(-5.0F, -3.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)).texOffs(22, 17)
                        .addBox(-5.0F, -8.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)).texOffs(1, 36).addBox(-2.5F, -5.65F, -8.25F, 5.0F, 5.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, 9.0F, 4.0F));
        robe.addOrReplaceChild("robe_r1", CubeListBuilder.create().texOffs(22, 31).addBox(-3.0F, -4.0F, -1.5F, 6.0F, 8.0F, 3.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-0.25F, -2.75F, 0.75F, 0.0F, 0.0F, 0.6109F));
        robe.addOrReplaceChild("robe_r2", CubeListBuilder.create().texOffs(61, 49).addBox(-4.0F, -1.0F, -3.5F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, -3.5F, 0.1745F, 0.0F, 0.0F));
        PartDefinition frontFlapUpper = robe.addOrReplaceChild("front_flap_upper", CubeListBuilder.create().texOffs(90, 14).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.5F, -7.0F, -0.0873F, 0.0F, 0.0F));
        frontFlapUpper.addOrReplaceChild("front_flap_lower", CubeListBuilder.create().texOffs(90, 22).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(91, 26)
                .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
        PartDefinition backFlapUpper = robe.addOrReplaceChild("back_flap_upper", CubeListBuilder.create().texOffs(90, 14).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.5F, -1.0F, -0.0873F, 3.1416F, 0.0F));
        backFlapUpper.addOrReplaceChild("back_flap_lower", CubeListBuilder.create().texOffs(90, 22).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(91, 26)
                .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition rightSleeve = rightArm.addOrReplaceChild("right_sleeve",
                CubeListBuilder.create().texOffs(88, 33).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.29F)).texOffs(89, 49).addBox(-1.5F, 5.25F, 2.0F, 3.0F, 5.0F, 1.0F)
                        .texOffs(97, 49).addBox(-1.0F, 7.25F, 2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)).texOffs(50, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition rightMantle = rightSleeve.addOrReplaceChild("right_mantle", CubeListBuilder.create().texOffs(40, 40).addBox(-3.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                .texOffs(40, 40).addBox(-2.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F)), PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, 0.1745F));
        rightMantle.addOrReplaceChild("right_mantle_r1", CubeListBuilder.create().texOffs(41, 29).addBox(1.0F, -2.0F, -3.0F, 1.0F, 5.0F, 6.0F),
                PartPose.offsetAndRotation(-1.5F, -2.75F, 0.0F, 0.0F, 0.0F, -0.3491F));
        rightMantle.addOrReplaceChild("right_mantle_r2", CubeListBuilder.create().texOffs(61, 19).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3491F));
        PartDefinition leftArm = root.getChild("left_arm");
        leftArm.addOrReplaceChild("left_sleeve",
                CubeListBuilder.create().texOffs(88, 33).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.29F)).mirror(false).texOffs(89, 49).mirror()
                        .addBox(-1.5F, 5.25F, 2.0F, 3.0F, 5.0F, 1.0F).mirror(false).texOffs(97, 49).mirror().addBox(-1.0F, 7.25F, 2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)).mirror(false)
                        .texOffs(50, 0).mirror().addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)).mirror(false),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition leftMantle = leftArm
                .addOrReplaceChild(
                        "left_mantle", CubeListBuilder.create().texOffs(40, 40).mirror().addBox(1.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)).mirror(false).texOffs(40, 40)
                                .mirror().addBox(0.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F)).mirror(false),
                        PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, -0.1745F));
        leftMantle.addOrReplaceChild("left_mantle_r1", CubeListBuilder.create().texOffs(41, 29).mirror().addBox(-2.0F, -2.0F, -3.0F, 1.0F, 5.0F, 6.0F).mirror(false),
                PartPose.offsetAndRotation(1.5F, -2.75F, 0.0F, 0.0F, 0.0F, 0.3491F));
        leftMantle.addOrReplaceChild("left_mantle_r2", CubeListBuilder.create().texOffs(61, 19).mirror().addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)).mirror(false),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.3491F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition rightSkirt = rightLeg.addOrReplaceChild("right_skirt", CubeListBuilder.create(), PartPose.rotation(0.0F, 0.0F, 0.0349F));
        rightSkirt.addOrReplaceChild("right_skirt_r1", CubeListBuilder.create().texOffs(25, 9).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.45F, 7.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        rightSkirt.addOrReplaceChild("right_skirt_r2", CubeListBuilder.create().texOffs(25, 9).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.45F, 5.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        rightSkirt.addOrReplaceChild("right_skirt_r3", CubeListBuilder.create().texOffs(25, 9).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.45F, 3.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        rightSkirt.addOrReplaceChild("right_skirt_panel", CubeListBuilder.create().texOffs(105, 32).addBox(-2.0F, 1.0F, -2.5F, 5.0F, 8.0F, 5.0F), PartPose.rotation(0.0F, 0.0F, 0.1745F));
        PartDefinition leftLeg = root.getChild("left_leg");
        PartDefinition leftSkirt = leftLeg.addOrReplaceChild("left_skirt", CubeListBuilder.create(), PartPose.rotation(0.0F, 0.0F, -0.0349F));
        leftSkirt.addOrReplaceChild("left_skirt_r1", CubeListBuilder.create().texOffs(25, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 7.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        leftSkirt.addOrReplaceChild("left_skirt_r2", CubeListBuilder.create().texOffs(25, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 5.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        leftSkirt.addOrReplaceChild("left_skirt_r3", CubeListBuilder.create().texOffs(25, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 3.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        leftSkirt.addOrReplaceChild("left_skirt_panel", CubeListBuilder.create().texOffs(105, 32).mirror().addBox(-3.0F, 1.0F, -2.5F, 5.0F, 8.0F, 5.0F).mirror(false),
                PartPose.rotation(0.0F, 0.0F, -0.1745F));
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("hip", CubeListBuilder.create().texOffs(91, 0).addBox(-5.0F, 9.5F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        emptyChain(body, "robe", "front_flap_upper", "front_flap_lower");
        emptyChain(body, "robe", "back_flap_upper", "back_flap_lower");
        return mesh;
    }
}
