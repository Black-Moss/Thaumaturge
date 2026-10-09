package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityRedstoneRelay;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class RelayTextOverlay {
    private static final float TEXT_SCALE = 0.0125F;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final double KNOB_OFFSET = 0.25;
    private static final double TEXT_HEIGHT = 0.35;

    private RelayTextOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos target = lookedAtBlock(mc);
        if (target == null || !(mc.level.getBlockEntity(target) instanceof BlockEntityRedstoneRelay relay)) {
            return;
        }
        Direction facing = mc.level.getBlockState(target).getValue(DiodeBlock.FACING);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        drawKnobValue(event.getPoseStack(), mc, buffers, knobAnchor(target, facing, -1), relay.getOut());
        drawKnobValue(event.getPoseStack(), mc, buffers, knobAnchor(target, facing, 1), relay.getIn());
        buffers.endBatch();
    }

    private static @Nullable BlockPos lookedAtBlock(Minecraft mc) {
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return null;
        }
        HitResult result = mc.hitResult;
        if (result instanceof BlockHitResult hit && result.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos();
        }
        return null;
    }

    private static Vec3 knobAnchor(BlockPos pos, Direction facing, int side) {
        double reach = side * KNOB_OFFSET;
        return new Vec3(pos.getX() + 0.5 + facing.getStepX() * reach, pos.getY() + TEXT_HEIGHT, pos.getZ() + 0.5 + facing.getStepZ() * reach);
    }

    private static void drawKnobValue(PoseStack poseStack, Minecraft mc, MultiBufferSource buffers, Vec3 anchor, int value) {
        Vec3 eye = mc.gameRenderer.getMainCamera().position();
        Vec3 offset = anchor.subtract(eye);
        float yaw = (float) Math.toDegrees(Math.atan2(-offset.x, -offset.z));
        Component label = Component.literal(String.valueOf(value));
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        float left = 1 - mc.font.width(label) / 2;
        mc.font.drawInBatch(label, left, 1.0F, TEXT_COLOR, true, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, 0, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }
}
