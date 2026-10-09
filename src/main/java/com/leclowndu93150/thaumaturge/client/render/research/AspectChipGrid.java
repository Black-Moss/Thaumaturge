package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

final class AspectChipGrid {
    private static final int CELL = 20;
    private static final int HALF_CELL = 10;
    private static final int CHIP_SIZE = 16;
    private static final int DEFAULT_ROW_LIMIT = 1;
    private static final int NO_BONUS = 0;
    private static final float FULL_ALPHA = 1.0F;

    private record Chip(Holder<IAspect> aspect, int amount, int x, int y) {
    }

    private final List<Chip> chips = new ArrayList<>();

    AspectChipGrid(AspectList aspects, int perRow, int anchorX, int anchorY) {
        List<AspectInstance> sorted = new ArrayList<>(aspects.entries());
        sorted.sort(Comparator.comparing(AspectChipGrid::sortKey));
        int count = sorted.size();
        int lastRow = count == 0 ? 0 : (count - 1) / perRow;
        int top = anchorY - HALF_CELL * lastRow;
        for (int i = 0; i < count; i++) {
            int column = i % perRow;
            int row = i / perRow;
            int shift = row == lastRow && (lastRow > DEFAULT_ROW_LIMIT || count < perRow) ? (perRow - count % perRow) * HALF_CELL : 0;
            chips.add(new Chip(sorted.get(i).aspect(), sorted.get(i).amount(), anchorX + CELL * column + shift, top + CELL * row));
        }
    }

    void render(GuiGraphicsExtractor graphics) {
        for (Chip chip : chips) {
            AspectTagRenderer.render(graphics, chip.x, chip.y, chip.aspect, chip.amount, NO_BONUS, FULL_ALPHA, false);
        }
    }

    @Nullable
    List<Component> popup(double mouseX, double mouseY) {
        for (Chip chip : chips) {
            if (PanelContext.inside(mouseX, mouseY, chip.x, chip.y, CHIP_SIZE)) {
                return List.of(AspectComponents.name(chip.aspect), AspectComponents.description(chip.aspect));
            }
        }
        return null;
    }

    private static String sortKey(AspectInstance entry) {
        return entry.aspect().unwrapKey().map(ResourceKey::identifier).map(Object::toString).orElse("");
    }
}
