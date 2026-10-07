package com.leclowndu93150.thaumaturge.mixin.client.renderer.rendertype;

import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderSetup.class)
public abstract class RenderSetupMixin {
    private static final String OVERLAY_SAMPLER = "Sampler1";
    private static final String LIGHTMAP_SAMPLER = "Sampler2";

    @Shadow
    @Final
    RenderPipeline pipeline;

    @Shadow
    @Final
    boolean useLightmap;

    @Shadow
    @Final
    boolean useOverlay;

    @ModifyReturnValue(method = "getTextures", at = @At("RETURN"))
    private Map<String, RenderSetup.TextureAndSampler> thaumaturge$bindVertexSamplers(Map<String, RenderSetup.TextureAndSampler> textures) {
        VertexFormat format = pipeline.getVertexFormat();
        boolean overlay = !useOverlay && format.contains(VertexFormatElement.UV1);
        boolean lightmap = !useLightmap && format.contains(VertexFormatElement.UV2);
        if (!overlay && !lightmap || !IrisCompat.shaderPackInUse()) {
            return textures;
        }
        Map<String, RenderSetup.TextureAndSampler> result = new HashMap<>(textures);
        Minecraft minecraft = Minecraft.getInstance();
        if (overlay) {
            result.putIfAbsent(OVERLAY_SAMPLER,
                    new RenderSetup.TextureAndSampler(minecraft.gameRenderer.overlayTexture().getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
        if (lightmap) {
            result.putIfAbsent(LIGHTMAP_SAMPLER, new RenderSetup.TextureAndSampler(minecraft.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)));
        }
        return result;
    }
}
