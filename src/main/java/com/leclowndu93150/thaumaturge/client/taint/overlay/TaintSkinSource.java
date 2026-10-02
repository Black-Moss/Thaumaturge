package com.leclowndu93150.thaumaturge.client.taint.overlay;

import net.minecraft.client.model.Model;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public interface TaintSkinSource {
    @Nullable
    TaintSkin resolve(Model<?> model, Identifier baseTexture, TaintSkinResources resources);
}
