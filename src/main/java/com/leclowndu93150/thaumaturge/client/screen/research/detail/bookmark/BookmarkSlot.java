package com.leclowndu93150.thaumaturge.client.screen.research.detail.bookmark;

import com.leclowndu93150.thaumaturge.client.screen.research.detail.Rect;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record BookmarkSlot(BookmarkKind kind, @Nullable Identifier recipeId, int ordinal, Rect hoverArea, Rect clickArea) {
}
