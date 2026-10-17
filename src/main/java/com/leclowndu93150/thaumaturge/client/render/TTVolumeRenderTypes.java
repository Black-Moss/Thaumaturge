package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import java.util.function.Function;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class TTVolumeRenderTypes {
    private static final Function<Identifier, RenderType> TRANSLUCENT_TEST_ONLY = Util.memoize(texture -> RenderType.create("tc_translucent_test_only",
            RenderSetup.builder(TTRenderPipelines.ENTITY_TRANSLUCENT_TEST_ONLY).withTexture("Sampler0", texture).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));

    private TTVolumeRenderTypes() {}

    public static RenderType entityTranslucentTestOnly(Identifier texture) {
        return TRANSLUCENT_TEST_ONLY.apply(texture);
    }

    public static RenderType emissiveBlockAtlas() {
        return RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
    }
}
