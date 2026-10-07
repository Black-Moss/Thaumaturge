package com.leclowndu93150.thaumaturge.client.effect.rendertype;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTFXPipelines;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Function;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTFXRenderTypes {
    private static final String SAMPLER = "Sampler0";
    private static final String SECOND_SAMPLER = "Sampler1";

    private static final RenderPipeline DIOPTRA_PIPELINE = TTFXPipelines.additiveTextured(TTIds.rl("pipeline/dioptra"));
    private static final RenderPipeline WARD_RUNES_PIPELINE = TTFXPipelines.additiveTextured(TTIds.rl("pipeline/ward_runes"), TTIds.rl("core/ward_add"));
    private static final RenderPipeline ARCHITECT_PIPELINE = TTFXPipelines.additiveTexturedNoDepth(TTIds.rl("pipeline/architect_overlay"));

    public static final RenderType SPARKLE = RenderType.create("tc_sparkle", RenderSetup.builder(TTRenderPipelines.SPARKLE_CULLED).createRenderSetup());
    public static final RenderType WARD_RUNES = RenderType.create("thaumaturge_ward_runes",
            RenderSetup.builder(WARD_RUNES_PIPELINE).withTexture(SAMPLER, TextureAtlas.LOCATION_BLOCKS).createRenderSetup());
    public static final RenderType RIFT_GLOW = endPortal("tc_rift_glow", TTRenderPipelines.RIFT_GLOW);
    public static final RenderType RIFT_GLOW_NO_DEPTH = endPortal("tc_rift_glow_no_depth", TTRenderPipelines.RIFT_GLOW_NO_DEPTH);
    public static final RenderType RIFT_SOLID = endPortal("tc_rift_solid", TTRenderPipelines.RIFT_SOLID);
    public static final RenderType HOLE_SURFACE = RenderType.create("tc_hole_surface",
            RenderSetup.builder(TTRenderPipelines.HOLE_SURFACE).withTexture(SAMPLER, AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    private static final Function<Identifier, RenderType> ADDITIVE = lit("tc_fx_additive", TTRenderPipelines.FX_ADDITIVE);
    private static final Function<Identifier, RenderType> ADDITIVE_NO_DEPTH = lit("tc_fx_additive_no_depth", TTRenderPipelines.FX_ADDITIVE_NO_DEPTH);
    private static final Function<Identifier, RenderType> ADDITIVE_ALPHA_TEST = lit("tc_fx_additive_alpha_test", TTRenderPipelines.FX_ADDITIVE_ALPHA_TEST);
    private static final Function<Identifier, RenderType> TRANSLUCENT = lit("tc_fx_translucent", TTRenderPipelines.FX_TRANSLUCENT);
    private static final Function<Identifier, RenderType> TRANSLUCENT_NO_DEPTH = lit("tc_fx_translucent_no_depth", TTRenderPipelines.FX_TRANSLUCENT_NO_DEPTH);
    private static final Function<Identifier, RenderType> ENTITY_ADDITIVE = lit("tc_entity_additive", TTRenderPipelines.ENTITY_ADDITIVE_EMISSIVE);
    private static final Function<Identifier, RenderType> ENTITY_ADDITIVE_UNLIT = unlit("tc_entity_additive_unlit", TTRenderPipelines.ENTITY_ADDITIVE_EMISSIVE);
    private static final Function<Identifier, RenderType> ADDITIVE_SORTED = Util.memoize(
            texture -> RenderType.create("tc_fx_additive_sorted", RenderSetup.builder(TTRenderPipelines.FX_ADDITIVE).withTexture(SAMPLER, texture).useLightmap().sortOnUpload().createRenderSetup()));
    private static final Function<Identifier, RenderType> DIOPTRA = unlit("tc_dioptra", DIOPTRA_PIPELINE);
    private static final Function<Identifier, RenderType> ARCHITECT = unlit("thaumaturge_architect", ARCHITECT_PIPELINE);

    private TTFXRenderTypes() {}

    public static RenderType additive(Identifier texture) {
        return ADDITIVE.apply(texture);
    }

    public static RenderType additiveNoDepth(Identifier texture) {
        return ADDITIVE_NO_DEPTH.apply(texture);
    }

    public static RenderType additiveAlphaTest(Identifier texture) {
        return ADDITIVE_ALPHA_TEST.apply(texture);
    }

    public static RenderType additiveSorted(Identifier texture) {
        return ADDITIVE_SORTED.apply(texture);
    }

    public static RenderType translucent(Identifier texture) {
        return TRANSLUCENT.apply(texture);
    }

    public static RenderType translucentNoDepth(Identifier texture) {
        return TRANSLUCENT_NO_DEPTH.apply(texture);
    }

    public static RenderType entityAdditive(Identifier texture) {
        return ENTITY_ADDITIVE.apply(texture);
    }

    public static RenderType entityAdditiveUnlit(Identifier texture) {
        return ENTITY_ADDITIVE_UNLIT.apply(texture);
    }

    public static RenderType dioptra(Identifier texture) {
        return DIOPTRA.apply(texture);
    }

    public static RenderType architect(Identifier texture) {
        return ARCHITECT.apply(texture);
    }

    private static Function<Identifier, RenderType> lit(String name, RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderType.create(name, RenderSetup.builder(pipeline).withTexture(SAMPLER, texture).useLightmap().createRenderSetup()));
    }

    private static Function<Identifier, RenderType> unlit(String name, RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderType.create(name, RenderSetup.builder(pipeline).withTexture(SAMPLER, texture).createRenderSetup()));
    }

    private static RenderType endPortal(String name, RenderPipeline pipeline) {
        return RenderType.create(name, RenderSetup.builder(pipeline).withTexture(SAMPLER, AbstractEndPortalRenderer.END_PORTAL_LOCATION)
                .withTexture(SECOND_SAMPLER, AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());
    }

    @SubscribeEvent
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(DIOPTRA_PIPELINE);
        event.registerPipeline(WARD_RUNES_PIPELINE);
        event.registerPipeline(ARCHITECT_PIPELINE);
    }
}
