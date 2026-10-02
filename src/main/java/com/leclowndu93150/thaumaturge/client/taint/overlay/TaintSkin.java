package com.leclowndu93150.thaumaturge.client.taint.overlay;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record TaintSkin(Identifier texture, boolean replacesBase, @Nullable Identifier glow) {
}
