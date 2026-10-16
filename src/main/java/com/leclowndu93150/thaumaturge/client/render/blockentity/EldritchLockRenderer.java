package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchLockRenderer implements BlockEntityRenderer<BlockEntityEldritchLock, EldritchLockRenderState> {
    private static final Identifier CUBE_TEXTURE = TTIds.rl("textures/entity/eldritch_cube.png");
    private static final String CUBE_PART = "cube";
    private static final int CUBE_TEXTURE_SIZE = 64;
    private static final float CUBE_PIXELS = 16.0F;
    private static final float CUBE_ORIGIN = -CUBE_PIXELS / 2.0F;

    private static final float CENTER = 0.5F;
    private static final float DOOR_HALF_SIZE = 2.5F;
    private static final float DOOR_LOW = CENTER - DOOR_HALF_SIZE;
    private static final float DOOR_HIGH = CENTER + DOOR_HALF_SIZE;

    private static final int CUBES_PER_ARM = 4;
    private static final float CUBE_SIZE = 0.5F;
    private static final float FIRST_CUBE_REACH = CENTER + CUBE_SIZE / 2.0F;
    private static final int[][] ARM_STEPS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    private static final float PHASE_DIVISOR_TICKS = 20.0F;
    private static final float CUBE_PHASE_TICKS = 10.0F;
    private static final float ARM_PHASE_TICKS = 20.0F;
    private static final double BREATH_PERIOD_TICKS = Math.PI * 2.0 * PHASE_DIVISOR_TICKS;
    private static final float MIDDLE_WIDTH = 1.0F;
    private static final float MIDDLE_SWELL = 0.2F;
    private static final float END_WIDTH = 1.4F;
    private static final float END_SWELL = 0.1F;

    private static final int RETRACT_INTERVAL_TICKS = 20;
    private static final int ARM_STAGGER_TICKS = 5;

    private static final float TABLET_DROP = 0.09F;
    private static final float TABLET_STANDOFF = 0.025F;
    private static final float TABLET_SCALE = 1.025641F;

    private static final int OUTLINE_NONE = 0;
    private static final int DISPLAY_SEED = 0;
    private static final int VIEW_DISTANCE = 64;

    private final ModelPart cube;
    private final ItemModelResolver itemModelResolver;
    private @Nullable ItemStack tabletStack;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(CUBE_PART, CubeListBuilder.create().texOffs(0, 0).addBox(CUBE_ORIGIN, CUBE_ORIGIN, CUBE_ORIGIN, CUBE_PIXELS, CUBE_PIXELS, CUBE_PIXELS), PartPose.ZERO);
        this.cube = LayerDefinition.create(mesh, CUBE_TEXTURE_SIZE, CUBE_TEXTURE_SIZE).bakeRoot().getChild(CUBE_PART);
    }

    @Override
    public EldritchLockRenderState createRenderState() {
        return new EldritchLockRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchLock lock, EldritchLockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(lock, state, partialTicks, cameraPosition, breakProgress);
        state.facing = lock.getBlockState().getValue(BlockEldritchLock.FACING);
        state.count = lock.getCount();
        Level level = lock.getLevel();
        long gameTime = level == null ? 0L : level.getGameTime();
        state.animationTime = (float) (gameTime % BREATH_PERIOD_TICKS) + partialTicks;
        if (level != null) {
            state.lightCoords = LevelRenderer.getLightCoords(level, lock.getBlockPos().relative(state.facing));
        }
        state.tablet = state.count >= 0 ? resolveTablet(level) : null;
    }

    private ItemStackRenderState resolveTablet(@Nullable Level level) {
        if (tabletStack == null) {
            tabletStack = new ItemStack(TTItems.RUNED_TABLET.get());
        }
        ItemStackRenderState resolved = new ItemStackRenderState();
        itemModelResolver.updateForTopItem(resolved, tabletStack, ItemDisplayContext.FIXED, level, null, DISPLAY_SEED);
        return resolved;
    }

    @Override
    public void submit(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        BlockPos pos = state.blockPos;
        Direction.Axis axis = state.facing.getAxis();
        collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> writeDoor(pose, buffer, pos, axis));
        poseStack.pushPose();
        poseStack.translate(CENTER, CENTER, CENTER);
        BlockFacingPose.northBased(poseStack, state.facing);
        submitArms(state, poseStack, collector);
        ItemStackRenderState tablet = state.tablet;
        if (tablet != null) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -TABLET_DROP, -(CENTER + TABLET_STANDOFF));
            poseStack.scale(TABLET_SCALE, TABLET_SCALE, TABLET_SCALE);
            tablet.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void submitArms(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        RenderType type = RenderTypes.entitySolid(CUBE_TEXTURE);
        for (int arm = 0; arm < ARM_STEPS.length; arm++) {
            int stepX = ARM_STEPS[arm][0];
            int stepY = ARM_STEPS[arm][1];
            int remaining = cubesLeft(state.count, arm);
            for (int index = 0; index < remaining; index++) {
                float reach = FIRST_CUBE_REACH + index * CUBE_SIZE;
                float across = CUBE_SIZE * width(index, (state.animationTime + (index + 1) * CUBE_PHASE_TICKS + arm * ARM_PHASE_TICKS) / PHASE_DIVISOR_TICKS);
                poseStack.pushPose();
                poseStack.translate(stepX * reach, stepY * reach, 0.0F);
                poseStack.scale(stepX != 0 ? CUBE_SIZE : across, stepY != 0 ? CUBE_SIZE : across, across);
                collector.submitModelPart(cube, poseStack, type, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
                poseStack.popPose();
            }
        }
    }

    private static int cubesLeft(int count, int arm) {
        if (count < 0) {
            return CUBES_PER_ARM;
        }
        return Mth.clamp(CUBES_PER_ARM - Math.floorDiv(count + arm * ARM_STAGGER_TICKS, RETRACT_INTERVAL_TICKS), 0, CUBES_PER_ARM);
    }

    private static float width(int index, float phase) {
        boolean end = index == 0 || index == CUBES_PER_ARM - 1;
        float swell = Mth.sin(phase);
        return end ? END_WIDTH + END_SWELL * swell : MIDDLE_WIDTH + MIDDLE_SWELL * swell;
    }

    private static void writeDoor(PoseStack.Pose pose, VertexConsumer buffer, BlockPos pos, Direction.Axis axis) {
        switch (axis) {
            case X -> EldritchPortalSurface.quad(pose, buffer, pos, CENTER, DOOR_LOW, DOOR_LOW, CENTER, DOOR_HIGH, DOOR_LOW, CENTER, DOOR_HIGH, DOOR_HIGH, CENTER, DOOR_LOW, DOOR_HIGH);
            case Y -> EldritchPortalSurface.quad(pose, buffer, pos, DOOR_LOW, CENTER, DOOR_LOW, DOOR_HIGH, CENTER, DOOR_LOW, DOOR_HIGH, CENTER, DOOR_HIGH, DOOR_LOW, CENTER, DOOR_HIGH);
            case Z -> EldritchPortalSurface.quad(pose, buffer, pos, DOOR_LOW, DOOR_LOW, CENTER, DOOR_LOW, DOOR_HIGH, CENTER, DOOR_HIGH, DOOR_HIGH, CENTER, DOOR_HIGH, DOOR_LOW, CENTER);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchLock lock) {
        return new AABB(lock.getBlockPos()).inflate(DOOR_HALF_SIZE - CENTER);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }
}
