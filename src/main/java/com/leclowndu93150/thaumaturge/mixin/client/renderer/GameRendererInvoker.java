package com.leclowndu93150.thaumaturge.mixin.client.renderer;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("setPostEffect")
    void thaumaturge$setPostEffect(Identifier id);
}
