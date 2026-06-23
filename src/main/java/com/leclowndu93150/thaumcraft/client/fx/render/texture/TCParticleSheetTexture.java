package com.leclowndu93150.thaumcraft.client.fx.render.texture;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.client.fx.render.rendertype.ArcRenderType;
import com.leclowndu93150.thaumcraft.client.fx.render.rendertype.BeamRenderType;
import com.leclowndu93150.thaumcraft.client.fx.render.rendertype.BoltRenderType;
import com.leclowndu93150.thaumcraft.client.fx.render.rendertype.EssentiaStreamRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCParticleSheetTexture {
    private TCParticleSheetTexture() {}

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TextureManager tm = Minecraft.getInstance().getTextureManager();
            tm.registerForNextReload(TCParticleLayer.SHEET);
            tm.registerForNextReload(TCParticleLayer.NITOR_CORE_SHEET);
            tm.registerForNextReload(EssentiaStreamRenderType.TEXTURE);
            tm.registerForNextReload(ArcRenderType.TEXTURE);
            tm.registerForNextReload(BoltRenderType.TEXTURE);
            tm.registerForNextReload(BeamRenderType.BEAM);
            tm.registerForNextReload(BeamRenderType.BEAML);
            tm.registerForNextReload(BeamRenderType.BEAMH);
            tm.registerForNextReload(BeamRenderType.NODE);
        });
    }
}
