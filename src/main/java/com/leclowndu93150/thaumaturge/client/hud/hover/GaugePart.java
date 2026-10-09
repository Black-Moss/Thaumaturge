package com.leclowndu93150.thaumaturge.client.hud.hover;

import net.minecraft.resources.Identifier;

public record GaugePart(Identifier texture, SourceRect source, Anchor anchor, int textureWidth, int textureHeight, int tint) {
}
