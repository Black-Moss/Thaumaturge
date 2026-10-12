package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.TurretCrossbowRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class CrossbowModel extends EntityModel<TurretCrossbowRenderState> {
    private static final int TEXTURE_SIZE = 256;
    private static final float TEXEL_SCALE = 0.5F;

    private static final String STAND = "stand";
    private static final String AIM = "aim";
    private static final String BOW_RIGHT = "bow_right";
    private static final String BOW_LEFT = "bow_left";
    private static final String LIMB = "limb";
    private static final String LIMB_TIP = "limb_tip";
    private static final String LOADER = "loader";
    private static final String NOSE = "nose";
    private static final String FOOT = "foot";
    private static final String[] LEG_NAMES = {"leg_front_right", "leg_front_left", "leg_back_left", "leg_back_right"};
    private static final int[][] LEG_UV = {{212, 42}, {2, 70}, {42, 70}, {82, 70}};
    private static final int[][] FOOT_UV = {{230, 42}, {20, 70}, {60, 70}, {100, 70}};

    private static final float MOUNT_TOP = -2.0F;
    private static final float MOUNT_HALF = 3.5F;
    private static final float MOUNT_SIZE = 7.0F;
    private static final float MOUNT_HEIGHT = 5.0F;

    private static final float LEG_HIP_Y = 2.5F;
    private static final float LEG_HALF = 2.0F;
    private static final float LEG_THICKNESS = 4.0F;
    private static final float LEG_LENGTH = 22.0F;
    private static final float LEG_SPLAY = 0.5F;
    private static final float LEG_FIRST_YAW = Mth.PI / 4.0F;
    private static final float QUARTER_TURN = Mth.HALF_PI;
    private static final float FOOT_HALF = 2.5F;
    private static final float FOOT_SIZE = 5.0F;
    private static final float FOOT_HEIGHT = 3.0F;
    private static final float FOOT_SINK = 1.0F;
    private static final float CART_LEG_SPLAY = 0.1F;
    private static final float CART_LEG_LIFT = 8.0F;

    private static final float STOCK_HALF_WIDTH = 4.0F;
    private static final float STOCK_HEIGHT = 4.0F;
    private static final float STOCK_HALF_LENGTH = 17.0F;
    private static final float STOCK_WIDTH = 8.0F;
    private static final float STOCK_LENGTH = 34.0F;
    private static final float BUTT_HALF_WIDTH = 3.0F;
    private static final float BUTT_TOP = -5.0F;
    private static final float BUTT_FRONT = 14.0F;
    private static final float BUTT_WIDTH = 6.0F;
    private static final float BUTT_HEIGHT = 6.0F;
    private static final float BUTT_DEPTH = 5.0F;
    private static final float NOSE_Z = -14.0F;
    private static final float NOSE_Y = 0.5F;
    private static final float NOSE_HALF = 2.0F;
    private static final float NOSE_SIZE = 4.0F;
    private static final float NOSE_TILT = Mth.PI / 4.0F;

    private static final float CASE_HALF_WIDTH = 4.5F;
    private static final float CASE_TOP = -13.0F;
    private static final float CASE_HALF_LENGTH = 9.0F;
    private static final float CASE_WIDTH = 9.0F;
    private static final float CASE_HEIGHT = 10.0F;
    private static final float CASE_LENGTH = 18.0F;
    private static final float BAR_HALF_WIDTH = 5.0F;
    private static final float BAR_TOP = -14.0F;
    private static final float BAR_FRONT_Z = -6.0F;
    private static final float BAR_BACK_Z = 3.0F;
    private static final float BAR_WIDTH = 10.0F;
    private static final float BAR_THICKNESS = 2.0F;

    private static final float BOW_ROOT_Y = -2.0F;
    private static final float BOW_ROOT_Z = -13.0F;
    private static final float LIMB_ONE_LENGTH = 10.0F;
    private static final float LIMB_ONE_HEIGHT = 4.0F;
    private static final float LIMB_ONE_DEPTH = 3.0F;
    private static final float LIMB_TWO_LENGTH = 7.0F;
    private static final float LIMB_TWO_HEIGHT = 3.0F;
    private static final float LIMB_TWO_DEPTH = 2.0F;
    private static final float LIMB_TIP_LENGTH = 4.0F;
    private static final float LIMB_TIP_HEIGHT = 2.0F;
    private static final float LIMB_TIP_DEPTH = 2.0F;
    private static final float BOW_SWEEP = 0.2443461F;
    private static final float LIMB_SWEEP = 0.35F;
    private static final float TIP_SWEEP = 0.3F;
    private static final float BOW_FLEX = 0.2F;
    private static final float LIMB_FLEX = 0.25F;
    private static final float TIP_FLEX = 0.15F;
    private static final float ROCK_FREQUENCY = 2.0F * Mth.PI;

    private static final float LOADER_Y = -2.0F;
    private static final float LOADER_Z = -4.0F;
    private static final float LOADER_LEAN = -0.5585054F;
    private static final float LOADER_ROCK = 0.5F;
    private static final float UPRIGHT_INNER_X = 4.25F;
    private static final float UPRIGHT_WIDTH = 2.0F;
    private static final float UPRIGHT_HEIGHT = 18.0F;
    private static final float UPRIGHT_DEPTH = 4.0F;
    private static final float LOADER_BAR_HALF = 5.5F;
    private static final float LOADER_BAR_TOP = -17.5F;
    private static final float LOADER_BAR_WIDTH = 11.0F;
    private static final float LOADER_BAR_THICKNESS = 2.0F;

    private final ModelPart[] legs = new ModelPart[LEG_NAMES.length];
    private final ModelPart aim;
    private final ModelPart bowRight;
    private final ModelPart limbRight;
    private final ModelPart tipRight;
    private final ModelPart bowLeft;
    private final ModelPart limbLeft;
    private final ModelPart tipLeft;
    private final ModelPart loader;

    public CrossbowModel(ModelPart root) {
        super(root);
        ModelPart stand = root.getChild(STAND);
        for (int i = 0; i < LEG_NAMES.length; i++) {
            legs[i] = stand.getChild(LEG_NAMES[i]);
        }
        this.aim = root.getChild(AIM);
        this.bowRight = aim.getChild(BOW_RIGHT);
        this.limbRight = bowRight.getChild(LIMB);
        this.tipRight = limbRight.getChild(LIMB_TIP);
        this.bowLeft = aim.getChild(BOW_LEFT);
        this.limbLeft = bowLeft.getChild(LIMB);
        this.tipLeft = limbLeft.getChild(LIMB_TIP);
        this.loader = aim.getChild(LOADER);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition stand = root.addOrReplaceChild(STAND, CubeListBuilder.create().texOffs(182, 42).addBox(-MOUNT_HALF, MOUNT_TOP, -MOUNT_HALF, MOUNT_SIZE, MOUNT_HEIGHT, MOUNT_SIZE),
                PartPose.ZERO);
        for (int i = 0; i < LEG_NAMES.length; i++) {
            PartDefinition leg = stand.addOrReplaceChild(LEG_NAMES[i],
                    CubeListBuilder.create().texOffs(LEG_UV[i][0], LEG_UV[i][1]).addBox(-LEG_HALF, 0.0F, -LEG_HALF, LEG_THICKNESS, LEG_LENGTH, LEG_THICKNESS),
                    PartPose.offsetAndRotation(0.0F, LEG_HIP_Y, 0.0F, LEG_SPLAY, LEG_FIRST_YAW + i * QUARTER_TURN, 0.0F));
            leg.addOrReplaceChild(FOOT, CubeListBuilder.create().texOffs(FOOT_UV[i][0], FOOT_UV[i][1]).addBox(-FOOT_HALF, -FOOT_SINK, -FOOT_HALF, FOOT_SIZE, FOOT_HEIGHT, FOOT_SIZE),
                    PartPose.offset(0.0F, LEG_LENGTH, 0.0F));
        }
        PartDefinition aim = root.addOrReplaceChild(AIM,
                CubeListBuilder.create().texOffs(2, 2).addBox(-STOCK_HALF_WIDTH, -STOCK_HEIGHT, -STOCK_HALF_LENGTH, STOCK_WIDTH, STOCK_HEIGHT, STOCK_LENGTH).texOffs(88, 2)
                        .addBox(-BUTT_HALF_WIDTH, BUTT_TOP, BUTT_FRONT, BUTT_WIDTH, BUTT_HEIGHT, BUTT_DEPTH).texOffs(112, 2)
                        .addBox(-CASE_HALF_WIDTH, CASE_TOP, -CASE_HALF_LENGTH, CASE_WIDTH, CASE_HEIGHT, CASE_LENGTH).texOffs(168, 2)
                        .addBox(-BAR_HALF_WIDTH, BAR_TOP, BAR_FRONT_Z, BAR_WIDTH, BAR_THICKNESS, BAR_THICKNESS).texOffs(194, 2)
                        .addBox(-BAR_HALF_WIDTH, BAR_TOP, BAR_BACK_Z, BAR_WIDTH, BAR_THICKNESS, BAR_THICKNESS),
                PartPose.offset(0.0F, MOUNT_TOP, 0.0F));
        aim.addOrReplaceChild(NOSE, CubeListBuilder.create().texOffs(220, 2).addBox(-NOSE_HALF, -NOSE_HALF, -NOSE_HALF, NOSE_SIZE, NOSE_SIZE, NOSE_SIZE),
                PartPose.offsetAndRotation(0.0F, NOSE_Y, NOSE_Z, NOSE_TILT, 0.0F, 0.0F));
        addBow(aim, BOW_RIGHT, -1.0F, 2, 30, 50);
        addBow(aim, BOW_LEFT, 1.0F, 64, 92, 112);
        aim.addOrReplaceChild(LOADER,
                CubeListBuilder.create().texOffs(126, 42).addBox(-UPRIGHT_INNER_X - UPRIGHT_WIDTH, -UPRIGHT_HEIGHT, -UPRIGHT_DEPTH / 2.0F, UPRIGHT_WIDTH, UPRIGHT_HEIGHT, UPRIGHT_DEPTH)
                        .texOffs(140, 42).addBox(UPRIGHT_INNER_X, -UPRIGHT_HEIGHT, -UPRIGHT_DEPTH / 2.0F, UPRIGHT_WIDTH, UPRIGHT_HEIGHT, UPRIGHT_DEPTH).texOffs(154, 42)
                        .addBox(-LOADER_BAR_HALF, LOADER_BAR_TOP, -LOADER_BAR_THICKNESS / 2.0F, LOADER_BAR_WIDTH, LOADER_BAR_THICKNESS, LOADER_BAR_THICKNESS),
                PartPose.offsetAndRotation(0.0F, LOADER_Y, LOADER_Z, LOADER_LEAN, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE).apply(MeshTransformer.scaling(TEXEL_SCALE));
    }

    private static void addBow(PartDefinition aim, String name, float side, int rootU, int limbU, int tipU) {
        float rootStart = side < 0.0F ? -LIMB_ONE_LENGTH : 0.0F;
        float limbStart = side < 0.0F ? -LIMB_TWO_LENGTH : 0.0F;
        float tipStart = side < 0.0F ? -LIMB_TIP_LENGTH : 0.0F;
        PartDefinition bow = aim.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(rootU, 42).addBox(rootStart, -LIMB_ONE_HEIGHT / 2.0F, -LIMB_ONE_DEPTH / 2.0F, LIMB_ONE_LENGTH, LIMB_ONE_HEIGHT, LIMB_ONE_DEPTH),
                PartPose.offsetAndRotation(side * STOCK_HALF_WIDTH, BOW_ROOT_Y, BOW_ROOT_Z, 0.0F, -side * BOW_SWEEP, 0.0F));
        PartDefinition limb = bow.addOrReplaceChild(LIMB,
                CubeListBuilder.create().texOffs(limbU, 42).addBox(limbStart, -LIMB_TWO_HEIGHT / 2.0F, -LIMB_TWO_DEPTH / 2.0F, LIMB_TWO_LENGTH, LIMB_TWO_HEIGHT, LIMB_TWO_DEPTH),
                PartPose.offsetAndRotation(side * LIMB_ONE_LENGTH, 0.0F, 0.0F, 0.0F, -side * LIMB_SWEEP, 0.0F));
        limb.addOrReplaceChild(LIMB_TIP, CubeListBuilder.create().texOffs(tipU, 42).addBox(tipStart, -LIMB_TIP_HEIGHT / 2.0F, -LIMB_TIP_DEPTH / 2.0F, LIMB_TIP_LENGTH, LIMB_TIP_HEIGHT, LIMB_TIP_DEPTH),
                PartPose.offsetAndRotation(side * LIMB_TWO_LENGTH, 0.0F, 0.0F, 0.0F, -side * TIP_SWEEP, 0.0F));
    }

    private static float rock(float progress) {
        return progress <= 0.0F ? 0.0F : Mth.sin(ROCK_FREQUENCY * Mth.sqrt(Math.min(progress, 1.0F)));
    }

    @Override
    public void setupAnim(TurretCrossbowRenderState state) {
        super.setupAnim(state);
        aim.yRot = state.yRot * Mth.DEG_TO_RAD;
        aim.xRot = state.xRot * Mth.DEG_TO_RAD;
        float flex = rock(state.swingAnim);
        bowRight.yRot -= BOW_FLEX * flex;
        limbRight.yRot -= LIMB_FLEX * flex;
        tipRight.yRot -= TIP_FLEX * flex;
        bowLeft.yRot += BOW_FLEX * flex;
        limbLeft.yRot += LIMB_FLEX * flex;
        tipLeft.yRot += TIP_FLEX * flex;
        loader.xRot += LOADER_ROCK * rock(state.loadProgress);
        for (int i = 0; i < legs.length; i++) {
            if (state.ridingMinecart) {
                legs[i].xRot = CART_LEG_SPLAY;
                legs[i].y -= CART_LEG_LIFT;
            } else {
                legs[i].xRot = LEG_SPLAY;
            }
        }
    }
}
