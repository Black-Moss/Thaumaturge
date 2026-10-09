package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GolemRenderer extends EntityRenderer<EntityThaumaturgeGolem, GolemRenderState> {
    private static final float SHADOW_RADIUS = 0.3F;
    private static final float ANCHOR_DROP = 1.5F;
    private static final float MAX_WALK_SPEED = 1.0F;
    private static final float GRINDER_DEGREES_PER_TICK = 12.0F;
    private static final float GRINDER_ATTACK_DEGREES = 180.0F;
    private static final float GHOST_ALPHA = 0.15F;
    private static final float XRAY_CHANNEL = 0.25F;
    private static final float XRAY_ALPHA = 0.25F;
    private static final int GHOST_COLOR = ARGB.white(GHOST_ALPHA);
    private static final int XRAY_COLOR = ARGB.colorFromFloat(XRAY_ALPHA, XRAY_CHANNEL, XRAY_CHANNEL, XRAY_CHANNEL);
    private static final int OPAQUE_WHITE = -1;
    private static final int OPAQUE_ALPHA = 255;
    private static final int HAULED_SLOT = 1;
    private static final int MAX_PASSES = 2;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float HELD_ITEM_SCALE = 0.5F;
    private static final float HELD_ITEM_LIFT = 2.0F * PIXEL;
    private static final float HELD_ITEM_FORWARD = 8.0F * PIXEL;
    private static final float HOOK_PARTIAL_TICK = 0.0F;
    private static final String XRAY_TYPE_PREFIX = "thaumaturge_golem_xray_";
    private static final String XRAY_SAMPLER = "Sampler0";
    private static final GolemPartModel.AttachPoint[] LIMB_POINTS = {GolemPartModel.AttachPoint.ARMS, GolemPartModel.AttachPoint.LEGS};
    private static final Map<Identifier, RenderType> XRAY_CACHE = Collections.synchronizedMap(new HashMap<>());

    private final CopperGolemRig rig;
    private final ItemModelResolver itemModelResolver;
    private final GolemAccessoryRenderTable accessoryTable;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
        this.accessoryTable = GolemAccessoryRenderTable.collect(context);
        this.rig = new CopperGolemRig(context.bakeLayer(ModelLayers.COPPER_GOLEM));
        this.itemModelResolver = context.getItemModelResolver();
    }

    static boolean isHoldingSealDisplayer(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (ISealDisplayer.class.isInstance(player.getItemInHand(hand).getItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GolemRenderState createRenderState() {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(EntityThaumaturgeGolem golem, GolemRenderState state, float partialTick) {
        super.extractRenderState(golem, state, partialTick);
        extractVisibility(golem, state);
        extractBuild(golem, state);
        extractItems(golem, state);
        ArmedEntityRenderState.extractArmedEntityRenderState(golem, state, itemModelResolver, partialTick);
        extractMotion(golem, state, partialTick);
    }

    private static void extractMotion(EntityThaumaturgeGolem golem, GolemRenderState state, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, golem.yBodyRotO, golem.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, golem.yHeadRotO, golem.yHeadRot);
        float relativeHead = Mth.wrapDegrees(headYaw - bodyYaw);
        float pitch = Mth.lerp(partialTick, golem.xRotO, golem.getXRot());
        float walkPosition = golem.walkAnimation.position(partialTick);
        float walkSpeed = Math.min(golem.walkAnimation.speed(partialTick), MAX_WALK_SPEED);
        double moveX = golem.getX() - golem.xo;
        double moveZ = golem.getZ() - golem.zo;
        state.bodyRot = bodyYaw;
        state.headYawDelta = relativeHead;
        state.yRot = relativeHead;
        state.pitch = pitch;
        state.xRot = pitch;
        state.walkPos = walkPosition;
        state.walkAnimationPos = walkPosition;
        state.walkSpeed = walkSpeed;
        state.walkAnimationSpeed = walkSpeed;
        state.idleAnimationState.start(0);
        state.attackTime = golem.getAttackAnim(partialTick);
        state.speedSq = moveX * moveX + moveZ * moveZ;
        state.yawDelta = golem.getYRot() - golem.yRotO;
        state.wheelRotation = golem.wheelRotation;
        state.grinderRot = GRINDER_DEGREES_PER_TICK * state.ageInTicks + GRINDER_ATTACK_DEGREES * state.attackTime;
        state.combat = golem.isInCombat();
    }

    private static void extractBuild(EntityThaumaturgeGolem golem, GolemRenderState state) {
        state.props = golem.properties();
        state.color = golem.color();
        state.accessories = golem.getAccessories();
        state.accessoryStates = golem.syncedAccessoryStates();
    }

    private static void extractVisibility(EntityThaumaturgeGolem golem, GolemRenderState state) {
        LocalPlayer player = Minecraft.getInstance().player;
        state.invisible = golem.isInvisible();
        state.ghost = state.invisible && player != null && !golem.isInvisibleTo(player);
        state.xray = player != null && player.isShiftKeyDown() && isHoldingSealDisplayer(player) && !player.hasLineOfSight(golem);
    }

    private void extractItems(EntityThaumaturgeGolem golem, GolemRenderState state) {
        ItemStack held = golem.getMainHandItem();
        state.holdingItem = !held.isEmpty();
        state.heldItemIsBlock = held.getItem() instanceof BlockItem;
        itemModelResolver.updateForLiving(state.heldItem, held, ItemDisplayContext.FIXED, golem);
        List<ItemStack> contents = golem.hands().contents();
        ItemStack hauled = contents.size() > HAULED_SLOT ? contents.get(HAULED_SLOT) : ItemStack.EMPTY;
        state.haulingItem = !hauled.isEmpty();
        state.haulerItemIsBlock = hauled.getItem() instanceof BlockItem;
        itemModelResolver.updateForLiving(state.haulerItem, hauled, ItemDisplayContext.FIXED, golem);
    }

    @Override
    public void submit(GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.props == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        poseStack.scale(-CopperGolemRig.SCALE, -CopperGolemRig.SCALE, CopperGolemRig.SCALE);
        poseStack.translate(0.0F, -ANCHOR_DROP, 0.0F);
        rig.setupAnim(state);
        for (PassStyle style : stylesFor(state)) {
            submitParts(rig, accessoryTable, state, poseStack, collector, style.xray(), style.color());
        }
        poseStack.popPose();
    }

    private static List<PassStyle> stylesFor(GolemRenderState state) {
        List<PassStyle> styles = new ArrayList<>(MAX_PASSES);
        if (!state.invisible) {
            styles.add(new PassStyle(false, OPAQUE_WHITE));
        } else if (state.ghost) {
            styles.add(new PassStyle(false, GHOST_COLOR));
        }
        if (state.xray) {
            styles.add(new PassStyle(true, XRAY_COLOR));
        }
        return styles;
    }

    public static void submitParts(CopperGolemRig golemRig, GolemAccessoryRenderTable accessories, GolemRenderState renderState, PoseStack stack, SubmitNodeCollector nodes, boolean xray, int color) {
        new Pass(golemRig, accessories, renderState, stack, nodes, xray, color).run();
    }

    private static RenderType renderType(Identifier texture, boolean xray, int color) {
        if (xray) {
            return XRAY_CACHE.computeIfAbsent(texture, GolemRenderer::createXrayType);
        }
        return ARGB.alpha(color) < OPAQUE_ALPHA ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }

    private static RenderType createXrayType(Identifier texture) {
        return RenderType.create(XRAY_TYPE_PREFIX + texture.getPath().hashCode(),
                RenderSetup.builder(TTRenderPipelines.ENTITY_TRANSLUCENT_NO_DEPTH).withTexture(XRAY_SAMPLER, texture).createRenderSetup());
    }

    private static List<GolemPartModel> modelsAt(GolemProperties props, GolemPartModel.AttachPoint point) {
        List<GolemPartModel> models = new ArrayList<>();
        collectModels(models, props.head(), point);
        collectModels(models, props.arms(), point);
        collectModels(models, props.legs(), point);
        collectModels(models, props.addon(), point);
        return models;
    }

    private static void collectModels(List<GolemPartModel> out, GolemPart part, GolemPartModel.AttachPoint point) {
        for (GolemPartModel model : part.models()) {
            if (model.attachPoint() == point) {
                out.add(model);
            }
        }
    }

    private record PassStyle(boolean xray, int color) {
    }

    private record Pass(CopperGolemRig rig, GolemAccessoryRenderTable table, GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, boolean xray, int color) {
        void run() {
            GolemProperties props = state.props;
            Identifier materialTexture = props.material().texture();
            RenderType bodyType = renderType(GolemSkins.forMaterial(materialTexture), xray, color);
            collector.submitModel(rig, state, pose, bodyType, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
            if (!xray) {
                RenderType eyesType = ARGB.alpha(color) < OPAQUE_ALPHA ? RenderTypes.entityTranslucent(GolemSkins.EYES) : RenderTypes.eyes(GolemSkins.EYES);
                collector.submitModel(rig, state, pose, eyesType, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, state.outlineColor, null);
            }
            submitAnchor(GolemAccessoryAnchor.HEAD, GolemPartModel.AttachPoint.HEAD, materialTexture);
            submitAnchor(GolemAccessoryAnchor.BODY, GolemPartModel.AttachPoint.BODY, materialTexture);
            for (GolemPartModel.AttachPoint point : LIMB_POINTS) {
                for (GolemPartModel model : modelsAt(props, point)) {
                    submitLimb(model, point, GolemPartModel.LimbSide.RIGHT, materialTexture);
                    submitLimb(model, point, GolemPartModel.LimbSide.LEFT, materialTexture);
                }
            }
            submitHeldItem();
        }

        private void submitLimb(GolemPartModel model, GolemPartModel.AttachPoint point, GolemPartModel.LimbSide side, Identifier materialTexture) {
            pose.pushPose();
            rig.translateToLimb(pose, point, side);
            submitMesh(model, side, materialTexture);
            pose.popPose();
        }

        private void submitAnchor(GolemAccessoryAnchor anchor, GolemPartModel.AttachPoint point, Identifier materialTexture) {
            pose.pushPose();
            rig.translateToAnchor(pose, anchor);
            for (GolemPartModel model : modelsAt(state.props, point)) {
                submitMesh(model, GolemPartModel.LimbSide.MIDDLE, materialTexture);
            }
            if (!xray) {
                table.submit(anchor, state, pose, collector, color);
                if (anchor == GolemAccessoryAnchor.BODY) {
                    GolemEquipmentRenderer.submitColorBand(state, pose, collector, color);
                }
            }
            pose.popPose();
        }

        private void submitMesh(GolemPartModel model, GolemPartModel.LimbSide side, Identifier materialTexture) {
            GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(model);
            for (TTMeshPart part : GolemMeshes.get(model.objModel()).parts()) {
                submitPart(hook, part, side, renderType(GolemMeshes.texture(part, textureFallback(model, part, materialTexture)), xray, color));
            }
        }

        private static Identifier textureFallback(GolemPartModel model, TTMeshPart part, Identifier materialTexture) {
            Identifier own = model.texture();
            return own != null && !model.useMaterialTextureForObjectPart(part.name()) ? own : materialTexture;
        }

        private void submitPart(GolemPartRenderHook hook, TTMeshPart part, GolemPartModel.LimbSide side, RenderType type) {
            pose.pushPose();
            hook.preRenderObjectPart(part.name(), state, pose, side, HOOK_PARTIAL_TICK);
            int light = state.lightCoords;
            int tint = color;
            collector.submitCustomGeometry(pose, type, (matrix, buffer) -> GolemMeshes.renderPart(part, matrix, buffer, light, tint));
            if (xray) {
                pose.popPose();
                return;
            }
            hook.postRenderObjectPart(part.name(), state, pose, collector, side);
            pose.popPose();
        }

        private void submitHeldItem() {
            if (xray || !state.holdingItem) {
                return;
            }
            pose.pushPose();
            rig.translateToAnchor(pose, GolemAccessoryAnchor.BODY);
            pose.translate(0.0F, HELD_ITEM_LIFT, -HELD_ITEM_FORWARD);
            pose.scale(HELD_ITEM_SCALE, HELD_ITEM_SCALE, HELD_ITEM_SCALE);
            state.heldItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }
    }
}
