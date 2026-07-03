package com.leclowndu93150.thaumcraft.content.workbench;

import com.leclowndu93150.thaumcraft.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingInput;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.ArrayList;
import java.util.List;

public class InventoryArcaneWorkbench extends SimpleContainer implements IArcaneWorkbench, CraftingContainer {
    private final List<Runnable> changeListeners = new ArrayList<>();

    public InventoryArcaneWorkbench() {
        super(15);
    }

    public void addChangedListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangedListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public CraftingInput asCraftInput() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(getItem(i));
        }
        return CraftingInput.of(3, 3, grid);
    }

    public ArcaneCraftingInput asArcaneCraftInput() {
        return ArcaneCraftingInput.of(3, 3, getItems());
    }
}