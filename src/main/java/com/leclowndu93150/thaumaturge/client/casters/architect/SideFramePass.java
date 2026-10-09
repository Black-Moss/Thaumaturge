package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public final class SideFramePass extends AbstractFacePass {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/frame_side.png");
    private static final float ALPHA = 0.1F;
    private static final float NO_ROTATION = 0.0F;

    @Override
    protected Identifier texture() {
        return TEXTURE;
    }

    @Override
    protected void emit(VertexConsumer buffer, Matrix4f pose, FaceFrame frame, float originX, float originY, float originZ, int neighbourMask) {
        OverlayQuad.emit(buffer, pose, originX, originY, originZ, frame.xAxis(), frame.yAxis(), NO_ROTATION, OverlayQuad.WHITE, OverlayQuad.WHITE, OverlayQuad.WHITE, ALPHA);
    }
}
