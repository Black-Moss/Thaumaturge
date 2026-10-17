package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.mixin.client.renderer.item.ItemStackRenderStateAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public final class TintedGuiItems {
    public static final int NO_TINT = 0xFFFFFFFF;
    private static final int UNTINTED_INDEX = -1;

    private TintedGuiItems() {}

    public static void item(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int tint) {
        ItemTintGraphics tinted = (ItemTintGraphics) graphics;
        tinted.thaumaturge$setItemTint(tint);
        graphics.item(stack, x, y);
        tinted.thaumaturge$setItemTint(NO_TINT);
    }

    public static void apply(ItemStackRenderState state, int tint) {
        ItemStackRenderStateAccessor access = (ItemStackRenderStateAccessor) state;
        ItemStackRenderState.LayerRenderState[] layers = access.thaumaturge$layers();
        for (int i = 0; i < access.thaumaturge$activeLayerCount(); i++) {
            tintLayer(layers[i], tint);
        }
        state.appendModelIdentityElement(new TintIdentity(tint));
    }

    private static void tintLayer(ItemStackRenderState.LayerRenderState layer, int tint) {
        List<BakedQuad> quads = layer.prepareQuadList();
        IntList tints = layer.tintLayers();
        int highest = UNTINTED_INDEX;
        for (BakedQuad quad : quads) {
            highest = Math.max(highest, quad.materialInfo().tintIndex());
        }
        for (int i = 0; i < tints.size(); i++) {
            tints.set(i, ARGB.multiply(tints.getInt(i), tint));
        }
        while (tints.size() <= highest) {
            tints.add(tint);
        }
        int plainSlot = tints.size();
        boolean plainUsed = false;
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            BakedQuad.MaterialInfo info = quad.materialInfo();
            if (info.tintIndex() == UNTINTED_INDEX) {
                BakedQuad.MaterialInfo tinted = new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), plainSlot, info.shade(), info.lightEmission());
                quads.set(i, new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                        quad.direction(), tinted));
                plainUsed = true;
            }
        }
        if (plainUsed) {
            tints.add(tint);
        }
    }

    private record TintIdentity(int tint) {
    }
}
