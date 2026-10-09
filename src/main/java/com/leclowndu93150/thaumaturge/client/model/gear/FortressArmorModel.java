package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;

public final class FortressArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int MASK_VARIANTS = 3;
    private static final int PLATE_V = 1;

    private static final String PART_HEAD = "head";
    private static final String PART_HAT = "hat";
    private static final String PART_BODY = "body";
    private static final String PART_RIGHT_ARM = "right_arm";
    private static final String PART_LEFT_ARM = "left_arm";
    private static final String PART_RIGHT_LEG = "right_leg";
    private static final String PART_LEFT_LEG = "left_leg";
    private static final String PART_GOGGLES = "goggles";
    private static final String PART_MASK_PREFIX = "mask_";
    private static final String PART_CREST_LEFT = "swept_crest_-1";
    private static final String PART_CREST_RIGHT = "swept_crest_1";

    private static final float ARM_PIVOT_X = 5.0F;
    private static final float ARM_PIVOT_Y = 2.0F;
    private static final float LEG_PIVOT_X = 1.9F;
    private static final float LEG_PIVOT_Y = 12.0F;
    private static final float CREST_PIVOT_X = 2.5F;
    private static final float CREST_PIVOT_Y = -8.0F;
    private static final float CREST_PIVOT_Z = -4.5F;
    private static final float CREST_TILT = 0.5235988F;

    private static final CubeDeformation SKIN = new CubeDeformation(0.03125F);
    private static final Set<Direction> NO_SOUTH = EnumSet.complementOf(EnumSet.of(Direction.SOUTH));
    private static final Set<Direction> NO_NORTH = EnumSet.complementOf(EnumSet.of(Direction.NORTH));
    private static final Set<Direction> FRONT_BACK = EnumSet.of(Direction.NORTH, Direction.SOUTH);

    private final ModelPart[] masks = new ModelPart[MASK_VARIANTS];
    private final ModelPart goggles;

    public FortressArmorModel(ModelPart root) {
        super(root);
        for (int i = 0; i < MASK_VARIANTS; i++) {
            masks[i] = head.getChild(maskName(i));
            masks[i].visible = false;
        }
        goggles = head.getChild(PART_GOGGLES);
        goggles.visible = false;
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        int selected = FortressArmorItem.mask(state.headEquipment);
        for (int i = 0; i < MASK_VARIANTS; i++) {
            masks[i].visible = selected == i;
        }
        goggles.visible = FortressArmorItem.hasGoggles(state.headEquipment);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition head = mesh.getRoot().addOrReplaceChild(PART_HEAD,
                assemble(Box.plain(1, 9, -4.5F, -8.75F, -4.5F, 9.0F, 2.5F, 9.0F).skin(), Box.plain(38, 9, -4.75F, -6.5F, -5.0F, 9.5F, 1.0F, 1.0F).faces(NO_SOUTH),
                        Box.plain(60, 9, -5.0F, -6.125F, -3.75F, 1.0F, 6.0F, 7.5F).skin(), Box.plain(78, 9, 4.0F, -6.125F, -3.75F, 1.0F, 6.0F, 7.5F).skin(),
                        Box.plain(96, 9, -4.25F, -5.875F, 4.125F, 8.5F, 5.5F, 0.5F).faces(NO_NORTH), Box.plain(119, 9, -4.25F, -7.875F, -4.875F, 1.5F, 1.0F, 0.5F),
                        Box.plain(1, 24, 2.75F, -7.875F, -4.875F, 1.5F, 1.0F, 0.5F)),
                PartPose.ZERO);
        head.addOrReplaceChild(PART_CREST_LEFT, assemble(Box.plain(115, 9, -0.5F, -3.5F, -0.625F, 1.0F, 4.0F, 0.5F)),
                PartPose.offsetAndRotation(-CREST_PIVOT_X, CREST_PIVOT_Y, CREST_PIVOT_Z, 0.0F, 0.0F, -CREST_TILT));
        head.addOrReplaceChild(PART_CREST_RIGHT, assemble(Box.plain(124, 9, -0.5F, -3.5F, -0.625F, 1.0F, 4.0F, 0.5F)),
                PartPose.offsetAndRotation(CREST_PIVOT_X, CREST_PIVOT_Y, CREST_PIVOT_Z, 0.0F, 0.0F, CREST_TILT));
        int[] maskU = {1, 21, 41};
        for (int i = 0; i < MASK_VARIANTS; i++) {
            head.addOrReplaceChild(maskName(i), assemble(flatPlate(maskU[i], -5.25F, -4.75F)), PartPose.ZERO);
        }
        head.addOrReplaceChild(PART_GOGGLES, assemble(flatPlate(61, -6.125F, -5.5F)), PartPose.ZERO);
        return finish(mesh);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(PART_BODY, assemble(Box.plain(81, 1, -4.5F, 10.75F, -2.75F, 9.0F, 1.0F, 5.5F), Box.plain(111, 1, -0.75F, 11.125F, -3.375F, 1.5F, 0.5F, 1.0F).faces(NO_SOUTH),
                Box.plain(6, 24, -4.25F, 1.25F, -3.5F, 8.5F, 2.5F, 1.0F), Box.plain(26, 24, -4.25F, 3.75F, -3.375F, 8.5F, 2.5F, 1.0F), Box.plain(46, 24, -4.25F, 6.25F, -3.25F, 8.5F, 2.5F, 1.0F),
                Box.plain(66, 24, -1.5F, 1.125F, -3.875F, 3.0F, 2.0F, 0.5F).faces(NO_SOUTH), Box.plain(74, 24, -4.25F, 1.25F, 2.4375F, 8.5F, 9.0F, 0.5F).faces(NO_NORTH),
                Box.plain(93, 24, -4.625F, 2.0625F, -2.375F, 0.5F, 8.0F, 4.5F), Box.plain(104, 24, 4.125F, 2.0625F, -2.375F, 0.5F, 8.0F, 4.5F),
                Box.plain(115, 24, 0.25F, 1.5F, 3.125F, 3.5F, 5.0F, 1.5F), Box.plain(1, 38, -3.75F, 3.5F, 3.25F, 2.0F, 6.0F, 2.0F), Box.plain(10, 38, -3.875F, 6.0F, 3.125F, 2.5F, 1.0F, 2.5F)),
                PartPose.ZERO);
        root.addOrReplaceChild(PART_RIGHT_ARM,
                assemble(Box.plain(21, 38, -3.5F, -2.5F, -2.75F, 5.0F, 2.5F, 5.5F), Box.plain(43, 38, -3.625F, 0.125F, -2.875F, 5.5F, 1.5F, 5.5F),
                        Box.plain(66, 38, -3.625F, 2.125F, -2.75F, 5.5F, 1.5F, 5.5F), Box.plain(89, 38, -3.25F, 5.625F, -2.625F, 4.5F, 4.0F, 5.0F).skin(),
                        Box.plain(1, 48, -3.375F, 8.875F, -2.875F, 4.5F, 1.0F, 5.5F)),
                armPose(-ARM_PIVOT_X));
        root.addOrReplaceChild(PART_LEFT_ARM,
                assemble(Box.plain(77, 48, -1.5F, -2.5F, -2.75F, 5.0F, 2.5F, 5.5F), Box.plain(99, 48, -1.625F, 0.125F, -2.875F, 5.5F, 1.5F, 5.5F),
                        Box.plain(1, 59, -1.625F, 2.125F, -2.75F, 5.5F, 1.5F, 5.5F), Box.plain(24, 59, -1.25F, 5.625F, -2.625F, 4.5F, 4.0F, 5.0F).skin(),
                        Box.plain(44, 59, -1.375F, 8.875F, -2.875F, 4.5F, 1.0F, 5.5F)),
                armPose(ARM_PIVOT_X));
        return finish(mesh);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(PART_RIGHT_LEG,
                assemble(Box.plain(22, 48, -1.85F, 0.25F, -3.125F, 3.5F, 2.5F, 0.5F), Box.plain(31, 48, -1.85F, 2.75F, -3.0F, 3.5F, 2.5F, 0.5F),
                        Box.plain(40, 48, -1.85F, 5.25F, -2.875F, 3.5F, 2.5F, 0.5F), Box.plain(49, 48, -2.725F, 0.5F, -2.25F, 0.5F, 5.0F, 4.5F).skin(),
                        Box.plain(60, 48, -1.85F, 0.75F, 2.5F, 3.5F, 4.5F, 0.5F).faces(NO_NORTH), Box.plain(69, 48, -1.725F, 8.0F, -2.5F, 3.0F, 2.0F, 0.5F).faces(NO_SOUTH)),
                legPose(-LEG_PIVOT_X));
        root.addOrReplaceChild(PART_LEFT_LEG,
                assemble(Box.plain(65, 59, -1.65F, 0.25F, -3.125F, 3.5F, 2.5F, 0.5F), Box.plain(74, 59, -1.65F, 2.75F, -3.0F, 3.5F, 2.5F, 0.5F),
                        Box.plain(83, 59, -1.65F, 5.25F, -2.875F, 3.5F, 2.5F, 0.5F), Box.plain(92, 59, 2.225F, 0.5F, -2.25F, 0.5F, 5.0F, 4.5F).skin(),
                        Box.plain(103, 59, -1.65F, 0.75F, 2.5F, 3.5F, 4.5F, 0.5F).faces(NO_NORTH), Box.plain(112, 59, -1.525F, 8.0F, -2.5F, 3.0F, 2.0F, 0.5F).faces(NO_SOUTH)),
                legPose(LEG_PIVOT_X));
        return finish(mesh);
    }

    private static Box flatPlate(int u, float y, float z) {
        return Box.plain(u, PLATE_V, -4.5F, y, z, 9.0F, 5.0F, 0.5F).faces(FRONT_BACK);
    }

    private static String maskName(int index) {
        return PART_MASK_PREFIX + index;
    }

    private static PartPose armPose(float x) {
        return PartPose.offset(x, ARM_PIVOT_Y, 0.0F);
    }

    private static PartPose legPose(float x) {
        return PartPose.offset(x, LEG_PIVOT_Y, 0.0F);
    }

    private static LayerDefinition finish(MeshDefinition mesh) {
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static CubeListBuilder assemble(Box... boxes) {
        CubeListBuilder builder = CubeListBuilder.create();
        for (Box box : boxes) {
            box.addTo(builder);
        }
        return builder;
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild(PART_HEAD, CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < MASK_VARIANTS; i++) {
            head.addOrReplaceChild(maskName(i), CubeListBuilder.create(), PartPose.ZERO);
        }
        head.addOrReplaceChild(PART_GOGGLES, CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild(PART_HAT, CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(PART_BODY, CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(PART_RIGHT_ARM, CubeListBuilder.create(), armPose(-ARM_PIVOT_X));
        root.addOrReplaceChild(PART_LEFT_ARM, CubeListBuilder.create(), armPose(ARM_PIVOT_X));
        root.addOrReplaceChild(PART_RIGHT_LEG, CubeListBuilder.create(), legPose(-LEG_PIVOT_X));
        root.addOrReplaceChild(PART_LEFT_LEG, CubeListBuilder.create(), legPose(LEG_PIVOT_X));
        return mesh;
    }

    private record Box(int u, int v, float x, float y, float z, float w, float h, float d, CubeDeformation deformation, Set<Direction> visibleFaces) {
        static Box plain(int u, int v, float x, float y, float z, float w, float h, float d) {
            return new Box(u, v, x, y, z, w, h, d, null, null);
        }

        Box skin() {
            return new Box(u, v, x, y, z, w, h, d, SKIN, null);
        }

        Box faces(Set<Direction> faces) {
            return new Box(u, v, x, y, z, w, h, d, null, faces);
        }

        void addTo(CubeListBuilder builder) {
            builder.texOffs(u, v);
            if (deformation != null) {
                builder.addBox(x, y, z, w, h, d, deformation);
            } else if (visibleFaces != null) {
                builder.addBox(x, y, z, w, h, d, visibleFaces);
            } else {
                builder.addBox(x, y, z, w, h, d);
            }
        }
    }
}
