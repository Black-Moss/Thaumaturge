package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public final class CornerFramePass extends AbstractFacePass {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/frame_corner.png");
    private static final float ALPHA = 0.66F;

    @Override
    protected Identifier texture() {
        return TEXTURE;
    }

    @Override
    protected void emit(VertexConsumer buffer, Matrix4f pose, FaceFrame frame, float originX, float originY, float originZ, int neighbourMask) {
        for (int slot = 0; slot < frame.cornerCount(); slot++) {
            if (frame.corner(slot).isOpen(neighbourMask)) {
                OverlayQuad.emit(buffer, pose, originX, originY, originZ, frame.xAxis(), frame.yAxis(), frame.cornerRotation(slot), OverlayQuad.WHITE, OverlayQuad.WHITE, OverlayQuad.WHITE, ALPHA);
            }
        }
    }
}
