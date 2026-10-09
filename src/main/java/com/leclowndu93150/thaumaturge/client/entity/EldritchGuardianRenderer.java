package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class EldritchGuardianRenderer extends MobRenderer<EntityEldritchGuardian, EldritchGuardianRenderState, EldritchGuardianModel> {
    private static final Identifier TEXTURE = TTIds.rl("textures/entity/eldritch_guardian.png");
    private static final float SHADOW_RADIUS = 0.5F;
    private static final float MAX_ALPHA = 0.6F;
    private static final double FADE_START_SQ = 256.0;
    private static final double FADE_END_SQ_HARD = 576.0;
    private static final double FADE_END_SQ_DEFAULT = 1024.0;

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel(context.bakeLayer(TTModelLayers.ELDRITCH_GUARDIAN)), SHADOW_RADIUS);
    }

    @Override
    public EldritchGuardianRenderState createRenderState() {
        return new EldritchGuardianRenderState();
    }

    @Override
    public void extractRenderState(EntityEldritchGuardian entity, EldritchGuardianRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.armLiftL = entity.arms().leftLift();
        state.armLiftR = entity.arms().rightLift();
        extractCombat(entity, state, partialTicks);
        state.alpha = fadeAlpha(entity);
    }

    static void extractCombat(LivingEntity entity, EldritchGuardianRenderState state, float partialTicks) {
        HumanoidArm swingingArm = entity.swingingArm == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
        state.meleeSwing = entity.getAttackAnim(partialTicks);
        state.hurtTime = Math.max(0.0F, entity.hurtTime - partialTicks);
        state.leftHanded = swingingArm == HumanoidArm.LEFT;
    }

    private static float fadeAlpha(EntityEldritchGuardian entity) {
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (camera == null) {
            return MAX_ALPHA;
        }
        double distanceSq = entity.distanceToSqr(camera);
        if (distanceSq < FADE_START_SQ) {
            return MAX_ALPHA;
        }
        double fadeEndSq = entity.level().getDifficulty() == Difficulty.HARD ? FADE_END_SQ_HARD : FADE_END_SQ_DEFAULT;
        double range = fadeEndSq - FADE_START_SQ;
        double progress = Math.min(range, distanceSq - FADE_START_SQ) / range;
        return (float) ((1.0 - progress) * MAX_ALPHA);
    }

    @Override
    protected RenderType getRenderType(EldritchGuardianRenderState state, boolean visible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getModelTint(EldritchGuardianRenderState state) {
        return ARGB.white(Mth.clamp(state.alpha, 0.0F, 1.0F));
    }

    @Override
    public Identifier getTextureLocation(EldritchGuardianRenderState state) {
        return TEXTURE;
    }
}
