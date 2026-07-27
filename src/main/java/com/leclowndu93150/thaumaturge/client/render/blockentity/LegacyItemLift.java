package com.leclowndu93150.thaumaturge.client.render.blockentity;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.phys.AABB;

public final class LegacyItemLift {
    public static final float LEGACY_CENTER_Y = 0.35F;

    private LegacyItemLift() {}

    public static float centerLift(ItemStackRenderState state) {
        AABB box = state.getModelBoundingBox();
        return LEGACY_CENTER_Y - (float) ((box.minY + box.maxY) / 2.0);
    }
}
