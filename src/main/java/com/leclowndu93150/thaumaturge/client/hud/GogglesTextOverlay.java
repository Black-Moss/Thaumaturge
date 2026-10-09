package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.hud.goggles.BillboardTextRenderer;
import com.leclowndu93150.thaumaturge.client.hud.goggles.GogglesReadout;
import com.leclowndu93150.thaumaturge.client.hud.goggles.GogglesReadoutResolver;
import com.leclowndu93150.thaumaturge.client.hud.goggles.PositionedLine;
import com.leclowndu93150.thaumaturge.client.hud.goggles.ReadoutLayout;
import com.leclowndu93150.thaumaturge.client.hud.goggles.ReadoutLayouter;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GogglesTextOverlay {
    private GogglesTextOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos target = targetedBlock(mc);
        if (target == null) {
            return;
        }
        GogglesReadoutResolver.resolve(mc.level, target).ifPresent(readout -> draw(event.getPoseStack(), mc, target, readout));
    }

    private static @Nullable BlockPos targetedBlock(Minecraft mc) {
        boolean viewing = mc.level != null && mc.player != null && !mc.options.hideGui;
        if (!viewing || !GogglesAccess.wearsRevealingGear(mc.player)) {
            return null;
        }
        HitResult result = mc.hitResult;
        return result instanceof BlockHitResult hit && result.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    private static void draw(PoseStack poseStack, Minecraft mc, BlockPos target, GogglesReadout readout) {
        ReadoutLayout layout = ReadoutLayout.STANDARD;
        Vec3 eye = mc.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        List<PositionedLine> lines = ReadoutLayouter.layout(target, readout, layout);
        BillboardTextRenderer.drawAll(poseStack, mc.font, buffers, eye, lines, layout);
        buffers.endBatch();
    }
}
