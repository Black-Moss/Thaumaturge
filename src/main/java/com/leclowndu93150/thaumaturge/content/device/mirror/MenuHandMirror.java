package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuHandMirror extends AbstractTTMenu {
    private static final int INPUT_SLOT = 0;
    private static final int INPUT_SLOTS = 1;
    private static final int INPUT_X = 80;
    private static final int INPUT_Y = 24;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int OFFHAND_SWAP_BUTTON = 40;

    public final int mirrorHotbarSlot;

    private final Player player;
    private final SimpleContainer input = new InputContainer(this);
    private boolean settling;

    public MenuHandMirror(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory);
    }

    public MenuHandMirror(int containerId, Inventory inventory) {
        super(TTMenus.HAND_MIRROR.get(), containerId);
        addSlot(new Slot(input, INPUT_SLOT, INPUT_X, INPUT_Y));
        addPlayerSlots(inventory);
        this.player = inventory.player;
        this.mirrorHotbarSlot = inventory.getSelectedSlot();
    }

    private void addPlayerSlots(Inventory inventory) {
        addInventoryExtendedSlots(inventory, INVENTORY_X, INVENTORY_Y);
        addInventoryHotbarSlots(inventory, INVENTORY_X, HOTBAR_Y);
    }

    private static boolean isMirror(ItemStack stack) {
        return stack.is(TTItems.HAND_MIRROR.get());
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == input && !settling && !input.isEmpty() && player instanceof ServerPlayer serverPlayer) {
            relay(serverPlayer);
        }
        super.slotsChanged(container);
    }

    private void relay(ServerPlayer sender) {
        settling = true;
        try {
            ItemStack inserted = input.removeItemNoUpdate(INPUT_SLOT);
            if (!ItemHandMirror.transport(sender.getMainHandItem(), inserted, sender)) {
                input.setItem(INPUT_SLOT, inserted);
            }
        } finally {
            settling = false;
        }
    }

    private @Nullable Slot slotAt(int index) {
        return index >= 0 && index < slots.size() ? slots.get(index) : null;
    }

    private boolean holdsMirror(int index) {
        Slot slot = slotAt(index);
        return slot != null && isMirror(slot.getItem());
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput containerInput, Player clicker) {
        boolean blocked = holdsMirror(slotId) || containerInput == ContainerInput.SWAP && swapsMirror(button, clicker);
        if (!blocked) {
            super.clicked(slotId, button, containerInput, clicker);
        }
    }

    private boolean swapsMirror(int button, Player clicker) {
        boolean inventoryButton = button == OFFHAND_SWAP_BUTTON || button >= 0 && button < HOTBAR_SLOT_COUNT;
        return button == mirrorHotbarSlot || inventoryButton && isMirror(clicker.getInventory().getItem(button));
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        boolean movable = slotAt(index) != null && !holdsMirror(index);
        return movable ? quickMoveBetween(index, INPUT_SLOTS, stack -> true) : ItemStack.EMPTY;
    }

    @Override
    public void removed(Player closing) {
        super.removed(closing);
        if (!(closing instanceof ServerPlayer)) {
            return;
        }
        clearContainer(closing, input);
    }

    @Override
    public boolean stillValid(Player viewer) {
        return isMirror(viewer.getMainHandItem());
    }

    private static final class InputContainer extends SimpleContainer {
        private final MenuHandMirror menu;

        InputContainer(MenuHandMirror menu) {
            super(INPUT_SLOTS);
            this.menu = menu;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.slotsChanged(this);
        }
    }
}
