package com.leclowndu93150.thaumaturge.client.hud.goggles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BillboardTextRenderer {
    private static final int BACKGROUND_NONE = 0;

    private BillboardTextRenderer() {}

    public static void drawAll(PoseStack poseStack, Font font, MultiBufferSource buffers, Vec3 camera, List<PositionedLine> lines, ReadoutLayout layout) {
        for (int i = 0; i < lines.size(); i++) {
            draw(poseStack, font, buffers, camera, lines.get(i), layout);
        }
    }

    private static void draw(PoseStack poseStack, Font font, MultiBufferSource buffers, Vec3 camera, PositionedLine line, ReadoutLayout layout) {
        Vec3 position = line.position();
        Vec3 toCamera = camera.subtract(position).normalize();
        float facing = (float) Math.atan2(toCamera.x, toCamera.z) + Mth.PI;
        poseStack.pushPose();
        poseStack.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
        poseStack.mulPose(Axis.YP.rotation(facing));
        poseStack.scale(-layout.textScale(), -layout.textScale(), layout.textScale());
        float left = -font.width(line.text()) * layout.halfWidthFactor();
        font.drawInBatch(line.text(), left, layout.baseline(), layout.textColor(), true, poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH, BACKGROUND_NONE, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }
}
