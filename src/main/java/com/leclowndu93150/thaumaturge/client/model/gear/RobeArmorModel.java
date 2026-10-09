package com.leclowndu93150.thaumaturge.client.model.gear;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class RobeArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float TAIL_REST = 6.0F * DEGREES_TO_RADIANS;
    private static final float HEM_REST = 4.0F * DEGREES_TO_RADIANS;
    private static final float LEG_PUSH = 1.25F;
    private static final float CROUCH_LIFT = 0.5F;
    private static final float TRAILING_LIFT = 0.65F;
    private static final Set<Direction> SKIP_SOUTH = EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP);
    private static final Set<Direction> SKIP_NORTH = EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP);
    private static final String[] SIDES = {"right", "left"};

    private final ModelPart rightTail;
    private final ModelPart rightHem;
    private final ModelPart leftTail;
    private final ModelPart leftHem;

    public RobeArmorModel(ModelPart root) {
        super(root);
        rightTail = body.getChild("tail_right_upper");
        rightHem = rightTail.getChild("tail_right_lower");
        leftTail = body.getChild("tail_left_upper");
        leftHem = leftTail.getChild("tail_left_lower");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        float sway = Mth.sin(state.ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
        float lift = state.isFallFlying || state.isVisuallySwimming ? TRAILING_LIFT : 0.0F;
        float gait = Math.abs(Mth.cos(state.walkAnimationPos * 0.6662F)) * speed;
        float bend = state.isCrouching ? 1.0F : 0.0F;
        swingTail(rightTail, rightHem, rightLeg, new Motion(gait, bend, sway, lift));
        swingTail(leftTail, leftHem, leftLeg, new Motion(gait, bend, -sway, lift));
    }

    private void swingTail(ModelPart tail, ModelPart hem, ModelPart leg, Motion motion) {
        float stride = motion.stride();
        float crouch = motion.crouch();
        float flutter = motion.flutter();
        float backLeg = Math.max(0.0F, leg.xRot - body.xRot);
        tail.xRot = TAIL_REST + backLeg * LEG_PUSH + stride * 0.1F + flutter + crouch * CROUCH_LIFT + motion.trailing();
        hem.xRot = HEM_REST + stride * 0.12F + flutter * 1.5F;
    }

    private record Motion(float stride, float crouch, float flutter, float trailing) {
    }

    private record Cube(int u, int v, float x, float y, float z, float w, float h, float d, Set<Direction> faces) {
        static Cube of(int u, int v, float x, float y, float z, float w, float h, float d) {
            return new Cube(u, v, x, y, z, w, h, d, null);
        }

        static Cube faced(Set<Direction> faces, int u, int v, float x, float y, float z, float w, float h, float d) {
            return new Cube(u, v, x, y, z, w, h, d, faces);
        }

        void addTo(CubeListBuilder builder) {
            builder.texOffs(u, v);
            if (faces == null) {
                builder.addBox(x, y, z, w, h, d);
            } else {
                builder.addBox(x, y, z, w, h, d, faces);
            }
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
        PartDefinition hood = part(mesh.getRoot(), "head", at(0.0F, 0.0F, 0.0F), Cube.of(1, 1, -4.75F, -8.75F, -4.5F, 9.5F, 9.0F, 9.0F), Cube.of(1, 20, -5.0F, -7.375F, -5.0F, 10.0F, 1.0F, 10.0F),
                Cube.faced(SKIP_SOUTH, 42, 20, -1.0F, -7.875F, -5.25F, 2.0F, 2.0F, 0.5F), Cube.of(48, 20, -5.125F, -6.5F, -4.25F, 0.5F, 6.5F, 6.0F),
                Cube.of(62, 20, 4.625F, -6.5F, -4.25F, 0.5F, 6.5F, 6.0F));
        part(hood, "hood_peak_upper", tilted(0.0F, -8.5F, 4.5F, 0.3490659F, 0.0F, 0.0F), Cube.of(71, 59, -3.5F, -0.25F, -0.5F, 7.0F, 5.5F, 1.5F));
        part(hood, "hood_peak_lower", tilted(0.0F, -5.0F, 5.0F, 0.5934119F, 0.0F, 0.0F), Cube.of(89, 59, -2.5F, 0.5F, 0.0F, 5.0F, 4.0F, 1.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition torso = part(root, "body", at(0.0F, 0.0F, 0.0F), Cube.of(39, 1, -4.5F, 0.25F, -2.5F, 9.0F, 10.5F, 5.0F), Cube.of(68, 1, -4.75F, 10.375F, -2.75F, 9.5F, 1.5F, 5.5F),
                Cube.faced(SKIP_SOUTH, 82, 20, -2.5F, 1.25F, -3.375F, 5.0F, 7.0F, 0.5F), Cube.faced(SKIP_NORTH, 94, 20, -4.25F, -0.25F, 2.5625F, 8.5F, 10.5F, 0.5F),
                Cube.faced(SKIP_SOUTH, 104, 47, -1.0F, 10.125F, -3.0F, 2.0F, 2.0F, 0.5F), Cube.of(110, 47, -4.375F, 11.75F, -3.375F, 2.5F, 3.0F, 1.5F),
                Cube.of(119, 47, 1.875F, 11.75F, -3.375F, 2.5F, 3.0F, 1.5F));
        part(torso, "sash", tilted(0.0F, 6.0F, -2.625F, 0.0F, 0.0F, -0.6108652F), Cube.faced(SKIP_SOUTH, 76, 20, -1.0F, -7.0F, -0.25F, 2.0F, 13.5F, 0.5F));
        part(torso, "back_scroll", tilted(0.0F, 9.5F, 4.0F, 0.0F, 0.0F, -0.1919862F), Cube.of(102, 59, -3.75F, -1.375F, -0.875F, 7.5F, 2.5F, 2.5F));
        part(torso, "void_tome", tilted(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F), Cube.of(1, 72, 1.0F, 0.0F, 3.1875F, 5.0F, 7.0F, 2.0F));
        PartDefinition rightUpper = part(torso, "tail_right_upper", tilted(-2.25F, 11.25F, 3.0F, 0.1047198F, 0.0F, 0.0F), Cube.of(1, 59, -1.5F, 0.0F, -0.125F, 3.0F, 7.0F, 0.5F));
        part(rightUpper, "tail_right_lower", tilted(0.0F, 7.0F, 0.0F, 0.0698132F, 0.0F, 0.0F), Cube.of(9, 59, -1.25F, -0.125F, -0.0625F, 2.5F, 6.0F, 0.5F));
        PartDefinition leftUpper = part(torso, "tail_left_upper", tilted(2.25F, 11.25F, 3.0F, 0.1047198F, 0.0F, 0.0F), Cube.of(16, 59, -1.5F, 0.0F, -0.125F, 3.0F, 7.0F, 0.5F));
        part(leftUpper, "tail_left_lower", tilted(0.0F, 7.0F, 0.0F, 0.0698132F, 0.0F, 0.0F), Cube.of(24, 59, -1.25F, -0.125F, -0.0625F, 2.5F, 6.0F, 0.5F));
        part(root, "right_arm", at(-5.0F, 2.0F, 0.0F), Cube.of(86, 35, -3.75F, -2.375F, -2.75F, 5.0F, 3.5F, 5.5F), Cube.of(108, 35, -3.375F, 1.25F, -2.375F, 4.5F, 6.0F, 4.5F),
                Cube.of(20, 47, -3.5F, 9.0625F, -2.625F, 5.0F, 1.0F, 5.0F), Cube.of(62, 47, -3.5F, 7.3125F, -2.625F, 5.0F, 1.0F, 5.0F));
        PartDefinition sleeve = part(root, "left_arm", at(5.0F, 2.0F, 0.0F), Cube.of(1, 35, -1.375F, -3.0F, -2.875F, 5.5F, 4.0F, 6.0F), Cube.of(25, 35, 3.875F, -3.5F, -2.75F, 0.5F, 3.5F, 5.5F),
                Cube.of(1, 47, -1.125F, 1.25F, -2.375F, 4.5F, 6.0F, 4.5F), Cube.of(41, 47, -1.5F, 9.0625F, -2.625F, 5.0F, 1.0F, 5.0F), Cube.of(83, 47, -1.5F, 7.3125F, -2.625F, 5.0F, 1.0F, 5.0F));
        part(sleeve, "pauldron_lame_left0", tilted(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F), Cube.of(38, 35, 3.375F, -1.0F, -3.5F, 1.0F, 3.5F, 7.0F));
        part(sleeve, "pauldron_lame_left1", tilted(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F), Cube.of(55, 35, 2.5F, 1.0F, -3.25F, 1.0F, 3.5F, 6.5F));
        part(sleeve, "pauldron_lame_left2", tilted(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F), Cube.of(71, 35, 1.625F, 3.0F, -3.0F, 1.0F, 3.5F, 6.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        part(root, "right_leg", at(-1.9F, 12.0F, 0.0F), Cube.faced(SKIP_SOUTH, 31, 59, -1.975F, 5.0F, -2.4375F, 3.5F, 4.5F, 0.5F), Cube.of(49, 59, -2.725F, 0.0F, -2.25F, 0.5F, 7.0F, 4.5F));
        part(root, "left_leg", at(1.9F, 12.0F, 0.0F), Cube.faced(SKIP_SOUTH, 40, 59, -1.525F, 5.0F, -2.4375F, 3.5F, 4.5F, 0.5F), Cube.of(60, 59, 2.225F, 0.0F, -2.25F, 0.5F, 7.0F, 4.5F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition torso = mesh.getRoot().getChild("body");
        for (String side : SIDES) {
            String tail = "tail_" + side;
            KnightArmorModel.emptyChild(KnightArmorModel.emptyChild(torso, tail + "_upper"), tail + "_lower");
        }
        return mesh;
    }
}
