package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TaintOverlayTextures {
    private static final TaintOverlayPattern PATTERN = new VeinTaintOverlayPattern();
    private static final Map<Model<?>, Identifier> CACHE = new IdentityHashMap<>();
    private static int nextId;

    private TaintOverlayTextures() {}

    public static Identifier get(Model<?> model) {
        return CACHE.computeIfAbsent(model, TaintOverlayTextures::create);
    }

    private static Identifier create(Model<?> model) {
        ModelUvLayout layout = ModelUvLayout.of(model);
        NativeImage image = new NativeImage(layout.width(), layout.height(), true);
        PATTERN.paint(image, layout, model.getClass().getName().hashCode() * 31L + layout.width() * 7L + layout.height());
        Identifier id = TCIds.rl("dynamic/taint_overlay/" + nextId++);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
        return id;
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(TCIds.rl("taint_overlays"), (ResourceManagerReloadListener) manager -> clear());
    }

    private static void clear() {
        for (Identifier id : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(id);
        }
        CACHE.clear();
    }
}
