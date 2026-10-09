package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterMode;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealFilterSpec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import net.minecraft.world.item.ItemStack;

public final class SealFilterState implements ISealFilter {
    private static final String KEY_STACKS = "filter";
    private static final String KEY_BLACKLIST = "bl";
    private static final String KEY_LIMITS = "sizes";
    private static final int MAX_STORED_COUNT = 1;
    private static final int NO_LIMIT = 0;

    private final SealFilterSpec spec;
    private final Cell[] cells;
    private final List<ItemStack> stackView;
    private final List<Integer> limitView;
    private boolean blacklist;

    public SealFilterState(SealFilterSpec spec) {
        this(spec, List.of(), true, List.of());
    }

    private SealFilterState(SealFilterSpec spec, List<ItemStack> storedStacks, boolean blacklist, List<Integer> storedLimits) {
        this.spec = spec;
        this.blacklist = blacklist;
        this.cells = new Cell[spec.slots()];
        for (int slot = 0; slot < cells.length; slot++) {
            cells[slot] = new Cell(slot < storedStacks.size() ? storedStacks.get(slot) : ItemStack.EMPTY, slot < storedLimits.size() ? storedLimits.get(slot) : NO_LIMIT);
        }
        this.stackView = new StackView(cells);
        this.limitView = new LimitView(cells);
    }

    public static MapCodec<SealFilterState> codec(SealFilterSpec spec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf(KEY_STACKS, List.of()).forGetter(SealFilterState::stacks),
                Codec.BOOL.optionalFieldOf(KEY_BLACKLIST, true).forGetter(state -> state.blacklist), Codec.INT.listOf().optionalFieldOf(KEY_LIMITS, List.of()).forGetter(SealFilterState::limits))
                .apply(instance, (storedStacks, storedBlacklist, storedLimits) -> new SealFilterState(spec, storedStacks, storedBlacklist, storedLimits)));
    }

    @Override
    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public ItemStack stack(int slot) {
        return cells[slot].stack;
    }

    @Override
    public List<ItemStack> stacks() {
        return stackView;
    }

    @Override
    public List<Integer> limits() {
        return limitView;
    }

    @Override
    public int limit(int slot) {
        return cells[slot].limit;
    }

    @Override
    public void setStack(int slot, ItemStack replacement) {
        cells[slot].stack = replacement.copy();
    }

    @Override
    public SealFilterSpec spec() {
        return spec;
    }

    @Override
    public boolean isBlacklist() {
        return blacklist && spec.mode() != SealFilterMode.WHITELIST_WITH_LIMITS;
    }

    @Override
    public void setLimit(int slot, int amount) {
        cells[slot].limit = amount;
    }

    private static final class Cell {
        private ItemStack stack;
        private int limit;

        private Cell(ItemStack stored, int limit) {
            this.stack = stored.isEmpty() ? ItemStack.EMPTY : stored.copyWithCount(Math.min(MAX_STORED_COUNT, stored.getCount()));
            this.limit = limit;
        }
    }

    private static final class StackView extends AbstractList<ItemStack> implements RandomAccess {
        private final Cell[] backing;

        private StackView(Cell[] backing) {
            this.backing = backing;
        }

        @Override
        public ItemStack get(int index) {
            return backing[index].stack;
        }

        @Override
        public int size() {
            return backing.length;
        }
    }

    private static final class LimitView extends AbstractList<Integer> implements RandomAccess {
        private final Cell[] backing;

        private LimitView(Cell[] backing) {
            this.backing = backing;
        }

        @Override
        public Integer get(int index) {
            return backing[index].limit;
        }

        @Override
        public int size() {
            return backing.length;
        }
    }
}
