package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealFilter;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPanel;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuSealBase extends AbstractTTMenu {
    public static final int BUTTON_BLACKLIST_ON = 20;
    public static final int BUTTON_BLACKLIST_OFF = 21;
    public static final int BUTTON_LOCK = 25;
    public static final int BUTTON_UNLOCK = 26;
    public static final int BUTTON_REDSTONE_ON = 27;
    public static final int BUTTON_REDSTONE_OFF = 28;
    public static final int BUTTON_TOGGLE_ON_BASE = 30;
    public static final int BUTTON_TOGGLE_OFF_BASE = 60;
    public static final int BUTTON_PRIORITY_DOWN = 80;
    public static final int BUTTON_PRIORITY_UP = 81;
    public static final int BUTTON_COLOR_DOWN = 82;
    public static final int BUTTON_COLOR_UP = 83;
    public static final int BUTTON_AREA_BASE = 90;
    public static final int MIDDLE_X = 88;
    public static final int MIDDLE_Y = 72;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 150;
    public static final int HOTBAR_Y = 208;

    private static final int GHOST_COLUMNS = 3;
    private static final int GHOST_PITCH = 24;
    private static final int GHOST_CENTER_BASE = 16;
    private static final int GHOST_CENTER_STEP = 12;
    private static final int GHOST_CENTER_SHIFT = 8;
    private static final int PRIORITY_LIMIT = 5;
    private static final int COLOR_MAX = 16;
    private static final int AREA_MIN = 1;
    private static final int AREA_MAX = 8;
    private static final int AREA_BUTTON_COUNT = 6;
    private static final int[] AREA_AXIS_ORDER = {1, 0, 2};
    private static final int LIMIT_STEP = 1;
    private static final int LIMIT_STEP_SHIFT = 10;

    private final Player player;
    private final @Nullable ISealEntity initial;
    private final List<SealPanel> panels;
    private final DataSlot prioritySlot = DataSlot.standalone();
    private final DataSlot areaXSlot = DataSlot.standalone();
    private final DataSlot areaYSlot = DataSlot.standalone();
    private final DataSlot areaZSlot = DataSlot.standalone();
    private final DataSlot colorSlot = DataSlot.standalone();
    private final int filterSlots;
    private int panelIndex;

    public MenuSealBase(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, SealHandler.lookup(playerInventory.player.level(), new SealPos(buf.readBlockPos(), buf.readEnum(Direction.class))));
    }

    public MenuSealBase(int containerId, Inventory playerInventory, @Nullable ISealEntity seal) {
        super(TTMenus.SEAL.get(), containerId);
        this.initial = seal;
        this.panels = seal == null ? List.of(SealPanel.PRIORITY) : seal.type().panels();
        this.player = playerInventory.player;
        Optional<ISealFilter> filter = seal == null ? Optional.empty() : seal.filter();
        this.filterSlots = filter.map(found -> found.spec().slots()).orElse(0);
        if (filter.isPresent()) {
            addGhostSlots();
        }
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
        addDataSlot(prioritySlot);
        addDataSlot(areaXSlot);
        addDataSlot(areaYSlot);
        addDataSlot(areaZSlot);
        addDataSlot(colorSlot);
        refreshData();
    }

    private void addGhostSlots() {
        FilterContainer container = new FilterContainer();
        int lastColumn = (filterSlots - 1) % GHOST_COLUMNS;
        int lastRow = (filterSlots - 1) / GHOST_COLUMNS;
        int originX = MIDDLE_X - (GHOST_CENTER_BASE + lastColumn * GHOST_CENTER_STEP - GHOST_CENTER_SHIFT);
        int originY = MIDDLE_Y - (GHOST_CENTER_BASE + lastRow * GHOST_CENTER_STEP - GHOST_CENTER_SHIFT);
        for (int slot = 0; slot < filterSlots; slot++) {
            addSlot(new GhostSlot(container, slot, originX + slot % GHOST_COLUMNS * GHOST_PITCH, originY + slot / GHOST_COLUMNS * GHOST_PITCH));
        }
    }

    private void refreshData() {
        ISealEntity target = seal();
        if (target == null) {
            return;
        }
        prioritySlot.set(target.priority());
        areaXSlot.set(target.area().getX());
        areaYSlot.set(target.area().getY());
        areaZSlot.set(target.area().getZ());
        colorSlot.set(target.color());
    }

    public @Nullable ISealEntity seal() {
        if (initial == null || !player.level().isClientSide()) {
            return initial;
        }
        ISealEntity live = SealHandler.lookup(player.level(), initial.pos());
        return live != null ? live : initial;
    }

    private @Nullable ISealFilter liveFilter() {
        ISealEntity target = seal();
        return target == null ? null : target.filter().orElse(null);
    }

    public List<SealPanel> panels() {
        return panels;
    }

    public SealPanel panel() {
        return panels.get(Mth.clamp(panelIndex, 0, panels.size() - 1));
    }

    public int priority() {
        return prioritySlot.get();
    }

    public BlockPos area() {
        return new BlockPos(areaXSlot.get(), areaYSlot.get(), areaZSlot.get());
    }

    public int color() {
        return colorSlot.get();
    }

    public int filterSlotCount() {
        return filterSlots;
    }

    @Override
    public void broadcastChanges() {
        refreshData();
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ISealEntity target = seal();
        if (target == null) {
            return false;
        }
        if (!applyButton(target, player, id)) {
            return false;
        }
        target.markChanged(player.level());
        return true;
    }

    private boolean applyButton(ISealEntity target, Player player, int id) {
        if (id >= 0 && id < panels.size()) {
            panelIndex = id;
            return true;
        }
        if (id == BUTTON_REDSTONE_ON || id == BUTTON_REDSTONE_OFF) {
            target.setRedstoneControlled(id == BUTTON_REDSTONE_ON);
            return true;
        }
        if (!SealAccess.mayEdit(player, target)) {
            return false;
        }
        return applySetting(target, id) || applyOwnership(player, target, id) || applyFilter(target, id) || applyRanges(target, id) || applyArea(target, id);
    }

    private static boolean applySetting(ISealEntity target, int id) {
        SealType type = target.type();
        List<SealSetting> settings = type.showsSettings() ? type.settings() : List.of();
        int enable = id - BUTTON_TOGGLE_ON_BASE;
        int disable = id - BUTTON_TOGGLE_OFF_BASE;
        if (enable >= 0 && enable < settings.size()) {
            target.setSetting(settings.get(enable), true);
            return true;
        }
        if (disable >= 0 && disable < settings.size()) {
            target.setSetting(settings.get(disable), false);
            return true;
        }
        return false;
    }

    private static boolean applyOwnership(Player player, ISealEntity target, int id) {
        if (id != BUTTON_LOCK && id != BUTTON_UNLOCK) {
            return false;
        }
        if (player.getUUID().equals(target.owner())) {
            target.setLocked(id == BUTTON_LOCK);
            return true;
        }
        return false;
    }

    private static boolean applyFilter(ISealEntity target, int id) {
        if (id != BUTTON_BLACKLIST_ON && id != BUTTON_BLACKLIST_OFF) {
            return false;
        }
        ISealFilter filter = target.filter().orElse(null);
        if (filter == null) {
            return false;
        }
        filter.setBlacklist(id == BUTTON_BLACKLIST_ON);
        return true;
    }

    private static boolean applyRanges(ISealEntity target, int id) {
        if (id == BUTTON_PRIORITY_DOWN || id == BUTTON_PRIORITY_UP) {
            int next = target.priority() + (id == BUTTON_PRIORITY_UP ? 1 : -1);
            if (next >= -PRIORITY_LIMIT && next <= PRIORITY_LIMIT) {
                target.setPriority((byte) next);
            }
            return true;
        }
        if (id == BUTTON_COLOR_DOWN || id == BUTTON_COLOR_UP) {
            int next = target.color() + (id == BUTTON_COLOR_UP ? 1 : -1);
            if (next >= 0 && next <= COLOR_MAX) {
                target.setColor((byte) next);
            }
            return true;
        }
        return false;
    }

    private static boolean applyArea(ISealEntity target, int id) {
        if (!target.type().hasArea() || id < BUTTON_AREA_BASE || id >= BUTTON_AREA_BASE + AREA_BUTTON_COUNT) {
            return false;
        }
        int axis = (id - BUTTON_AREA_BASE) / 2;
        int step = (id - BUTTON_AREA_BASE) % 2 == 0 ? -1 : 1;
        BlockPos area = target.area();
        int[] size = {area.getX(), area.getY(), area.getZ()};
        int index = AREA_AXIS_ORDER[axis];
        size[index] = Mth.clamp(size[index] + step, AREA_MIN, AREA_MAX);
        target.setArea(new BlockPos(size[0], size[1], size[2]));
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        ISealEntity target = seal();
        if (target == null || input == ContainerInput.QUICK_CRAFT || slotId < 0 || slotId >= filterSlots) {
            super.clicked(slotId, button, input, player);
            return;
        }
        ISealFilter filter = target.filter().orElse(null);
        if (filter == null || !SealAccess.mayEdit(player, target)) {
            return;
        }
        applyGhostClick(filter, slotId, button == 1, input == ContainerInput.QUICK_MOVE, getCarried());
        target.markChanged(player.level());
    }

    private static void applyGhostClick(ISealFilter filter, int slot, boolean secondary, boolean shift, ItemStack carried) {
        ItemStack shown = filter.stack(slot);
        boolean same = !carried.isEmpty() && ItemStack.isSameItemSameComponents(shown, carried);
        boolean counted = filter.usesLimits();
        if (secondary) {
            if (!counted) {
                clearGhost(filter, slot);
            } else if (carried.isEmpty() && !shown.isEmpty()) {
                lowerLimit(filter, slot, shift ? LIMIT_STEP_SHIFT : LIMIT_STEP);
            } else if (same) {
                lowerLimit(filter, slot, carried.getCount());
            }
            return;
        }
        if (carried.isEmpty()) {
            if (counted && !shown.isEmpty()) {
                filter.setLimit(slot, filter.limit(slot) + (shift ? LIMIT_STEP_SHIFT : LIMIT_STEP));
            }
            return;
        }
        if (counted && same) {
            filter.setLimit(slot, filter.limit(slot) + carried.getCount());
            return;
        }
        filter.setStack(slot, carried.copyWithCount(1));
        filter.setLimit(slot, 0);
    }

    private static void lowerLimit(ISealFilter filter, int slot, int amount) {
        int next = filter.limit(slot) - amount;
        if (next < 0) {
            clearGhost(filter, slot);
        } else {
            filter.setLimit(slot, next);
        }
    }

    private static void clearGhost(ISealFilter filter, int slot) {
        filter.setStack(slot, ItemStack.EMPTY);
        filter.setLimit(slot, 0);
    }

    @Override
    public boolean stillValid(Player player) {
        ISealEntity target = seal();
        return target != null && SealAccess.isLive(player, target);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    final class GhostSlot extends Slot {
        GhostSlot(FilterContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean isActive() {
            return panel() == SealPanel.FILTER;
        }
    }

    final class FilterContainer implements Container {
        @Override
        public int getContainerSize() {
            return filterSlots;
        }

        @Override
        public boolean isEmpty() {
            ISealFilter filter = liveFilter();
            return filter == null || filter.stacks().stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            ISealFilter filter = liveFilter();
            return filter == null ? ItemStack.EMPTY : filter.stack(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            ISealFilter filter = liveFilter();
            if (filter != null) {
                filter.setStack(slot, stack);
            }
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {}
    }
}
