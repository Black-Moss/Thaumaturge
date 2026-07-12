package com.leclowndu93150.thaumcraft.client.item;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.casters.WandTipTracker;
import com.leclowndu93150.thaumcraft.client.model.WandItemSpecialRenderer;
import com.leclowndu93150.thaumcraft.content.wands.ItemWand;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class WandHandRenderer {
    private static final float BASE_SCALE = 0.8F;
    private static final float ITEM_SCALE = 0.4F;
    private static final float USE_TILT_MAX_TICKS = 3.0F;
    private static final float USE_TILT_DEGREES = 60.0F;
    private static final float WAVE_ROLL_PERIOD = 10.0F;
    private static final float WAVE_PITCH_PERIOD = 15.0F;
    private static final float WAVE_DEGREES = 10.0F;

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
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        float mirror = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float equip = event.getEquipProgress();
        float swing = event.getSwingProgress();
        boolean using = player.isUsingItem() && player.getUsedItemHand() == event.getHand();

        poseStack.pushPose();
        if (!using) {
            float swaySin = Mth.sin(swing * Mth.PI);
            float swaySqrtSin = Mth.sin(Mth.sqrt(swing) * Mth.PI);
            poseStack.translate(mirror * -swaySqrtSin * 0.4F,
                    Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F) * 0.2F,
                    -swaySin * 0.2F);
        }
        poseStack.translate(mirror * 0.7F * BASE_SCALE,
                -0.65F * BASE_SCALE - equip * 0.6F,
                -0.9F * BASE_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * 45.0F));
        float swingCurve = Mth.sin(swing * swing * Mth.PI);
        float swingSqrtCurve = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(mirror * -swingCurve * 20.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * -swingSqrtCurve * 20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-swingSqrtCurve * 80.0F));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

        if (using) {
            float bowTicks = Math.max(0.0F, player.getTicksUsingItem() + partial - 1.0F);
            float draw = bowTicks / 20.0F;
            draw = (draw * draw + draw * 2.0F) / 3.0F;
            if (draw > 1.0F) {
                draw = 1.0F;
            }
            poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * -18.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(mirror * -12.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
            poseStack.translate(mirror * -0.9F, 0.2F, 0.0F);
            if (draw > 0.1F) {
                poseStack.translate(0.0F, Mth.sin((bowTicks - 0.1F) * 1.3F) * 0.01F * (draw - 0.1F), 0.0F);
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

        poseStack.translate(mirror * -0.5F, -0.5F, -0.5F);
        if (arg.rod().staff()) {
            poseStack.translate(0.0F, 0.5F, 0.0F);
        }
        poseStack.translate(mirror * 0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, 1.1F, 1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        if (using) {
            float useTicks = player.getTicksUsingItem() + partial;
            float tilt = Math.min(useTicks, USE_TILT_MAX_TICKS);
            poseStack.translate(0.0F, 1.0F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(mirror * 10.0F));
            poseStack.mulPose(Axis.XN.rotationDegrees(USE_TILT_DEGREES * (tilt / USE_TILT_MAX_TICKS)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(
                    mirror * Mth.sin(useTicks / WAVE_ROLL_PERIOD) * WAVE_DEGREES));
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    Mth.sin(useTicks / WAVE_PITCH_PERIOD) * WAVE_DEGREES));
            poseStack.translate(0.0F, -1.0F, 0.0F);
        }

        WandTipTracker.capture(poseStack, WandItemSpecialRenderer.tipModelY(arg));
        WandItemSpecialRenderer.submitParts(arg, poseStack, collector, event.getPackedLight());
        poseStack.popPose();
    }
}
