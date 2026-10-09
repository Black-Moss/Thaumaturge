package com.leclowndu93150.thaumaturge.client.casters.architect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class AxisArrowPass {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/architect_arrows.png");
    private static final float ALPHA = 1.0F;
    private static final float NO_ROTATION = 0.0F;
    private static final Direction[][] ACROSS = {{Direction.SOUTH, Direction.UP}, {Direction.EAST, Direction.SOUTH}, {Direction.EAST, Direction.UP}};

    public void draw(MultiBufferSource.BufferSource buffers, Matrix4f pose, Vec3 camera, BlockPos anchor, int ticks, boolean[] shown) {
        float red = ArrowPulse.red(ticks, anchor);
        float green = ArrowPulse.green(ticks, anchor);
        float blue = ArrowPulse.blue(ticks, anchor);
        float originX = (float) (anchor.getX() + OverlayQuad.HALF - camera.x);
        float originY = (float) (anchor.getY() + OverlayQuad.HALF - camera.y);
        float originZ = (float) (anchor.getZ() + OverlayQuad.HALF - camera.z);
        RenderType type = TTFXRenderTypes.architect(TEXTURE);
        VertexConsumer buffer = buffers.getBuffer(type);
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (!shown[axis.ordinal()]) {
                continue;
            }
            Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
            for (Direction across : ACROSS[axis.ordinal()]) {
                OverlayQuad.emit(buffer, pose, originX, originY, originZ, across, along, NO_ROTATION, red, green, blue, ALPHA);
            }
        }
        buffers.endBatch(type);
    }
}
