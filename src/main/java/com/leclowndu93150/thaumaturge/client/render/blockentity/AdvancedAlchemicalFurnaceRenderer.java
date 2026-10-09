package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemMeshes;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class AdvancedAlchemicalFurnaceRenderer implements BlockEntityRenderer<BlockEntityAdvancedAlchemicalFurnace, AdvancedAlchemicalFurnaceRenderState> {
    private static final Identifier MESH = TTIds.rl("models/mesh/advanced_alchemical_furnace.ttmesh");
    private static final Identifier BASE_TEXTURE = TTIds.rl("textures/block/advanced_alchemical_furnace.png");
    private static final Identifier BASE_TEXTURE_HOT = TTIds.rl("textures/block/advanced_alchemical_furnace_on.png");
    private static final Identifier TANK_TEXTURE = TTIds.rl("textures/block/advanced_alchemical_furnace_tank.png");
    private static final Identifier TANK_TEXTURE_FILLED = TTIds.rl("textures/block/advanced_alchemical_furnace_tank_on.png");
    private static final Identifier TRIM_TEXTURE = TTIds.rl("textures/block/metal_thaumium.png");
    private static final SpriteId GOO_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/flux_goo"));
    private static final SpriteId BACKING_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, TTIds.rl("block/base_metal"));
    private static final SpriteId FLAME_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/fire_0"));
    private static final String BASE_PART = "Base";
    private static final String TANK_PART = "Tank";
    private static final String TRIM_PART = "TankTrim";
    private static final int SIDE_COUNT = 4;
    private static final float QUARTER_TURN_DEGREES = 90.0F;
    private static final float CENTER = 0.5F;
    private static final int WHITE = 0xFFFFFFFF;
    private static final float NORMAL_OFFSET = 0.01F;
    private static final float SAMPLE_QUARTER_TURN = Mth.HALF_PI;
    private static final int GOO_BLOCK_LIGHT = 12;
    private static final int BACKING_BLOCK_LIGHT = 9;
    private static final int FLAME_BLOCK_LIGHT = 14;
    private static final int GOO_LIGHT = LightCoordsUtil.pack(GOO_BLOCK_LIGHT, 0);
    private static final int BACKING_LIGHT = LightCoordsUtil.pack(BACKING_BLOCK_LIGHT, 0);
    private static final int FLAME_LIGHT = LightCoordsUtil.pack(FLAME_BLOCK_LIGHT, 0);
    private static final float WINDOW_TOP = 1.8F;
    private static final float WINDOW_HEIGHT = 0.6F;
    private static final float GOO_SURFACE_Y = 1.1F;
    private static final float WINDOW_A_X = 1.35F;
    private static final float WINDOW_B_X = 1.65F;
    private static final float WINDOW_SPAN = 0.3F;
    private static final float WINDOW_A_Z = 1.9F;
    private static final float WINDOW_B_Z = -0.9F;
    private static final float WINDOW_GOO_DEPTH = 0.01F;
    private static final PanelPlane GOO_SURFACE = new PanelPlane(1.0F, GOO_SURFACE_Y, 1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0F);
    private static final PanelPlane WINDOW_A_BACKING = windowPlane(WINDOW_A_X, WINDOW_SPAN, WINDOW_A_Z);
    private static final PanelPlane WINDOW_A_GOO = windowPlane(WINDOW_A_X, WINDOW_SPAN, WINDOW_A_Z + WINDOW_GOO_DEPTH);
    private static final PanelPlane WINDOW_B_BACKING = windowPlane(WINDOW_B_X, -WINDOW_SPAN, WINDOW_B_Z);
    private static final PanelPlane WINDOW_B_GOO = windowPlane(WINDOW_B_X, -WINDOW_SPAN, WINDOW_B_Z - WINDOW_GOO_DEPTH);
    private static final float FLAME_TOP_Y = 1.7071F;
    private static final float FLAME_FAR_Z = -0.2071F;
    private static final float FLAME_SLOPE = 0.7071F;
    private static final float FLAME_BACKING_SHIFT = 0.0354F;
    private static final PanelPlane FLAME = new PanelPlane(1.0F, FLAME_TOP_Y, FLAME_FAR_Z, -1.0F, 0.0F, 0.0F, 0.0F, -FLAME_SLOPE, -FLAME_SLOPE);
    private static final PanelPlane FLAME_BACKING = new PanelPlane(1.0F, FLAME_TOP_Y - FLAME_BACKING_SHIFT, FLAME_FAR_Z + FLAME_BACKING_SHIFT, -1.0F, 0.0F, 0.0F, 0.0F, -FLAME_SLOPE, -FLAME_SLOPE);
    private static final float PANEL_FULL = 0.0F;
    private static final float BOUNDS_MARGIN_LOW = 1.0F;
    private static final float BOUNDS_MARGIN_HIGH = 2.0F;
    private static final float BOUNDS_HEIGHT = 2.0F;
    private static final int QUAD_VERTICES = 4;
    private static final float QUAD_VERTEX_COUNT = QUAD_VERTICES;

    private final SpriteGetter sprites;

    public AdvancedAlchemicalFurnaceRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public AdvancedAlchemicalFurnaceRenderState createRenderState() {
        return new AdvancedAlchemicalFurnaceRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityAdvancedAlchemicalFurnace furnace, AdvancedAlchemicalFurnaceRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(furnace, state, partialTicks, cameraPosition, breakProgress);
        state.assembled = furnace.isAssembled();
        state.heat = furnace.heat();
        state.stored = furnace.aspects().totalAmount();
        Level level = furnace.getLevel();
        state.meshLights = state.assembled && level != null ? sampleLights(level, furnace.getBlockPos()) : Map.of();
    }

    @Override
    public void submit(AdvancedAlchemicalFurnaceRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.assembled) {
            return;
        }
        boolean hot = state.heat > BlockEntityAdvancedAlchemicalFurnace.HOT_HEAT;
        boolean filled = state.stored > 0;
        poseStack.pushPose();
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.XN.rotationDegrees(QUARTER_TURN_DEGREES));
        submitParts(hot, filled, poseStack, collector, state.lightCoords, state.meshLights.isEmpty() ? null : state.meshLights);
        poseStack.popPose();
        if (filled) {
            submitEssentia(state, poseStack, collector);
        }
        if (hot) {
            submitVents(state, poseStack, collector);
        }
    }

    public static void submitMesh(boolean hot, boolean filled, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        submitParts(hot, filled, poseStack, collector, light, null);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityAdvancedAlchemicalFurnace furnace) {
        BlockPos pos = furnace.getBlockPos();
        return new AABB(pos.getX() - BOUNDS_MARGIN_LOW, pos.getY(), pos.getZ() - BOUNDS_MARGIN_LOW, pos.getX() + BOUNDS_MARGIN_HIGH, pos.getY() + BOUNDS_HEIGHT, pos.getZ() + BOUNDS_MARGIN_HIGH);
    }

    private static void submitParts(boolean hot, boolean filled, PoseStack poseStack, SubmitNodeCollector collector, int light, @Nullable Map<String, int[][]> lights) {
        submitPart(BASE_PART, 1, hot ? BASE_TEXTURE_HOT : BASE_TEXTURE, poseStack, collector, light, lights);
        submitPart(TANK_PART, SIDE_COUNT, filled ? TANK_TEXTURE_FILLED : TANK_TEXTURE, poseStack, collector, light, lights);
        submitPart(TRIM_PART, SIDE_COUNT, TRIM_TEXTURE, poseStack, collector, light, lights);
    }

    private static void submitPart(String name, int copies, Identifier texture, PoseStack poseStack, SubmitNodeCollector collector, int light, @Nullable Map<String, int[][]> lights) {
        RenderType renderType = RenderTypes.entityCutout(texture);
        int[][] table = lights == null ? null : lights.get(name);
        for (TTMeshPart part : GolemMeshes.get(MESH).parts()) {
            if (!name.equals(part.name())) {
                continue;
            }
            for (int copy = 0; copy < copies; copy++) {
                int[] quadLights = table == null ? null : table[copy];
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(copy * QUARTER_TURN_DEGREES));
                collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, WHITE, quadLights));
                poseStack.popPose();
            }
        }
    }

    private static Map<String, int[][]> sampleLights(Level level, BlockPos pos) {
        Map<String, int[][]> lights = new HashMap<>();
        Vector3f point = new Vector3f();
        Vector3f normal = new Vector3f();
        for (TTMeshPart part : GolemMeshes.get(MESH).parts()) {
            String name = part.name();
            if (!BASE_PART.equals(name) && !TANK_PART.equals(name) && !TRIM_PART.equals(name)) {
                continue;
            }
            int orientations = BASE_PART.equals(name) ? 1 : SIDE_COUNT;
            int[][] table = new int[orientations][part.quadCount()];
            for (int orientation = 0; orientation < orientations; orientation++) {
                Matrix4f placement = new Matrix4f().translation(CENTER, 0.0F, CENTER).rotateX(-SAMPLE_QUARTER_TURN).rotateZ(orientation * SAMPLE_QUARTER_TURN);
                for (int quad = 0; quad < part.quadCount(); quad++) {
                    table[orientation][quad] = sampleQuad(level, pos, part, quad, placement, point, normal);
                }
            }
            lights.put(name, table);
        }
        return lights;
    }

    private static int sampleQuad(Level level, BlockPos pos, TTMeshPart part, int quad, Matrix4f placement, Vector3f point, Vector3f normal) {
        float[] positions = part.positions();
        float[] normals = part.normals();
        point.zero();
        normal.zero();
        for (int vertex = 0; vertex < QUAD_VERTICES; vertex++) {
            int base = (quad * QUAD_VERTICES + vertex) * 3;
            point.add(positions[base], positions[base + 1], positions[base + 2]);
            normal.add(normals[base], normals[base + 1], normals[base + 2]);
        }
        point.div(QUAD_VERTEX_COUNT);
        placement.transformPosition(point);
        placement.transformDirection(normal);
        if (normal.lengthSquared() > 0.0F) {
            normal.normalize();
        }
        point.fma(NORMAL_OFFSET, normal);
        return LevelRenderer.getLightCoords(level, BlockPos.containing(pos.getX() + point.x, pos.getY() + point.y, pos.getZ() + point.z));
    }

    private void submitEssentia(AdvancedAlchemicalFurnaceRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float empty = 1.0F - Math.min(1.0F, (float) state.stored / BlockEntityAdvancedAlchemicalFurnace.MAX_ESSENTIA);
        submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), GOO_SPRITE, GOO_LIGHT, GOO_SURFACE, PANEL_FULL);
        for (int side = 0; side < SIDE_COUNT; side++) {
            poseStack.pushPose();
            turnAboutCenter(poseStack, side);
            submitPanel(poseStack, collector, Sheets.cutoutBlockItemSheet(), BACKING_SPRITE, BACKING_LIGHT, WINDOW_A_BACKING, PANEL_FULL);
            submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), GOO_SPRITE, GOO_LIGHT, WINDOW_A_GOO, empty);
            submitPanel(poseStack, collector, Sheets.cutoutBlockItemSheet(), BACKING_SPRITE, BACKING_LIGHT, WINDOW_B_BACKING, PANEL_FULL);
            submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), GOO_SPRITE, GOO_LIGHT, WINDOW_B_GOO, empty);
            poseStack.popPose();
        }
    }

    private void submitVents(AdvancedAlchemicalFurnaceRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        float cooled = 1.0F - Math.min(1.0F, (float) state.heat / BlockEntityAdvancedAlchemicalFurnace.MAX_POWER);
        for (int side = 0; side < SIDE_COUNT; side++) {
            poseStack.pushPose();
            turnAboutCenter(poseStack, side);
            submitPanel(poseStack, collector, Sheets.translucentBlockItemSheet(), FLAME_SPRITE, FLAME_LIGHT, FLAME, cooled);
            submitPanel(poseStack, collector, Sheets.cutoutBlockItemSheet(), BACKING_SPRITE, BACKING_LIGHT, FLAME_BACKING, PANEL_FULL);
            poseStack.popPose();
        }
    }

    private static void turnAboutCenter(PoseStack poseStack, int side) {
        poseStack.translate(CENTER, 0.0F, CENTER);
        poseStack.mulPose(Axis.YP.rotationDegrees(side * QUARTER_TURN_DEGREES));
        poseStack.translate(-CENTER, 0.0F, -CENTER);
    }

    private void submitPanel(PoseStack poseStack, SubmitNodeCollector collector, RenderType sheet, SpriteId spriteId, int light, PanelPlane plane, float bottom) {
        TextureAtlasSprite sprite = sprites.get(spriteId);
        collector.submitCustomGeometry(poseStack, sheet, (pose, buffer) -> writePanel(sprite.wrap(buffer), pose, plane, bottom, light));
    }

    private static void writePanel(VertexConsumer buffer, PoseStack.Pose pose, PanelPlane plane, float bottom, int light) {
        Vector3f normal = plane.normal();
        float nx = normal.x;
        float ny = normal.y;
        float nz = normal.z;
        panelVertex(buffer, pose, plane, 0.0F, bottom, 0.0F, 1.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 1.0F, bottom, 1.0F, 1.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 1.0F, 1.0F, 1.0F, 0.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 0.0F, 1.0F, 0.0F, 0.0F, light, nx, ny, nz);
        panelVertex(buffer, pose, plane, 0.0F, 1.0F, 0.0F, 0.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 1.0F, 1.0F, 1.0F, 0.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 1.0F, bottom, 1.0F, 1.0F, light, -nx, -ny, -nz);
        panelVertex(buffer, pose, plane, 0.0F, bottom, 0.0F, 1.0F, light, -nx, -ny, -nz);
    }

    private static void panelVertex(VertexConsumer buffer, PoseStack.Pose pose, PanelPlane plane, float px, float py, float u, float v, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, plane.x(px, py), plane.y(px, py), plane.z(px, py)).setColor(WHITE).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private static PanelPlane windowPlane(float startX, float spanX, float z) {
        return new PanelPlane(startX, WINDOW_TOP, z, spanX, 0.0F, 0.0F, 0.0F, -WINDOW_HEIGHT, 0.0F);
    }

    private record PanelPlane(float originX, float originY, float originZ, float rightX, float rightY, float rightZ, float downX, float downY, float downZ) {
        float x(float px, float py) {
            return originX + px * rightX + py * downX;
        }

        float y(float px, float py) {
            return originY + px * rightY + py * downY;
        }

        float z(float px, float py) {
            return originZ + px * rightZ + py * downZ;
        }

        Vector3f normal() {
            Vector3f normal = new Vector3f(rightX, rightY, rightZ).cross(downX, downY, downZ);
            return normal.lengthSquared() > 0.0F ? normal.normalize() : normal.set(0.0F, 1.0F, 0.0F);
        }
    }
}
