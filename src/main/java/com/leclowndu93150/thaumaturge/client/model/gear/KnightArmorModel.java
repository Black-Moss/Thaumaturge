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

public final class KnightArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart tabard;
    private final ModelPart tabardHem;
    private final ModelPart cape;

    public KnightArmorModel(ModelPart root) {
        super(root);
        ModelPart chestplate = body.getChild("chestplate");
        tabard = chestplate.getChild("tabard_upper").getChild("tabard_middle");
        tabardHem = tabard.getChild("tabard_lower");
        cape = chestplate.getChild("cape_upper");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        ArmorCloth.Motion motion = ArmorCloth.sample(state);
        float forwardLeg = Math.min(ArmorCloth.forwardSwing(rightLeg, body), ArmorCloth.forwardSwing(leftLeg, body));
        ArmorCloth.swayFront(tabard, tabardHem, body, forwardLeg, motion.flutter(), motion, false);
        CapeSway.swing(cape, state);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition helmet = head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -12.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(-3.4F)), PartPose.ZERO);
        PartDefinition faceplate = helmet.addOrReplaceChild("faceplate", CubeListBuilder.create(), PartPose.offset(0.0F, -4.65F, -0.35F));
        faceplate.addOrReplaceChild("faceplate_r1", CubeListBuilder.create().texOffs(70, 31).addBox(-8.0F, -3.6F, -3.4F, 16.0F, 11.0F, 11.0F, new CubeDeformation(-3.15F)),
                PartPose.offsetAndRotation(0.0F, 1.15F, -5.3F, 0.1309F, 0.0F, 0.0F));
        faceplate.addOrReplaceChild("faceplate_r2", CubeListBuilder.create().texOffs(70, 3).addBox(-8.0F, -8.6F, -3.4F, 16.0F, 12.0F, 11.0F, new CubeDeformation(-3.1F)),
                PartPose.offsetAndRotation(0.0F, 0.5F, -5.25F, -0.1745F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition chestplate = body.addOrReplaceChild("chestplate",
                CubeListBuilder.create().texOffs(131, 5).addBox(-4.0F, -8.0F, -8.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(-0.25F)).texOffs(131, 21)
                        .addBox(-4.0F, -8.0F, -2.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(-0.25F)).texOffs(131, 15).addBox(-4.0F, -1.5F, -8.25F, 8.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F))
                        .texOffs(153, 17).addBox(-5.0F, -3.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)).texOffs(153, 17)
                        .addBox(-5.0F, -8.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)),
                PartPose.offset(0.0F, 9.0F, 4.0F));
        PartDefinition tabardUpper = chestplate.addOrReplaceChild("tabard_upper", CubeListBuilder.create().texOffs(133, 42).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 8.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, -7.0F, -8.0F));
        PartDefinition tabardMiddle = tabardUpper.addOrReplaceChild("tabard_middle",
                CubeListBuilder.create().texOffs(133, 51).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
        tabardMiddle.addOrReplaceChild("tabard_lower", CubeListBuilder.create().texOffs(133, 59).addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(134, 63)
                .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
        PartDefinition capeUpper = chestplate.addOrReplaceChild("cape_upper", CubeListBuilder.create().texOffs(152, 42).addBox(-5.0F, -0.25F, -0.5F, 10.0F, 8.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0873F, 0.0F, 0.0F));
        PartDefinition capeMiddle = capeUpper.addOrReplaceChild("cape_middle", CubeListBuilder.create().texOffs(152, 51).addBox(-5.0F, -0.25F, -0.5F, 10.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, -0.1309F, 0.0F, 0.0F));
        capeMiddle.addOrReplaceChild("cape_lower", CubeListBuilder.create().texOffs(152, 59).addBox(-5.0F, -0.25F, -0.5F, 10.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F)).texOffs(152, 63).addBox(-5.0F,
                2.25F, -0.5F, 10.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.1745F, 0.0F, 0.0F));
        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition rightShoulder = rightArm.addOrReplaceChild("right_shoulder",
                CubeListBuilder.create().texOffs(181, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)).texOffs(215, 0)
                        .addBox(-4.0F, 2.0F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(-1.5F)).texOffs(163, 8).addBox(0.0F, 5.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        rightShoulder.addOrReplaceChild("right_pauldron", CubeListBuilder.create().texOffs(192, 19).addBox(-4.0F, -3.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)).texOffs(194, 29)
                .addBox(-3.5F, 0.0F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, 0.1745F));
        PartDefinition leftArm = root.getChild("left_arm");
        PartDefinition leftShoulder = leftArm.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create().texOffs(181, 0).mirror().addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)).mirror(false).texOffs(215, 0).mirror()
                        .addBox(-2.0F, 2.0F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(-1.5F)).mirror(false).texOffs(163, 8).mirror()
                        .addBox(-2.0F, 5.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F)).mirror(false),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        leftShoulder
                .addOrReplaceChild(
                        "left_pauldron", CubeListBuilder.create().texOffs(192, 19).mirror().addBox(-2.0F, -3.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)).mirror(false).texOffs(194, 29)
                                .mirror().addBox(-0.5F, 0.0F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(-0.5F)).mirror(false),
                        PartPose.offsetAndRotation(-0.5F, -0.25F, 0.0F, 0.0F, 0.0F, -0.1745F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = root.getChild("right_leg");
        rightLeg.addOrReplaceChild("right_tasset", CubeListBuilder.create().texOffs(220, 30).addBox(-4.6F, -2.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.5F)).texOffs(221, 47).addBox(-4.1F,
                1.5F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)), PartPose.rotation(0.0F, 0.0F, 0.2094F));
        PartDefinition leftLeg = root.getChild("left_leg");
        leftLeg.addOrReplaceChild("left_tasset", CubeListBuilder.create().texOffs(220, 30).mirror().addBox(-3.4F, -2.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.5F)).mirror(false)
                .texOffs(221, 47).mirror().addBox(-2.9F, 1.5F, -4.0F, 7.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)).mirror(false), PartPose.rotation(0.0F, 0.0F, -0.2094F));
        PartDefinition body = root.getChild("body");
        body.addOrReplaceChild("hip", CubeListBuilder.create().texOffs(219, 18).addBox(-5.0F, 9.5F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        emptyChain(body, "chestplate", "tabard_upper", "tabard_middle", "tabard_lower");
        emptyChain(body, "chestplate", "cape_upper");
        return mesh;
    }
}
