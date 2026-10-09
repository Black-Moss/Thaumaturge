package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class JarRenderer implements BlockEntityRenderer<BlockEntityJar, JarRenderState> {
    public static final Identifier ANIMATED_GLOW_LOCATION = TTIds.rl("block/animatedglow");
    public static final SpriteId ANIMATED_GLOW_SPRITE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ANIMATED_GLOW_LOCATION);
    public static final float FLUID_MIN = 0.25F;
    public static final float FLUID_MAX = 0.75F;
    public static final float FLUID_BASE_Y = 0.0625F;
    public static final float FLUID_MAX_HEIGHT = 0.625F;

    private static final Identifier LABEL_TEXTURE = TTIds.rl("textures/entity/label.png");
    private static final Identifier BRINE_TEXTURE = TTIds.rl("textures/entity/jarbrine.png");
    private static final int OPAQUE = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final float SIDE_V_BOTTOM = 0.9375F;
    private static final float CENTER = 0.5F;
    private static final float PLATE_HEIGHT = 0.4F;
    private static final float PLATE_NEAR = 0.184F;
    private static final float PLATE_FAR = 0.816F;
    private static final float PLATE_HALF = 0.25F;
    private static final float ICON_HALF = 0.19F;
    private static final float ICON_LIFT = 0.001F;
    private static final float YAW_NORTH = 180.0F;
    private static final float YAW_EAST = 90.0F;
    private static final float YAW_WEST = 270.0F;
    private static final int TILT_PERIOD = 4;
    private static final float TILT_BIAS = 2.0F;
    private static final float BRACE_HALF = 0.5F * 0.390625F * 1.001F;
    private static final float BRACE_MIN = CENTER - BRACE_HALF;
    private static final float BRACE_MAX = CENTER + BRACE_HALF;
    private static final float BRACE_BOTTOM_Y = 0.75F;
    private static final float BRACE_TOP_Y = 0.890625F;
    private static final float LID_MIN = 0.375F;
    private static final float LID_MAX = 0.625F;
    private static final float LID_BOTTOM_Y = 0.875F;
    private static final float LID_TOP_Y = 1.0F;
    private static final float BRINE_TEXTURE_WIDTH = 64.0F;
    private static final float BRINE_TEXTURE_HEIGHT = 32.0F;
    private static final int BIT_X = 4;
    private static final int BIT_Y = 2;
    private static final int BIT_Z = 1;
    private static final int FALLBACK_PLATE_INDEX = 0;
    private static final float[][] FLUID_RING_TOP = {{FLUID_MIN, FLUID_MIN}, {FLUID_MIN, FLUID_MAX}, {FLUID_MAX, FLUID_MAX}, {FLUID_MAX, FLUID_MIN}};
    private static final float[][] FLUID_RING_BOTTOM = {{FLUID_MIN, FLUID_MIN}, {FLUID_MAX, FLUID_MIN}, {FLUID_MAX, FLUID_MAX}, {FLUID_MIN, FLUID_MAX}};
    private static final Wall[] FLUID_WALLS = {new Wall(FLUID_MIN, FLUID_MIN, FLUID_MIN, FLUID_MAX, -1.0F, 0.0F, 0.0F), new Wall(FLUID_MAX, FLUID_MAX, FLUID_MAX, FLUID_MIN, 1.0F, 0.0F, 0.0F),
            new Wall(FLUID_MAX, FLUID_MIN, FLUID_MIN, FLUID_MIN, 0.0F, 0.0F, -1.0F), new Wall(FLUID_MIN, FLUID_MAX, FLUID_MAX, FLUID_MAX, 0.0F, 0.0F, 1.0F)};
    private static final Plate[] PLATES_BY_HORIZONTAL_INDEX = {new Plate(CENTER, PLATE_FAR, 0.0F), new Plate(PLATE_NEAR, CENTER, YAW_WEST), new Plate(CENTER, PLATE_NEAR, YAW_NORTH),
            new Plate(PLATE_FAR, CENTER, YAW_EAST)};

    private static final Prism BRACE = new Prism(BRACE_MIN, BRACE_BOTTOM_Y, BRACE_MIN, BRACE_MAX, BRACE_TOP_Y, BRACE_MAX,
            List.of(panel(0, 1, 0, corner(BIT_Y, 38, 24), corner(BIT_Y | BIT_Z, 38, 30), corner(BIT_X | BIT_Y | BIT_Z, 44, 30), corner(BIT_X | BIT_Y, 44, 24)),
                    panel(0, 0, -1, corner(0, 32, 32), corner(BIT_Y, 32, 30), corner(BIT_X | BIT_Y, 38, 30), corner(BIT_X, 38, 32)),
                    panel(0, 0, 1, corner(BIT_X | BIT_Z, 50, 32), corner(BIT_X | BIT_Y | BIT_Z, 50, 30), corner(BIT_Y | BIT_Z, 44, 30), corner(BIT_Z, 44, 32)),
                    panel(-1, 0, 0, corner(BIT_Z, 38, 32), corner(BIT_Y | BIT_Z, 38, 30), corner(BIT_Y, 44, 30), corner(0, 44, 32)),
                    panel(1, 0, 0, corner(BIT_X, 56, 32), corner(BIT_X | BIT_Y, 56, 30), corner(BIT_X | BIT_Y | BIT_Z, 50, 30), corner(BIT_X | BIT_Z, 50, 32))));
    private static final Prism LID = new Prism(LID_MIN, LID_BOTTOM_Y, LID_MIN, LID_MAX, LID_TOP_Y, LID_MAX,
            List.of(panel(0, 0, -1, corner(0, 0, 29), corner(BIT_Y, 0, 27), corner(BIT_X | BIT_Y, 4, 27), corner(BIT_X, 4, 29)),
                    panel(0, 0, 1, corner(BIT_X | BIT_Z, 8, 29), corner(BIT_X | BIT_Y | BIT_Z, 8, 27), corner(BIT_Y | BIT_Z, 12, 27), corner(BIT_Z, 12, 29)),
                    panel(-1, 0, 0, corner(BIT_Z, 4, 29), corner(BIT_Y | BIT_Z, 4, 27), corner(BIT_Y, 8, 27), corner(0, 8, 29)),
                    panel(1, 0, 0, corner(BIT_X, 12, 29), corner(BIT_X | BIT_Y, 12, 27), corner(BIT_X | BIT_Y | BIT_Z, 16, 27), corner(BIT_X | BIT_Z, 16, 29))));

    private static final List<Prism> PRISMS_BOTH = List.of(BRACE, LID);
    private static final List<Prism> PRISMS_BRACE_ONLY = List.of(BRACE);
    private static final List<Prism> PRISMS_LID_ONLY = List.of(LID);
    private static final List<Prism> PRISMS_NONE = List.of();

    private final SpriteGetter sprites;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public JarRenderState createRenderState() {
        return new JarRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityJar jar, JarRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(jar, state, partialTicks, cameraPosition, breakProgress);
        Level level = jar.getLevel();
        Holder<IAspect> held = resolve(level, jar.aspectKey());
        Holder<IAspect> filter = resolve(level, jar.aspectFilterKey());
        state.braced = jar.isBlocked();
        state.capacity = jar.capacity();
        state.amount = held != null ? jar.amount() : 0;
        state.aspectColor = held != null ? OPAQUE | held.value().color() : 0;
        state.connectedAbove = level != null && EssentiaFlowHandler.transport(level, jar.getBlockPos().above(), Direction.DOWN) != null;
        Direction facing = jar.facing();
        state.facing = facing != null ? facing : Direction.NORTH;
        state.filterAspect = filter;
        state.filterTexture = filter != null ? filter.value().texture() : null;
        state.hasFilter = state.filterTexture != null;
        state.filterColor = filter != null ? OPAQUE | filter.value().color() : 0;
    }

    private static @Nullable Holder<IAspect> resolve(@Nullable Level level, @Nullable ResourceKey<IAspect> key) {
        return level == null || key == null ? null : EssentiaTransportHelper.resolve(level, key);
    }

    @Override
    public void submit(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Identifier labelIcon = state.hasFilter ? state.filterTexture : null;
        submitContents(state, poseStack, collector);
        if (labelIcon != null) {
            submitLabel(state, labelIcon, poseStack, collector);
        }
        for (Prism prism : activePrisms(state)) {
            submitPrism(prism, state.lightCoords, poseStack, collector);
        }
    }

    private void submitContents(JarRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.amount <= 0) {
            return;
        }
        TextureAtlasSprite glow = sprites.get(ANIMATED_GLOW_SPRITE);
        submitFluid(state.amount, state.capacity, state.aspectColor, state.lightCoords, glow, poseStack, collector);
    }

    private static List<Prism> activePrisms(JarRenderState state) {
        if (state.braced) {
            return state.connectedAbove ? PRISMS_BOTH : PRISMS_BRACE_ONLY;
        }
        return state.connectedAbove ? PRISMS_LID_ONLY : PRISMS_NONE;
    }

    public static void submitFluid(float amount, int capacity, int colour, int light, TextureAtlasSprite sprite, PoseStack poseStack, SubmitNodeCollector collector) {
        float fill = Math.min(1.0F, amount / capacity);
        float height = FLUID_BASE_Y + FLUID_MAX_HEIGHT * fill;
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            fluidQuadTop(wrapped, pose, height, colour, light);
            fluidQuadBottom(wrapped, pose, colour, light);
            for (Wall wall : FLUID_WALLS) {
                fluidQuadSide(wrapped, pose, wall.startX(), wall.startZ(), wall.endX(), wall.endZ(), height, colour, light, wall.nx(), wall.ny(), wall.nz());
            }
        });
    }

    public static void fluidQuadTop(VertexConsumer buffer, PoseStack.Pose pose, float height, int colour, int light) {
        horizontalFace(buffer, pose, FLUID_RING_TOP, height, 1.0F, colour, light);
    }

    public static void fluidQuadBottom(VertexConsumer buffer, PoseStack.Pose pose, int colour, int light) {
        horizontalFace(buffer, pose, FLUID_RING_BOTTOM, FLUID_BASE_Y, -1.0F, colour, light);
    }

    private static void horizontalFace(VertexConsumer buffer, PoseStack.Pose pose, float[][] ring, float y, float ny, int colour, int light) {
        for (float[] xz : ring) {
            addVertex(buffer, pose, xz[0], y, xz[1], xz[0], xz[1], colour, light, 0.0F, ny, 0.0F);
        }
    }

    public static void fluidQuadSide(VertexConsumer buffer, PoseStack.Pose pose, float startX, float startZ, float endX, float endZ, float height, int colour, int light, float nx, float ny, float nz) {
        float vLow = SIDE_V_BOTTOM - height;
        float[] xs = {startX, endX, endX, startX};
        float[] zs = {startZ, endZ, endZ, startZ};
        float[] ys = {FLUID_BASE_Y, FLUID_BASE_Y, height, height};
        float[] us = {FLUID_MIN, FLUID_MAX, FLUID_MAX, FLUID_MIN};
        float[] vs = {vLow, vLow, SIDE_V_BOTTOM, SIDE_V_BOTTOM};
        for (int slot = 0; slot < xs.length; slot++) {
            addVertex(buffer, pose, xs[slot], ys[slot], zs[slot], us[slot], vs[slot], colour, light, nx, ny, nz);
        }
    }

    public static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int colour, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private static void submitLabel(JarRenderState state, Identifier iconTexture, PoseStack poseStack, SubmitNodeCollector collector) {
        Direction facing = state.facing;
        Plate plate = PLATES_BY_HORIZONTAL_INDEX[facing.getAxis().isHorizontal() ? facing.get2DDataValue() : FALLBACK_PLATE_INDEX];
        float tilt = (state.blockPos.getX() + facing.ordinal()) % TILT_PERIOD - TILT_BIAS;
        int light = state.lightCoords;
        int iconColour = state.filterColor;
        poseStack.pushPose();
        poseStack.translate(plate.x(), PLATE_HEIGHT, plate.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(plate.yaw()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tilt));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(LABEL_TEXTURE), (pose, buffer) -> plateQuad(pose, buffer, PLATE_HALF, WHITE, light));
        poseStack.translate(0.0F, 0.0F, ICON_LIFT);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(iconTexture), (pose, buffer) -> plateQuad(pose, buffer, ICON_HALF, iconColour, light));
        poseStack.popPose();
    }

    private static void plateQuad(PoseStack.Pose pose, VertexConsumer buffer, float half, int colour, int light) {
        float[][] corners = {{-half, -half, 0.0F, 1.0F}, {half, -half, 1.0F, 1.0F}, {half, half, 1.0F, 0.0F}, {-half, half, 0.0F, 0.0F}};
        for (float[] c : corners) {
            addVertex(buffer, pose, c[0], c[1], 0.0F, c[2], c[3], colour, light, 0.0F, 0.0F, -1.0F);
        }
    }

    private static void submitPrism(Prism prism, int light, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(BRINE_TEXTURE), (pose, buffer) -> {
            for (Panel panel : prism.panels()) {
                for (Corner corner : panel.corners()) {
                    int mask = corner.mask();
                    float x = (mask & BIT_X) != 0 ? prism.maxX() : prism.minX();
                    float y = (mask & BIT_Y) != 0 ? prism.maxY() : prism.minY();
                    float z = (mask & BIT_Z) != 0 ? prism.maxZ() : prism.minZ();
                    addVertex(buffer, pose, x, y, z, corner.pixelU() / BRINE_TEXTURE_WIDTH, corner.pixelV() / BRINE_TEXTURE_HEIGHT, WHITE, light, panel.nx(), panel.ny(), panel.nz());
                }
            }
        });
    }

    private static Corner corner(int mask, int pixelU, int pixelV) {
        return new Corner(mask, pixelU, pixelV);
    }

    private static Panel panel(float nx, float ny, float nz, Corner... corners) {
        return new Panel(nx, ny, nz, List.of(corners));
    }

    private record Plate(float x, float z, float yaw) {
    }

    private record Wall(float startX, float startZ, float endX, float endZ, float nx, float ny, float nz) {
    }

    private record Corner(int mask, int pixelU, int pixelV) {
    }

    private record Panel(float nx, float ny, float nz, List<Corner> corners) {
    }

    private record Prism(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, List<Panel> panels) {
    }
}
