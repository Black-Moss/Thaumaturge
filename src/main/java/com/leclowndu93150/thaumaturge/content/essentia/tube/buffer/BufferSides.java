package com.leclowndu93150.thaumaturge.content.essentia.tube.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Direction;

public final class BufferSides {
    private static final int SIDE_COUNT = Direction.values().length;

    private final BufferSideConfig[] configs = new BufferSideConfig[SIDE_COUNT];

    public BufferSides() {
        Arrays.fill(configs, BufferSideConfig.DEFAULT);
    }

    public BufferSideConfig config(Direction side) {
        return configs[side.ordinal()];
    }

    public boolean isOpen(Direction side) {
        return config(side).open();
    }

    public ChokeLevel choke(Direction side) {
        return config(side).choke();
    }

    public void setOpen(Direction side, boolean open) {
        configs[side.ordinal()] = config(side).withOpen(open);
    }

    public void cycleChoke(Direction side) {
        configs[side.ordinal()] = config(side).withChoke(choke(side).next());
    }

    public boolean[] openFlags() {
        boolean[] flags = new boolean[SIDE_COUNT];
        for (int i = 0; i < SIDE_COUNT; i++) {
            flags[i] = configs[i].open();
        }
        return flags;
    }

    public List<Integer> chokeOrdinals() {
        List<Integer> ordinals = new ArrayList<>(SIDE_COUNT);
        for (BufferSideConfig config : configs) {
            ordinals.add(config.choke().ordinal());
        }
        return ordinals;
    }

    public List<Boolean> openList() {
        List<Boolean> flags = new ArrayList<>(SIDE_COUNT);
        for (BufferSideConfig config : configs) {
            flags.add(config.open());
        }
        return flags;
    }

    public void restore(List<Integer> chokeOrdinals, List<Boolean> openFlags) {
        for (int i = 0; i < SIDE_COUNT; i++) {
            ChokeLevel choke = i < chokeOrdinals.size() ? ChokeLevel.fromOrdinal(chokeOrdinals.get(i)) : ChokeLevel.NORMAL;
            boolean open = i >= openFlags.size() || openFlags.get(i);
            configs[i] = new BufferSideConfig(choke, open);
        }
    }
}
