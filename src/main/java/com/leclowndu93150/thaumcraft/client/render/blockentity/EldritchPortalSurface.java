package com.leclowndu93150.thaumcraft.client.render.blockentity;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.fx.render.pipeline.TCRenderPipelines;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;

public final class EldritchPortalSurface {
    public static final Identifier TUNNEL_TEXTURE = TCIds.rl("textures/misc/tunnel.png");
    public static final Identifier PARTICLE_FIELD_TEXTURE = TCIds.rl("textures/misc/particlefield.png");

    public static final RenderType SURFACE = RenderType.create(
            "tc_eldritch_portal_surface",
            RenderSetup.builder(TCRenderPipelines.PORTAL_SURFACE)
                    .withTexture("Sampler0", TUNNEL_TEXTURE)
                    .withTexture("Sampler1", PARTICLE_FIELD_TEXTURE)
                    .createRenderSetup());

    private EldritchPortalSurface() {}

    public static void quad(PoseStack.Pose pose, VertexConsumer buffer,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4) {
        Matrix4fc mat = pose.pose();
        buffer.addVertex(mat, x1, y1, z1);
        buffer.addVertex(mat, x2, y2, z2);
        buffer.addVertex(mat, x3, y3, z3);
        buffer.addVertex(mat, x4, y4, z4);
    }
}
