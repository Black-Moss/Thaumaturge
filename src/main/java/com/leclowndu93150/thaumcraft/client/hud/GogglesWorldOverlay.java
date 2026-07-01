package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class GogglesWorldOverlay {
    private static final int ROW_SIZE = 5;
    private static final float TAG_SCALE_CAP = 0.3F;
    private static final float TAG_SCALE_GROWTH = 0.031F;
    private static final float TAG_SCALE_DECAY_DIVISOR = 10.0F;
    private static final float ROW_SPACING = 1.05F;
    private static final float SPACE_ABOVE_LIFT = 0.4F;
    private static final float TAG_ALPHA = 0.75F;
    private static final float TEXT_SCALE = 0.04F;
    private static final int TEXT_SHADOW_COLOR = 0xFF111111;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private static float tagScale;
    private static @Nullable BlockPos lastTarget;

    private GogglesWorldOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!GogglesAccess.wearsGoggles(mc.player)) {
            resetAnimation();
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            resetAnimation();
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof IAspectContainer container)) {
            resetAnimation();
            return;
        }
        AspectList aspects = container.getAspects();
        if (aspects.isEmpty()) {
            resetAnimation();
            return;
        }
        if (!pos.equals(lastTarget)) {
            tagScale = 0.0F;
            lastTarget = pos.immutable();
        }
        if (tagScale < TAG_SCALE_CAP) {
            tagScale = tagScale + (TAG_SCALE_GROWTH - tagScale / TAG_SCALE_DECAY_DIVISOR);
        }

        boolean spaceAbove = mc.level.getBlockState(pos.above()).isAir();
        Direction dir = spaceAbove ? Direction.UP : hit.getDirection();
        double y = pos.getY() + (spaceAbove ? SPACE_ABOVE_LIFT : 0.0F);
        drawTags(event.getPoseStack(), mc, pos.getX(), y, pos.getZ(), aspects, dir);
    }

    private static void resetAnimation() {
        tagScale = 0.0F;
        lastTarget = null;
    }

    private static void drawTags(PoseStack poseStack, Minecraft mc,
                                 double x, double y, double z, AspectList aspects, Direction dir) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cam = camera.position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Font font = mc.font;
        List<AspectInstance> entries = aspects.sortedByTag();
        float yawDeg = (float) Math.toDegrees(Math.atan2(cam.x - (x + 0.5), cam.z - (z + 0.5)));

        int current = 0;
        float rowShift = 0.0F;
        int left = entries.size();
        for (AspectInstance entry : entries) {
            int div = Math.min(left, ROW_SIZE);
            if (current >= ROW_SIZE) {
                current = 0;
                rowShift += tagScale * ROW_SPACING;
                left -= ROW_SIZE;
                if (left < ROW_SIZE) {
                    div = left % ROW_SIZE;
                }
            }
            float shift = (current - div / 2.0F + 0.5F) * tagScale * 4.0F;
            shift *= tagScale;

            poseStack.pushPose();
            poseStack.translate(
                    x + 0.5 + tagScale * 2.0F * dir.getStepX() - cam.x,
                    y + 0.5 + tagScale * 2.0F * dir.getStepY() - cam.y,
                    z + 0.5 + tagScale * 2.0F * dir.getStepZ() - cam.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yawDeg + 180.0F));
            poseStack.scale(-1.0F, 1.0F, 1.0F);
            poseStack.translate(shift, rowShift, 0.0);

            poseStack.pushPose();
            poseStack.scale(tagScale, tagScale, tagScale);
            AspectTagWorldRenderer.renderQuad(poseStack,
                    buffers.getBuffer(RenderTypes.entityTranslucent(entry.aspect().value().texture())),
                    entry.aspect(), TAG_ALPHA, false, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();

            String amount = Integer.toString(entry.amount());
            int width = font.width(amount);
            poseStack.pushPose();
            poseStack.scale(tagScale * TEXT_SCALE, -tagScale * TEXT_SCALE, tagScale * TEXT_SCALE);
            poseStack.translate(0.0, 6.0, -0.1);
            font.drawInBatch(amount, 14 - width, 1, TEXT_SHADOW_COLOR, false,
                    poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
            poseStack.translate(0.0, 0.0, -0.1);
            font.drawInBatch(amount, 13 - width, 0, TEXT_COLOR, false,
                    poseStack.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();

            poseStack.popPose();
            current++;
        }
        buffers.endBatch();
    }
}
