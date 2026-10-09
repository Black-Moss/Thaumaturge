package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.PechRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class PechModel extends EntityModel<PechRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float TORSO_LEAN = 0.2094F;
    private static final float PACK_TILT = -0.1047F;
    private static final float WALK_FREQUENCY = 0.62F;
    private static final float LEG_SWING = 1.3F;
    private static final float ARM_SWING = 0.9F;
    private static final float JAW_MUMBLE_OPEN = 0.38F;
    private static final float JAW_MUMBLE_RATE = 0.14F;
    private static final float JAW_WALK_CHATTER = 0.12F;
    private static final float POUCH_SWAY = 0.22F;
    private static final float POUCH_BOUNCE = 0.12F;
    private static final float PACK_SWAY = 0.05F;
    private static final float IDLE_ARM_RATE = 0.08F;
    private static final float IDLE_ARM_SPREAD = 0.06F;
    private static final float ATTACK_LIFT = 1.4F;
    private static final float ATTACK_TWIST = 0.35F;
    private static final float ARM_WIDTH = 2.0F;
    private static final float LEG_SPACING = 1.5F;
    private static final float HIP_HEIGHT = 19.0F;
    private static final float SHOULDER_SPACING = 3.5F;
    private static final float SHOULDER_HEIGHT = 10.0F;

    public final ModelPart head;
    public final ModelPart jowls;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public final ModelPart pack;
    public final ModelPart pouch;

    public PechModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.jowls = root.getChild("jowls");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.pack = root.getChild("pack");
        this.pouch = root.getChild("pouch");
    }

    private static CubeListBuilder cuboid(CubeListBuilder builder, int u, int v, float[] dims) {
        return builder.texOffs(u, v).addBox(dims[0], dims[1], dims[2], dims[3], dims[4], dims[5]);
    }

    private static float[] dims(float x, float y, float z, float w, float h, float d) {
        return new float[]{x, y, z, w, h, d};
    }

    private static CubeListBuilder arm(boolean mirrored, int u, float originX) {
        CubeListBuilder builder = CubeListBuilder.create();
        if (mirrored) {
            builder.mirror();
        }
        return cuboid(builder, u, 23, dims(originX, 0.0F, -1.0F, ARM_WIDTH, 6.0F, 2.0F));
    }

    private static CubeListBuilder leg(boolean mirrored, int thighU, int thighV, int footU, int footV) {
        CubeListBuilder builder = CubeListBuilder.create();
        if (mirrored) {
            builder.mirror();
        }
        cuboid(builder, thighU, thighV, dims(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F));
        return cuboid(builder, footU, footV, dims(-1.5F, 4.0F, -2.5F, 3.0F, 1.0F, 4.0F));
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", cuboid(CubeListBuilder.create(), 87, 23, dims(-3.0F, 0.0F, -2.0F, 6.0F, 3.0F, 4.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));
        body.addOrReplaceChild("torso", cuboid(CubeListBuilder.create(), 90, 0, dims(-3.5F, -7.0F, -2.5F, 7.0F, 7.0F, 5.0F)), PartPose.rotation(TORSO_LEAN, 0.0F, 0.0F));
        PartPose facePose = PartPose.offset(0.0F, 9.0F, -1.5F);
        CubeListBuilder skull = cuboid(CubeListBuilder.create(), 0, 23, dims(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 5.0F));
        cuboid(skull, 85, 35, dims(-3.5F, -5.0F, -3.5F, 7.0F, 1.0F, 1.0F));
        root.addOrReplaceChild("head", cuboid(skull, 78, 35, dims(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F)), facePose);
        root.addOrReplaceChild("jowls", cuboid(CubeListBuilder.create(), 41, 23, dims(-3.5F, -1.0F, -4.0F, 7.0F, 3.0F, 4.0F)), facePose);
        for (float side : new float[]{-1.0F, 1.0F}) {
            boolean right = side < 0.0F;
            CubeListBuilder legShape = right ? leg(true, 108, 23, 48, 35) : leg(false, 0, 35, 63, 35);
            root.addOrReplaceChild(right ? "right_leg" : "left_leg", legShape, PartPose.offset(side * LEG_SPACING, HIP_HEIGHT, 0.0F));
        }
        for (float side : new float[]{-1.0F, 1.0F}) {
            boolean right = side < 0.0F;
            CubeListBuilder armShape = right ? arm(true, 23, -ARM_WIDTH) : arm(false, 32, 0.0F);
            root.addOrReplaceChild(right ? "right_arm" : "left_arm", armShape, PartPose.offset(side * SHOULDER_SPACING, SHOULDER_HEIGHT, -1.0F));
        }
        CubeListBuilder packShape = cuboid(CubeListBuilder.create(), 0, 0, dims(-6.0F, -7.0F, 0.0F, 12.0F, 13.0F, 9.0F));
        cuboid(packShape, 43, 0, dims(-6.5F, -8.0F, -0.5F, 13.0F, 2.0F, 10.0F));
        cuboid(packShape, 13, 35, dims(-7.0F, -11.0F, 2.5F, 14.0F, 3.0F, 3.0F));
        root.addOrReplaceChild("pack", packShape, PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, PACK_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("pouch", cuboid(CubeListBuilder.create(), 64, 23, dims(-4.0F, 0.0F, -0.5F, 8.0F, 4.0F, 3.0F)), PartPose.offset(0.0F, 16.0F, 2.5F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(PechRenderState state) {
        super.setupAnim(state);
        float phase = state.walkAnimationPos * WALK_FREQUENCY;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float stride = Mth.sin(phase) * speed;
        float headYaw = state.yRot * Mth.DEG_TO_RAD;
        float headPitch = state.xRot * Mth.DEG_TO_RAD;
        float mumble = JAW_MUMBLE_OPEN * Mth.abs(Mth.sin(state.mumble * JAW_MUMBLE_RATE));
        float chatter = JAW_WALK_CHATTER * speed * Mth.abs(Mth.sin(phase * 2.0F));
        this.head.yRot = headYaw;
        this.head.xRot = headPitch;
        this.jowls.yRot = headYaw;
        this.jowls.xRot = headPitch + mumble + chatter;
        float legAngle = stride * LEG_SWING;
        float armAngle = stride * ARM_SWING;
        this.rightLeg.xRot = legAngle;
        this.leftLeg.xRot = -legAngle;
        this.rightArm.xRot = -armAngle;
        this.leftArm.xRot = armAngle;
        float spread = IDLE_ARM_SPREAD * (1.0F + Mth.sin(state.ageInTicks * IDLE_ARM_RATE));
        this.rightArm.zRot = spread;
        this.leftArm.zRot = -spread;
        this.pouch.zRot = stride * POUCH_SWAY;
        this.pouch.xRot = POUCH_BOUNCE * speed * Mth.abs(Mth.cos(phase));
        this.pack.zRot = stride * PACK_SWAY;
        if (state.attackTime > 0.0F) {
            float arc = Mth.sin(Mth.sqrt(state.attackTime) * Mth.PI);
            this.rightArm.xRot -= arc * ATTACK_LIFT;
            this.rightArm.yRot = -Mth.sin(state.attackTime * Mth.PI) * ATTACK_TWIST;
        }
    }
}
