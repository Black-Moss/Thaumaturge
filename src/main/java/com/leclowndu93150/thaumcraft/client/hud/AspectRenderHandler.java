package com.leclowndu93150.thaumcraft.client.hud;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumcraft.api.items.GogglesAccess;
import com.leclowndu93150.thaumcraft.client.render.aspect.AspectTagWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class AspectRenderHandler {
    private static final int TAGS_PER_ROW = 5;
    private static final float ROW_OFFSET_MULTIPLIER = 1.05F;
    private static final int TAG_ALPHA = 192;
    private static final float TEXT_SCALE = 0.04F;
    private static final float TEXT_Z_OFFSET = 0.1F;
    private static final float TAG_SCALE_GROWTH_CONSTANT = 0.031F;
    private static final float TAG_SCALE_GROWTH_DIVISOR = 10F;
    private static final float TAG_SCALE_CAP = 0.3F;
    private static final float TAG_SCALE_DECAY = 0.005F;

    private static float tagScale = 0.0F;

    private AspectRenderHandler() {
    }

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        Level level = mc.level;
        Player player = mc.player;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.position();

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        if (!GogglesAccess.wearsGoggles(player)) {
            decayTagScale();
            return;
        }

        if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) {
            decayTagScale();
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasSpaceAbove = level.getBlockState(pos.above()).isAir();

        if (be == null) {
            decayTagScale();
            return;
        }
        IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, pos, state, be, hasSpaceAbove ? Direction.UP : blockHitResult.getDirection());
        if (container == null || container.getAspects().isEmpty()) {
            decayTagScale();
            return;
        }

        if (tagScale < TAG_SCALE_CAP) {
            tagScale += TAG_SCALE_GROWTH_CONSTANT - tagScale / TAG_SCALE_GROWTH_DIVISOR;
        }

        drawTags(
                poseStack,
                buffer,
                camPos,
                pos,
                hasSpaceAbove ? Direction.UP : blockHitResult.getDirection(),
                container.getAspects(),
                LightCoordsUtil.FULL_BRIGHT,
                camera
        );
        buffer.endBatch();
    }

    private static void decayTagScale() {
        if (tagScale > 0.0F) {
            tagScale -= TAG_SCALE_DECAY;
            if (tagScale < 0.0F) tagScale = 0.0F;
        }
    }

    public static void drawTags(
            PoseStack poseStack,
            MultiBufferSource buffer,
            Vec3 cameraPos,
            BlockPos pos,
            @Nullable Direction side,
            AspectList aspects,
            int light,
            Camera camera
    ) {
        if (aspects == null || aspects.isEmpty() || tagScale <= 0.0F)
            return;

        Minecraft mc = Minecraft.getInstance();

        int ox = 0;
        int oy = 0;
        int oz = 0;

        if (side != null) {
            ox = side.getStepX();
            oy = side.getStepY();
            oz = side.getStepZ();
        }

        int current = 0;
        int left = aspects.size();
        float shiftY = 0;

        Font font = mc.font;

        for (AspectInstance aspect : aspects.entries()) {

            int div = Math.min(left, TAGS_PER_ROW);

            if (current >= TAGS_PER_ROW) {
                current = 0;
                shiftY -= tagScale * ROW_OFFSET_MULTIPLIER;
                left -= TAGS_PER_ROW;

                if (left < TAGS_PER_ROW)
                    div = left;
            }

            float shiftX = (current - div / 2f + 0.5f) * tagScale * 4f;
            shiftX *= tagScale;

            poseStack.pushPose();

            poseStack.translate(
                    pos.getX() + 0.5 + tagScale * 2f * ox - cameraPos.x,
                    pos.getY() + 0.5 - shiftY + tagScale * 2f * oy - cameraPos.y,
                    pos.getZ() + 0.5 + tagScale * 2f * oz - cameraPos.z
            );

            poseStack.mulPose(camera.rotation());

            poseStack.translate(shiftX, 0, 0);

            poseStack.scale(tagScale, tagScale, tagScale);

            VertexConsumer consumer = buffer.getBuffer(RenderTypes.entityTranslucent(aspect.aspect().value().texture()));
            AspectTagWorldRenderer.renderQuad(poseStack, consumer, aspect.aspect(), TAG_ALPHA / 255.0F, false, light);

            int amount = aspect.amount();

            if (amount > 0) {
                poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

                poseStack.translate(0, 6, TEXT_Z_OFFSET);

                String txt = Integer.toString(amount);

                float width = font.width(txt);

                font.drawInBatch(
                        txt,
                        14 - width,
                        1,
                        0xFF111111,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        light
                );

                poseStack.translate(0, 0, TEXT_Z_OFFSET);

                font.drawInBatch(
                        txt,
                        13 - width,
                        0,
                        0xFFFFFFFF,
                        false,
                        poseStack.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        light
                );
            }

            poseStack.popPose();

            current++;
        }
    }
}
