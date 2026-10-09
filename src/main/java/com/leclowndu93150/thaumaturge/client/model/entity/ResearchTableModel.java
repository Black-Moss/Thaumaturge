package com.leclowndu93150.thaumaturge.client.model.entity;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public final class ResearchTableModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final Set<Direction> OPEN_TOP = EnumSet.complementOf(EnumSet.of(Direction.DOWN));
    private static final Set<Direction> RESTING = EnumSet.complementOf(EnumSet.of(Direction.UP));
    private static final Set<Direction> RAIL_SIDES = EnumSet.complementOf(EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST));
    private static final Set<Direction> STRETCHER_SIDES = EnumSet.complementOf(EnumSet.of(Direction.NORTH, Direction.SOUTH));
    private static final Set<Direction> SHELF_SIDES = EnumSet.complementOf(EnumSet.of(Direction.WEST, Direction.EAST));

    private static final float SCROLL_ANCHOR_X = -2.0F;
    private static final float SCROLL_ANCHOR_Y = -2.0F;
    private static final float SCROLL_ANCHOR_Z = 2.0F;
    private static final float SCROLL_YAW = 10.0F;
    private static final float INK_X = -6.0F;
    private static final float INK_Z = 3.0F;
    private static final float[] SCROLL_ROW_Z = {2.0F, -1.0F, -4.0F};

    public final ModelPart root;
    public final ModelPart table;
    public final ModelPart inkwell;
    public final ModelPart scrollTube;
    public final ModelPart scrollRibbon;
    public final ModelPart shelfScrolls;

    public ResearchTableModel(ModelPart root) {
        this.root = root;
        this.table = root.getChild("table");
        this.inkwell = root.getChild("inkwell");
        this.scrollTube = root.getChild("scroll_tube");
        this.scrollRibbon = root.getChild("scroll_ribbon");
        this.shelfScrolls = root.getChild("shelf_scrolls");
    }

    private static CubeListBuilder flatBox(int u, int v, boolean mirrored, float x, float y, float z, float w, float h, float d) {
        CubeListBuilder builder = CubeListBuilder.create();
        if (mirrored) {
            builder.mirror();
        }
        return builder.texOffs(u, v).addBox(x, y, z, w, h, d);
    }

    private static void attachStatic(PartDefinition parent, String name, CubeListBuilder shape) {
        parent.addOrReplaceChild(name, shape, PartPose.ZERO);
    }

    private static void attachScroll(PartDefinition parent, String name, CubeListBuilder shape) {
        parent.addOrReplaceChild(name, shape, scrollAnchor());
    }

    private static CubeListBuilder scrollRowShape() {
        CubeListBuilder shape = CubeListBuilder.create();
        for (float z : SCROLL_ROW_Z) {
            shape.texOffs(0, 0).addBox(-3.0F, 10.0F, z, 8.0F, 2.0F, 2.0F, RESTING);
        }
        return shape;
    }

    private static CubeListBuilder legsShape() {
        CubeListBuilder shape = CubeListBuilder.create();
        for (float x : new float[]{-8.0F, 21.0F}) {
            shape.texOffs(96, 0).addBox(x, 3.0F, 5.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP);
            shape.texOffs(96, 0).addBox(x, 3.0F, -8.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP);
        }
        return shape;
    }

    private static CubeListBuilder railsShape() {
        CubeListBuilder shape = CubeListBuilder.create();
        for (float z : new float[]{7.0F, -8.0F}) {
            shape.texOffs(0, 19).addBox(-5.0F, 3.0F, z, 26.0F, 1.0F, 1.0F, RAIL_SIDES);
        }
        return shape;
    }

    private static CubeListBuilder shelfShape() {
        CubeListBuilder shape = CubeListBuilder.create();
        for (float x : new float[]{-8.0F, 21.0F}) {
            shape.texOffs(72, 32).addBox(x, 12.0F, -5.0F, 3.0F, 1.0F, 10.0F, STRETCHER_SIDES);
        }
        shape.texOffs(0, 21).addBox(-5.0F, 12.0F, -5.0F, 26.0F, 1.0F, 10.0F, SHELF_SIDES);
        return shape.texOffs(96, 16).addBox(12.0F, 8.0F, -4.0F, 7.0F, 4.0F, 9.0F, RESTING);
    }

    private static PartPose scrollAnchor() {
        return PartPose.offsetAndRotation(SCROLL_ANCHOR_X, SCROLL_ANCHOR_Y, SCROLL_ANCHOR_Z, 0.0F, SCROLL_YAW, 0.0F);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        attachScroll(root, "scroll_ribbon", flatBox(0, 4, true, -15.1F, -0.275F, -6.75F, 1.0F, 2.0F, 2.0F));
        attachScroll(root, "scroll_tube", flatBox(0, 0, true, -21.0F, -0.5F, -8.0F, 8.0F, 2.0F, 2.0F));
        root.addOrReplaceChild("inkwell", flatBox(0, 44, true, 0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 3.0F), PartPose.offset(INK_X, SCROLL_ANCHOR_Y, INK_Z));
        PartDefinition table = root.addOrReplaceChild("table", CubeListBuilder.create(), PartPose.ZERO);
        attachStatic(table, "shelf", shelfShape());
        attachStatic(table, "rails", railsShape());
        attachStatic(table, "legs", legsShape());
        attachStatic(table, "top", flatBox(0, 0, false, -8.0F, 0.0F, -8.0F, 32.0F, 3.0F, 16.0F));
        attachStatic(root, "shelf_scrolls", scrollRowShape());
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
