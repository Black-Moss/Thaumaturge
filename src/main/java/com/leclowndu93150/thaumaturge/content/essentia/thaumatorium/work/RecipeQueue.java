package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class RecipeQueue {
    public static final int BASE_CAPACITY = 1;

    private static final int CAPACITY_PER_BRAIN_BOX = 2;

    private final List<Identifier> ids = new ArrayList<>();
    private int capacity = BASE_CAPACITY;

    public static int capacityFor(int brainBoxes) {
        return BASE_CAPACITY + CAPACITY_PER_BRAIN_BOX * brainBoxes;
    }

    public List<Identifier> ids() {
        return ids;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return ids.isEmpty();
    }

    public boolean isFull() {
        return ids.size() >= capacity;
    }

    public boolean remove(Identifier id) {
        return ids.remove(id);
    }

    public void add(Identifier id) {
        ids.add(id);
    }

    public boolean resize(int newCapacity) {
        capacity = newCapacity;
        boolean trimmed = false;
        while (ids.size() > capacity) {
            ids.remove(ids.size() - 1);
            trimmed = true;
        }
        return trimmed;
    }

    public void restore(int storedCapacity, List<Identifier> storedIds) {
        capacity = storedCapacity;
        ids.clear();
        ids.addAll(storedIds);
    }
}
