package com.leclowndu93150.thaumcraft.client.fx.render.pipeline;

import com.leclowndu93150.thaumcraft.TCIds;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCRenderPipelines {
    private static final BlendFunction TC_ADDITIVE = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE);
    private static final DepthStencilState TEST_NO_WRITE = new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false);
    private static final DepthStencilState ALWAYS_NO_WRITE = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    public static final RenderPipeline FX_ADDITIVE = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/fx_additive"))
            .withColorTargetState(new ColorTargetState(TC_ADDITIVE))
            .withDepthStencilState(TEST_NO_WRITE)
            .build();

    public static final RenderPipeline FX_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/fx_translucent"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(TEST_NO_WRITE)
            .build();

    public static final RenderPipeline FX_ADDITIVE_NO_DEPTH = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/fx_additive_no_depth"))
            .withColorTargetState(new ColorTargetState(TC_ADDITIVE))
            .withDepthStencilState(ALWAYS_NO_WRITE)
            .build();

    public static final RenderPipeline FX_TRANSLUCENT_NO_DEPTH = RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/fx_translucent_no_depth"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(ALWAYS_NO_WRITE)
            .build();

    public static final RenderPipeline GUI_TEXTURED_ADDITIVE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(TCIds.MODID, "pipeline/gui_textured_additive"))
            .withVertexShader("core/position_tex_color")
            .withFragmentShader("core/position_tex_color")
            .withSampler("Sampler0")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .build();

    @SubscribeEvent
    static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(FX_ADDITIVE);
        event.registerPipeline(FX_TRANSLUCENT);
        event.registerPipeline(FX_ADDITIVE_NO_DEPTH);
        event.registerPipeline(FX_TRANSLUCENT_NO_DEPTH);
        event.registerPipeline(GUI_TEXTURED_ADDITIVE);
    }

    private TCRenderPipelines() {}
}
