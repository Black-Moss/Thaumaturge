package com.leclowndu93150.thaumaturge.client.screen.research.detail;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget.ItemHit;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen.AspectHit;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class HitRecorder {
    private final List<ItemHit> itemHits = new ArrayList<>();
    private final List<AspectHit> aspectHits = new ArrayList<>();

    public void clear() {
        itemHits.clear();
        aspectHits.clear();
    }

    public void recordItem(ItemStack stack, int x, int y) {
        itemHits.add(new ItemHit(stack, x, y));
    }

    public void recordAspect(AspectInstance aspect, int x, int y) {
        aspectHits.add(new AspectHit(aspect, x, y));
    }

    public @Nullable ItemHit topItemAt(double pointX, double pointY) {
        for (ItemHit hit : itemHits.reversed()) {
            if (hit.contains(pointX, pointY)) {
                return hit;
            }
        }
        return null;
    }

    public @Nullable AspectHit topKnownAspectAt(double pointX, double pointY) {
        for (AspectHit hit : aspectHits.reversed()) {
            if (hit.contains(pointX, pointY) && AspectKnowledgeAccess.isKnown(hit.aspect().aspect())) {
                return hit;
            }
        }
        return null;
    }
}
