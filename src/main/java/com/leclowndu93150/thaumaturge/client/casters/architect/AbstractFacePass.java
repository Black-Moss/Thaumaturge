package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class AbstractFacePass {
    private static final Direction[] FACES = Direction.values();

    protected abstract Identifier texture();

    protected abstract void emit(VertexConsumer buffer, Matrix4f pose, FaceFrame frame, float originX, float originY, float originZ, int neighbourMask);

    public final void draw(MultiBufferSource.BufferSource buffers, Matrix4f pose, Vec3 camera, PreviewSnapshot snapshot) {
        RenderType type = TTFXRenderTypes.architect(texture());
        VertexConsumer buffer = buffers.getBuffer(type);
        for (int index = 0; index < snapshot.size(); index++) {
            BlockPos pos = snapshot.position(index);
            int mask = snapshot.neighbourMask(index);
            for (Direction face : FACES) {
                if (NeighbourMask.has(mask, face)) {
                    continue;
                }
                float originX = (float) (pos.getX() + OverlayQuad.HALF - camera.x) + face.getStepX() * OverlayQuad.HALF;
                float originY = (float) (pos.getY() + OverlayQuad.HALF - camera.y) + face.getStepY() * OverlayQuad.HALF;
                float originZ = (float) (pos.getZ() + OverlayQuad.HALF - camera.z) + face.getStepZ() * OverlayQuad.HALF;
                emit(buffer, pose, FaceFrame.of(face), originX, originY, originZ, mask);
            }
        }
        buffers.endBatch(type);
    }
}
