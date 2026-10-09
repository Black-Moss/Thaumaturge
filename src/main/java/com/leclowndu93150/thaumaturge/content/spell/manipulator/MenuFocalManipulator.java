package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuFocalManipulator extends AbstractContainerMenu {
    public static final int BUTTON_INSCRIBE = 0;
    public static final int FOCUS_SLOT_X = 91;
    public static final int FOCUS_SLOT_Y = 19;
    public static final int CABINET_X = 13;
    public static final int HOTBAR_Y = 12;
    public static final int MAIN_Y = 69;
    public static final int SLOT_PITCH = 18;

    private static final int FOCUS_SLOT = 0;
    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_MAIN_START = 9;
    private static final int MAIN_SIZE = 27;
    private static final int MAIN_FIRST = 1;
    private static final int MAIN_END = MAIN_FIRST + MAIN_SIZE;
    private static final int HOTBAR_FIRST = MAIN_END;
    private static final int HOTBAR_END = HOTBAR_FIRST + HOTBAR_SIZE;
    private static final int HOTBAR_COLUMNS = 3;
    private static final int MAIN_ROWS = 9;
    private static final float REFUSED_VOLUME = 0.33F;
    private static final float REFUSED_PITCH = 1.0F;

    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final @Nullable BlockEntityFocalManipulator table;

    public MenuFocalManipulator(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, buffer.readBlockPos(), null, new ItemStacksResourceHandler(1));
    }

    public MenuFocalManipulator(int containerId, Inventory inventory, BlockEntityFocalManipulator table) {
        this(containerId, inventory, table.getBlockPos(), table, table.items());
    }

    private MenuFocalManipulator(int containerId, Inventory inventory, BlockPos pos, @Nullable BlockEntityFocalManipulator table, ItemStacksResourceHandler items) {
        super(TTMenus.FOCAL_MANIPULATOR.get(), containerId);
        this.pos = pos;
        this.table = table;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);
        addSlot(new FocusSlot(items));
        for (int slot = INVENTORY_MAIN_START; slot < INVENTORY_MAIN_START + MAIN_SIZE; slot++) {
            int cell = slot - INVENTORY_MAIN_START;
            addSlot(new Slot(inventory, slot, CABINET_X + SLOT_PITCH * (cell / MAIN_ROWS), MAIN_Y + SLOT_PITCH * (cell % MAIN_ROWS)));
        }
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            addSlot(new Slot(inventory, slot, CABINET_X + SLOT_PITCH * (slot % HOTBAR_COLUMNS), HOTBAR_Y + SLOT_PITCH * (slot / HOTBAR_COLUMNS)));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public Optional<BlockEntityFocalManipulator> table() {
        return Optional.ofNullable(table);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_INSCRIBE) {
            return false;
        }
        if (table != null && !table.startInscribing(player)) {
            player.level().playSound(null, pos, TTSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, REFUSED_VOLUME, REFUSED_PITCH);
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.FOCAL_MANIPULATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        if (!route(moving, index)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private boolean route(ItemStack moving, int index) {
        if (index == FOCUS_SLOT) {
            return moveItemStackTo(moving, MAIN_FIRST, HOTBAR_END, false);
        }
        boolean toFocus = FocusItems.isFocus(moving) && moveItemStackTo(moving, FOCUS_SLOT, MAIN_FIRST, false);
        boolean toRest = !moving.isEmpty() && (index < MAIN_END ? moveItemStackTo(moving, HOTBAR_FIRST, HOTBAR_END, false) : moveItemStackTo(moving, MAIN_FIRST, MAIN_END, false));
        return toFocus || toRest;
    }

    private static final class FocusSlot extends ResourceHandlerSlot {
        FocusSlot(ItemStacksResourceHandler items) {
            super(items, items::set, FOCUS_SLOT, FOCUS_SLOT_X, FOCUS_SLOT_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FocusItems.isFocus(stack);
        }
    }
}
