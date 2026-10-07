package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTFXPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class SealRenderTypes {
    public static final Identifier RING_TEXTURE = TTIds.rl("textures/misc/seal_area.png");
    public static final Identifier CORNER_TEXTURE = TTIds.rl("textures/misc/frame_corner.png");

    public static final RenderPipeline PIPELINE = TTFXPipelines.translucentTextured(TTIds.rl("pipeline/seal_marker"));
    public static final RenderPipeline SEE_THROUGH_PIPELINE = TTFXPipelines.translucentTexturedNoDepth(TTIds.rl("pipeline/seal_marker_see_through"));

    public static final RenderType ICON = create("thaumaturge_seal_icon", PIPELINE, TextureAtlas.LOCATION_ITEMS);
    public static final RenderType ICON_SEE_THROUGH = create("thaumaturge_seal_icon_see_through", SEE_THROUGH_PIPELINE, TextureAtlas.LOCATION_ITEMS);
    public static final RenderType RING = create("thaumaturge_seal_ring", PIPELINE, RING_TEXTURE);
    public static final RenderType RING_SEE_THROUGH = create("thaumaturge_seal_ring_see_through", SEE_THROUGH_PIPELINE, RING_TEXTURE);
    public static final RenderType CORNER = create("thaumaturge_seal_corner", PIPELINE, CORNER_TEXTURE);
    public static final RenderType CORNER_SEE_THROUGH = create("thaumaturge_seal_corner_see_through", SEE_THROUGH_PIPELINE, CORNER_TEXTURE);

    private SealRenderTypes() {}

    private static RenderType create(String name, RenderPipeline pipeline, Identifier texture) {
        return RenderType.create(name, RenderSetup.builder(pipeline).withTexture("Sampler0", texture).createRenderSetup());
    }

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
        event.registerPipeline(SEE_THROUGH_PIPELINE);
    }
}
