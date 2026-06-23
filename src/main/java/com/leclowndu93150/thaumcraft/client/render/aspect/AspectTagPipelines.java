package com.leclowndu93150.thaumcraft.client.render.aspect;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class AspectTagPipelines {
    public static final RenderPipeline GUI_TEXTURED_ADDITIVE = RenderPipeline.builder()
            .withLocation(TCIds.rl("pipeline/gui_textured_additive"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();

    private AspectTagPipelines() {}

    @SubscribeEvent
    public static void onRegister(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_TEXTURED_ADDITIVE);
    }
}
