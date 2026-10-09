package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.content.menu.AbstractHeldItemMenu;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuFocusPouch extends AbstractHeldItemMenu {
    private static final int POUCH_COLUMNS = 6;
    private static final int POUCH_ORIGIN_X = 40;
    private static final int POUCH_ORIGIN_Y = 51;
    private static final int POUCH_PITCH = 17;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 151;

    private final SimpleContainer pouchContainer = new SimpleContainer(FocusPouchItem.SIZE);

    public MenuFocusPouch(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public MenuFocusPouch(int containerId, Inventory inventory, InteractionHand hand) {
        super(TTMenus.FOCUS_POUCH.get(), containerId, inventory, hand);
        NonNullList<ItemStack> contents = FocusPouchItem.getInventory(held());
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            pouchContainer.setItem(slot, contents.get(slot));
            addSlot(new FocusSlot(pouchContainer, slot, POUCH_ORIGIN_X + slot % POUCH_COLUMNS * POUCH_PITCH, POUCH_ORIGIN_Y + slot / POUCH_COLUMNS * POUCH_PITCH));
        }
        addStandardInventorySlots(inventory, PLAYER_INV_X, PLAYER_INV_Y);
    }

    @Override
    protected void writeBack(ItemStack pouch) {
        if (!pouch.is(TTItems.FOCUS_POUCH.get())) {
            return;
        }
        NonNullList<ItemStack> contents = NonNullList.withSize(FocusPouchItem.SIZE, ItemStack.EMPTY);
        for (int slot = 0; slot < FocusPouchItem.SIZE; slot++) {
            contents.set(slot, pouchContainer.getItem(slot));
        }
        FocusPouchItem.setInventory(pouch, contents);
    }

    @Override
    protected ItemStack quickMove(Player player, int index) {
        return quickMoveBetween(index, FocusPouchItem.SIZE, FocusItems::isFocus);
    }

    private static final class FocusSlot extends Slot {
        FocusSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return FocusItems.isFocus(stack);
        }
    }
}
