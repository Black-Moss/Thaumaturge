package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderContext;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;

public final class GolemEquipmentRenderer {
    private static final Identifier WHITE_WOOL = Identifier.withDefaultNamespace("textures/block/white_wool.png");
    private static final Identifier COLOR_BAND = TTIds.rl("models/mesh/golem_color_band.ttmesh");
    private static final int OPAQUE_ALPHA = 255;
    private static final String GLASS = "glass";

    private GolemEquipmentRenderer() {}

    public static void submit(Identifier mesh, PoseStack pose, SubmitNodeCollector collector, GolemAccessoryRenderContext context) {
        submit(mesh, pose, collector, context.lightCoords(), context.color());
    }

    public static void submitColorBand(GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, int color) {
        if (state.color > 0) {
            int tint = ARGB.color(ARGB.alpha(color), DyeColor.byId(state.color - 1).getTextureDiffuseColor());
            submit(COLOR_BAND, pose, collector, state.lightCoords, tint);
        }
    }

    private static void submit(Identifier mesh, PoseStack pose, SubmitNodeCollector collector, int light, int color) {
        for (TTMeshPart part : GolemMeshes.get(mesh).parts()) {
            Identifier texture = GolemMeshes.texture(part, WHITE_WOOL);
            collector.submitCustomGeometry(pose, renderType(texture, color), (matrix, buffer) -> GolemMeshes.renderPart(part, matrix, buffer, light, color));
        }
    }

    private static RenderType renderType(Identifier texture, int color) {
        if (ARGB.alpha(color) < OPAQUE_ALPHA) {
            return TTFlatRenderTypes.entityTranslucentNoDepthWrite(texture);
        }
        return texture.getPath().contains(GLASS) ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
    }
}
