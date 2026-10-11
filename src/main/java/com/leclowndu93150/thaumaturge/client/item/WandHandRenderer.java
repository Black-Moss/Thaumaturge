package com.leclowndu93150.thaumaturge.client.item;

import net.minecraft.client.renderer.texture.OverlayTexture;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderers;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.model.WandItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class WandHandRenderer {
    private static final float BASE_SCALE = 0.8F;
    private static final float ITEM_SCALE = 0.4F;
    private static final float WAND_GRIP_Y = 0.9F;
    private static final float STAFF_GRIP_Y = 1.36F;
    private static final float TIP_RAMP_TICKS = 5.0F;
    private static final float TIP_DEGREES = -34.0F;
    private static final float SWAY_DEGREES = 3.2F;
    private static final float SWAY_SECONDARY = 0.45F;
    private static final float SWAY_PITCH_RATE = 0.093F;
    private static final float SWAY_PITCH_DRIFT_RATE = 0.041F;
    private static final float SWAY_ROLL_RATE = 0.071F;
    private static final float SWAY_ROLL_DRIFT_RATE = 0.029F;
    private static final float SWAY_ROLL_PHASE = 1.9F;
    private static final float TREMBLE_DEGREES = 0.7F;
    private static final float TREMBLE_PITCH_RATE = 2.9F;
    private static final float TREMBLE_ROLL_RATE = 4.3F;

    private WandHandRenderer() {}

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        ItemStack stack = event.getItemStack();
        if (player == null || !(stack.getItem() instanceof ItemWand)) {
            return;
        }
        event.setCanceled(true);
        WandItemSpecialRenderer.WandArg arg = WandItemSpecialRenderer.extract(stack);
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float mirror = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float equip = event.getEquipProgress();
        float swing = event.getSwingProgress();
        boolean using = player.isUsingItem() && player.getUsedItemHand() == event.getHand();

        poseStack.pushPose();
        if (!using) {
            applyMissSway(poseStack, mirror, swing);
        }
        applyCarryPose(poseStack, mirror, equip, swing);
        if (using) {
            applyDrawPose(poseStack, mirror, player.getTicksUsingItem() + partial);
        }
        applyModelBasis(poseStack, mirror, arg.rod().staff());
        if (using) {
            applyUseWave(poseStack, mirror, arg.rod().staff(), chargesOnUse(stack), player.getTicksUsingItem() + partial);
        }
        WandTipTracker.capture(poseStack, WandItemSpecialRenderer.tipModelY(arg));
        WandRenderers.render(WandItemSpecialRenderer.context(stack, arg, poseStack, collector, event.getPackedLight(), OverlayTexture.NO_OVERLAY, null, true));
        poseStack.popPose();
    }

    private static void applyMissSway(PoseStack poseStack, float mirror, float swing) {
        float lateral = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        float vertical = Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F);
        float forward = Mth.sin(swing * Mth.PI);
        poseStack.translate(mirror * -lateral * 0.4F, vertical * 0.2F, -forward * 0.2F);
    }

    private static void applyCarryPose(PoseStack poseStack, float mirror, float equip, float swing) {
        poseStack.translate(mirror * 0.7F * BASE_SCALE, -0.65F * BASE_SCALE - equip * 0.6F, -0.9F * BASE_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * 45.0F));
        float snap = Mth.sin(swing * swing * Mth.PI);
        float arc = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * -snap * 20.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * -arc * 20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-arc * 80.0F));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
    }

    private static void applyDrawPose(PoseStack poseStack, float mirror, float useTicks) {
        float drawTicks = Math.max(0.0F, useTicks - 1.0F);
        float draw = drawTicks / 20.0F;
        draw = Math.min(1.0F, (draw * draw + draw * 2.0F) / 3.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * -18.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * -12.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
        poseStack.translate(mirror * -0.9F, 0.2F, 0.0F);
        if (draw > 0.1F) {
            poseStack.translate(0.0F, Mth.sin((drawTicks - 0.1F) * 1.3F) * 0.01F * (draw - 0.1F), 0.0F);
        }
        poseStack.translate(0.0F, 0.0F, draw * 0.1F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * -335.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * -50.0F));
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.scale(1.0F, 1.0F, 1.0F + draw * 0.2F);
        poseStack.translate(0.0F, -0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * 50.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * 335.0F));
    }

    private static void applyModelBasis(PoseStack poseStack, float mirror, boolean staff) {
        poseStack.translate(mirror * -0.5F, -0.5F, -0.5F);
        if (staff) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
        }
        poseStack.translate(mirror * 0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, 1.1F, 1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }

    private static void applyUseWave(PoseStack poseStack, float mirror, boolean staff, boolean charged, float useTicks) {
        float ramp = Mth.clamp(useTicks / TIP_RAMP_TICKS, 0.0F, 1.0F);
        float settle = ramp * ramp * (3.0F - 2.0F * ramp);
        float pitch;
        float roll;
        if (charged) {
            pitch = Mth.sin(useTicks * TREMBLE_PITCH_RATE) * TREMBLE_DEGREES;
            roll = Mth.cos(useTicks * TREMBLE_ROLL_RATE) * TREMBLE_DEGREES;
        } else {
            pitch = (Mth.sin(useTicks * SWAY_PITCH_RATE) + Mth.sin(useTicks * SWAY_PITCH_DRIFT_RATE) * SWAY_SECONDARY) * SWAY_DEGREES;
            roll = (Mth.cos(useTicks * SWAY_ROLL_RATE) + Mth.sin(useTicks * SWAY_ROLL_DRIFT_RATE + SWAY_ROLL_PHASE) * SWAY_SECONDARY) * SWAY_DEGREES;
        }
        float grip = staff ? STAFF_GRIP_Y : WAND_GRIP_Y;
        poseStack.translate(0.0F, grip, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(TIP_DEGREES * settle + pitch * settle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * roll * settle));
        poseStack.translate(0.0F, -grip, 0.0F);
    }

    private static boolean chargesOnUse(ItemStack wand) {
        ItemStackTemplate socketed = wand.get(TTDataComponents.SOCKETED_FOCUS.get());
        if (socketed == null) {
            return false;
        }
        Spell spell = FocusItems.spell(socketed.create());
        return spell != null && spell.style() == CastStyle.CHARGED;
    }
}
