package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class HierophantModel extends EntityModel<HierophantRenderState> {
    private static final float MILLIS_PER_TICK = 50;
    private static final float BLEND_IN_TICKS = 3;
    private static final float BLEND_OUT_TICKS = 5;
    private static final float RECOIL_TICKS = 10;
    private boolean hammerOnly;
    private final KeyframeAnimation idle;
    private final KeyframeAnimation glide;
    private final KeyframeAnimation hurt;
    private final Map<HierophantAction, KeyframeAnimation> actions = new EnumMap<>(HierophantAction.class);

    public HierophantModel(ModelPart bakedRoot) {
        super(bakedRoot.getChild("root"));
        idle = HierophantIdleAnimation.create().bake(root);
        glide = HierophantGlideAnimation.create().bake(root);
        hurt = HierophantHurtAnimation.create().bake(root);
        actions.put(HierophantAction.SUMMON, HierophantSummonAnimation.create().bake(root));
        actions.put(HierophantAction.CAST, HierophantCastAnimation.create().bake(root));
        actions.put(HierophantAction.SWIPE_RIGHT, HierophantSwipeRightAnimation.create().bake(root));
        actions.put(HierophantAction.SWIPE_LEFT, HierophantSwipeLeftAnimation.create().bake(root));
        actions.put(HierophantAction.NOVA, HierophantNovaAnimation.create().bake(root));
        actions.put(HierophantAction.THROW, HierophantThrowAnimation.create().bake(root));
        actions.put(HierophantAction.DEATH, HierophantDeathAnimation.create().bake(root));
    }

    public void onlyEye() {
        for (ModelPart part : allParts()) {
            part.skipDraw = true;
        }
        root.getChild("body").getChild("head").getChild("eye").skipDraw = false;
    }

    public void onlyHammer() {
        hammerOnly = true;
        for (ModelPart part : allParts()) {
            part.skipDraw = true;
        }
        root.getChild("body").getChild("halo").getChild("halo_segment_6").skipDraw = false;
    }

    public Vector3f palm(boolean left) {
        final String side = left ? "left" : "right";
        final PoseStack pose = new PoseStack();
        root.translateAndRotate(pose);
        final ModelPart body = root.getChild("body");
        body.translateAndRotate(pose);
        final ModelPart arm = body.getChild("arm_" + side);
        arm.translateAndRotate(pose);
        final ModelPart forearm = arm.getChild("forearm_" + side);
        forearm.translateAndRotate(pose);
        final ModelPart hand = forearm.getChild("hand_" + side);
        hand.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f(0, 4.0F / 16, -1.0F / 16));
    }

    @Override
    public void setupAnim(HierophantRenderState state) {
        super.setupAnim(state);
        root.getChild("body").getChild("halo").getChild("halo_segment_6").visible = hammerOnly || state.action != HierophantAction.THROW || state.actionTicks < HierophantAction.THROW.release()
                || state.actionTicks >= 64;
        final KeyframeAnimation action = actions.get(state.action);
        final float blend = action == null
                ? 0
                : state.action == HierophantAction.SUMMON || state.action == HierophantAction.DEATH
                        ? 1
                        : Mth.clamp(Math.min(state.actionTicks / BLEND_IN_TICKS, (state.action.duration() - state.actionTicks) / BLEND_OUT_TICKS), 0, 1);
        final long idleTime = (long) (state.ageInTicks * MILLIS_PER_TICK);
        idle.apply(idleTime, (1 - blend) * (1 - state.movement));
        glide.apply(idleTime, (1 - blend) * state.movement);
        if (action != null) {
            action.apply((long) (state.actionTicks * MILLIS_PER_TICK), blend);
        }
        if (state.action == HierophantAction.IDLE && state.recoil > 0) {
            hurt.apply((long) ((RECOIL_TICKS - state.recoil) * MILLIS_PER_TICK), 0.6F);
        }
    }
}
