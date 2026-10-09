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
import net.minecraft.util.Mth;

public final class PraetorArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float SEATED_CLOTH_LIFT = 0.3F;
    private static final float SEATED_CLOTH_WIDTH = 0.4F;
    private static final float TRAILING_LIFT = 0.65F;
    private static final float PADDING = 0.03125F;
    private static final float CLOTH_REST = -8.0F * DEGREES_TO_RADIANS;
    private static final float HEM_REST = -4.0F * DEGREES_TO_RADIANS;
    private static final float CAPE_REST = 8.0F * DEGREES_TO_RADIANS;
    private static final float CAPE_HEM_REST = 6.0F * DEGREES_TO_RADIANS;
    private static final String[] SIDES = {"right", "left"};

    private final ModelPart rightCloth;
    private final ModelPart rightHem;
    private final ModelPart leftCloth;
    private final ModelPart leftHem;
    private final ModelPart cape;
    private final ModelPart capeHem;

    public PraetorArmorModel(ModelPart root) {
        super(root);
        rightCloth = body.getChild("tabard_right_upper");
        rightHem = rightCloth.getChild("tabard_right_lower");
        leftCloth = body.getChild("tabard_left_upper");
        leftHem = leftCloth.getChild("tabard_left_lower");
        cape = body.getChild("cape_mantle_upper");
        capeHem = cape.getChild("cape_mantle_lower");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        Gait gait = Gait.sample(state);
        float trailing = state.isFallFlying || state.isVisuallySwimming ? TRAILING_LIFT : 0.0F;
        swayCloth(rightCloth, rightHem, rightLeg, state, gait, gait.flutter());
        swayCloth(leftCloth, leftHem, leftLeg, state, gait, -gait.flutter());
        cape.xRot = CAPE_REST + gait.stride() * 0.32F + gait.flutter() + gait.crouch() * 0.16F + trailing;
        capeHem.xRot = CAPE_HEM_REST + gait.stride() * 0.12F + gait.flutter() * 1.5F;
    }

    private void swayCloth(ModelPart cloth, ModelPart hem, ModelPart leg, HumanoidRenderState state, Gait gait, float flutter) {
        float lift = state.isPassenger ? SEATED_CLOTH_LIFT : 0.0F;
        float swing = Math.min(0.0F, leg.xRot - body.xRot);
        cloth.xRot = CLOTH_REST + swing - gait.stride() * 0.3F - Math.abs(body.yRot) - gait.speed() * 0.08F - gait.crouch() - lift;
        cloth.y -= gait.crouch();
        cloth.xScale = state.isPassenger ? SEATED_CLOTH_WIDTH : 1.0F;
        hem.xRot = HEM_REST - gait.stride() * 0.12F - flutter + gait.crouch();
    }

    private record Gait(float speed, float stride, float crouch, float flutter) {
        static Gait sample(HumanoidRenderState state) {
            float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
            float stride = Math.abs(Mth.cos(state.walkAnimationPos * 0.6662F)) * speed;
            float crouch = state.isCrouching ? 1.0F : 0.0F;
            float flutter = Mth.sin(state.ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
            return new Gait(speed, stride, crouch, flutter);
        }
    }

    private record Cube(int u, int v, float x, float y, float z, float w, float h, float d, float grow) {
        static Cube of(int u, int v, float x, float y, float z, float w, float h, float d) {
            return new Cube(u, v, x, y, z, w, h, d, 0.0F);
        }

        static Cube padded(int u, int v, float x, float y, float z, float w, float h, float d) {
            return new Cube(u, v, x, y, z, w, h, d, PADDING);
        }

        void addTo(CubeListBuilder builder) {
            builder.texOffs(u, v).addBox(x, y, z, w, h, d, new CubeDeformation(grow));
        }
    }

    private static PartDefinition part(PartDefinition parent, String name, PartPose pose, Cube... cubes) {
        CubeListBuilder builder = CubeListBuilder.create();
        for (Cube cube : cubes) {
            cube.addTo(builder);
        }
        return parent.addOrReplaceChild(name, builder, pose);
    }

    private static PartPose at(float x, float y, float z) {
        return PartPose.offset(x, y, z);
    }

    private static PartPose tilted(float x, float y, float z, float rx, float ry, float rz) {
        return PartPose.offsetAndRotation(x, y, z, rx, ry, rz);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition head = part(mesh.getRoot(), "head", at(0.0F, 0.0F, 0.0F), Cube.padded(108, 1, -4.25F, -8.75F, -4.9375F, 8.5F, 8.5F, 0.5F),
                Cube.padded(1, 17, -4.25F, -9.375F, -4.375F, 8.5F, 1.0F, 8.5F), Cube.padded(36, 17, -4.125F, -8.375F, 4.25F, 8.5F, 8.0F, 0.5F),
                Cube.padded(55, 17, -4.75F, -8.125F, -4.1875F, 0.5F, 8.0F, 8.5F), Cube.padded(74, 17, 4.25F, -8.125F, -4.1875F, 0.5F, 8.0F, 8.5F));
        part(head, "swept_cheek_-1", tilted(-4.25F, -4.5F, -2.5F, 0.0F, -0.2094395F, 0.0698132F), Cube.of(93, 17, -0.625F, -0.25F, -2.0F, 1.0F, 4.5F, 4.0F));
        part(head, "swept_cheek_1", tilted(4.25F, -4.5F, -2.5F, 0.0F, 0.2094395F, -0.0698132F), Cube.of(104, 17, -0.375F, -0.25F, -2.0F, 1.0F, 4.5F, 4.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition torso = part(root, "body", at(0.0F, 0.0F, 0.0F), Cube.padded(1, 1, -4.25F, 0.75F, -2.5F, 8.5F, 10.0F, 5.0F), Cube.of(29, 1, -4.25F, 1.0F, -3.875F, 8.5F, 7.0F, 1.0F),
                Cube.of(49, 1, -2.5F, 2.625F, -4.875F, 5.0F, 5.0F, 0.5F), Cube.of(61, 1, -4.5F, 10.375F, -2.875F, 9.0F, 1.5F, 6.0F), Cube.of(92, 1, -4.5F, 0.875F, -4.375F, 3.0F, 9.5F, 0.5F),
                Cube.of(100, 1, 1.5F, 0.875F, -4.375F, 3.0F, 9.5F, 0.5F), Cube.of(1, 35, -5.0F, -0.25F, -5.5F, 10.0F, 2.5F, 0.5F), Cube.of(23, 35, -4.75F, -1.875F, 4.875F, 9.5F, 4.0F, 0.5F),
                Cube.of(101, 35, -4.0F, 0.5F, 3.75F, 1.0F, 1.0F, 1.5F), Cube.of(122, 35, 3.0F, 0.5F, 3.75F, 1.0F, 1.0F, 1.5F));
        part(torso, "gorget_side_-1", tilted(-5.0F, 1.0F, 0.0F, 0.1396263F, 0.0F, 0.0698132F), Cube.of(44, 35, -0.25F, -1.5F, -4.875F, 0.5F, 2.5F, 9.5F));
        part(torso, "gorget_side_1", tilted(5.0F, 1.0F, 0.0F, 0.1396263F, 0.0F, -0.0698132F), Cube.of(65, 35, -0.25F, -1.5F, -4.875F, 0.5F, 2.5F, 9.5F));
        PartDefinition rightFlap = part(torso, "tabard_right_upper", tilted(-2.875F, 10.5F, -4.375F, -0.1396263F, 0.0F, 0.0F), Cube.of(86, 35, -1.5F, 0.0F, -0.25F, 3.0F, 5.5F, 0.5F));
        part(rightFlap, "tabard_right_lower", tilted(0.0F, 5.5F, 0.0F, -0.0698132F, 0.0F, 0.0F), Cube.of(94, 35, -1.25F, -0.125F, -0.1875F, 2.5F, 4.0F, 0.5F));
        PartDefinition leftFlap = part(torso, "tabard_left_upper", tilted(2.875F, 10.5F, -4.375F, -0.1396263F, 0.0F, 0.0F), Cube.of(107, 35, -1.5F, 0.0F, -0.25F, 3.0F, 4.0F, 0.5F));
        part(leftFlap, "tabard_left_lower", tilted(0.0F, 4.0F, 0.0F, -0.0698132F, 0.0F, 0.0F), Cube.of(115, 35, -1.25F, -0.125F, -0.1875F, 2.5F, 2.5F, 0.5F));
        PartDefinition mantle = part(torso, "cape_mantle_upper", tilted(-1.0F, 0.75F, 4.375F, 0.1396263F, 0.0F, 0.0F), Cube.of(1, 48, -4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 0.5F));
        part(mantle, "cape_mantle_lower", tilted(0.0F, 12.0F, 0.0F, 0.1047198F, 0.0F, 0.0F), Cube.of(21, 48, -4.25F, -0.125F, 0.0625F, 8.5F, 3.5F, 0.5F));
        PartDefinition rightShoulder = part(root, "right_arm", at(-5.0F, 2.0F, 0.0F), Cube.padded(40, 48, -3.25F, -1.875F, -2.375F, 4.5F, 3.5F, 4.5F),
                Cube.padded(82, 48, -3.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F), Cube.of(102, 48, -3.375F, 8.1875F, -2.6875F, 4.5F, 0.5F, 5.5F),
                Cube.of(1, 62, -3.375F, 5.6875F, -2.6875F, 4.5F, 0.5F, 5.5F));
        part(rightShoulder, "legate_pauldron_right", tilted(-1.0F, -1.75F, 0.0F, 0.0F, 0.0F, 0.0872665F), Cube.of(59, 48, -2.75F, -0.25F, -2.875F, 5.5F, 3.0F, 5.5F));
        PartDefinition leftShoulder = part(root, "left_arm", at(5.0F, 2.0F, 0.0F), Cube.padded(60, 62, -1.25F, -1.875F, -2.375F, 4.5F, 3.5F, 4.5F),
                Cube.of(102, 62, 3.75F, -4.5F, -2.625F, 0.5F, 4.0F, 5.0F), Cube.padded(1, 73, -1.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F), Cube.of(21, 73, -1.375F, 8.1875F, -2.6875F, 4.5F, 0.5F, 5.5F),
                Cube.of(42, 73, -1.375F, 5.6875F, -2.6875F, 4.5F, 0.5F, 5.5F));
        part(leftShoulder, "legate_pauldron_left", tilted(1.0F, -1.75F, 0.0F, 0.0F, 0.0F, -0.2094395F), Cube.of(79, 62, -2.75F, -0.75F, -2.875F, 5.5F, 4.5F, 5.5F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        part(root, "right_leg", at(-1.9F, 12.0F, 0.0F), Cube.of(22, 62, -1.75F, 0.375F, -3.125F, 3.5F, 3.0F, 6.0F), Cube.of(42, 62, -1.5F, 2.375F, -2.875F, 3.0F, 3.0F, 5.5F));
        part(root, "left_leg", at(1.9F, 12.0F, 0.0F), Cube.of(63, 73, -1.75F, 0.375F, -3.125F, 3.5F, 3.0F, 6.0F), Cube.of(83, 73, -1.5F, 2.375F, -2.875F, 3.0F, 3.0F, 5.5F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition torso = mesh.getRoot().getChild("body");
        for (String side : SIDES) {
            String flap = "tabard_" + side;
            KnightArmorModel.emptyChild(KnightArmorModel.emptyChild(torso, flap + "_upper"), flap + "_lower");
        }
        PartDefinition mantle = KnightArmorModel.emptyChild(torso, "cape_mantle_upper");
        KnightArmorModel.emptyChild(mantle, "cape_mantle_lower");
        return mesh;
    }
}
