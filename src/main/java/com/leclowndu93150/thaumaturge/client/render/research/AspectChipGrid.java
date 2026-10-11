package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

final class AspectChipGrid {
    private static final int PITCH = 20;
    private static final int CHIP_SIZE = 16;
    private static final int HALF = 2;

    private final List<Chip> chips;

    AspectChipGrid(AspectList aspects, int perRow, int x, int y) {
        this.chips = place(aspects.sortedByTag(), Math.max(1, perRow), x, y);
    }

    private static List<Chip> place(List<AspectInstance> sorted, int perRow, int x, int y) {
        int count = sorted.size();
        int rows = (count + perRow - 1) / perRow;
        int top = y - (rows - 1) * PITCH / HALF;
        int lastRowCount = count - (rows - 1) * perRow;
        int lastRowShift = (perRow - lastRowCount) * PITCH / HALF;
        List<Chip> placed = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int row = index / perRow;
            int column = index % perRow;
            int shift = row == rows - 1 ? lastRowShift : 0;
            placed.add(new Chip(sorted.get(index), x + shift + column * PITCH, top + row * PITCH));
        }
        return List.copyOf(placed);
    }

    void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Chip chip : chips) {
            AspectTagRenderer.render(graphics, minecraft.font, chip.x(), chip.y(), chip.entry().aspect(), chip.entry().amount());
        }
    }

    @Nullable
    List<Component> popup(double mouseX, double mouseY) {
        for (Chip chip : chips) {
            if (chip.contains(mouseX, mouseY)) {
                return List.of(AspectComponents.name(chip.entry().aspect()), AspectComponents.description(chip.entry().aspect()));
            }
        }
        return null;
    }

    private record Chip(AspectInstance entry, int x, int y) {
        boolean contains(double pointX, double pointY) {
            return pointX >= x && pointX < x + CHIP_SIZE && pointY >= y && pointY < y + CHIP_SIZE;
        }
    }
}
