package com.leclowndu93150.thaumaturge.client.screen.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;

record PreviewFluidVertexConsumer(VertexConsumer delegate, PoseStack.Pose pose, BlockPos pos) implements VertexConsumer {
    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(pose, x - (pos.getX() & 15), y - (pos.getY() & 15), z - (pos.getZ() & 15));
        return this;
    }
    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        delegate.setColor(r, g, b, a);
        return this;
    }
    @Override
    public VertexConsumer setColor(int color) {
        delegate.setColor(color);
        return this;
    }
    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }
    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }
    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(240, 240);
        return this;
    }
    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(pose, x, y, z);
        return this;
    }
    @Override
    public VertexConsumer setLineWidth(float width) {
        delegate.setLineWidth(width);
        return this;
    }
}
