package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuPotionSprayer extends AbstractTTMenu {
    public static final int POTION_SLOT_X = 56;
    public static final int POTION_SLOT_Y = 64;
    public static final int INVENTORY_X = 16;
    public static final int INVENTORY_Y = 151;
    public static final int HOTBAR_Y = 209;

    private static final int POTION_SLOT = 0;
    private static final int POTION_SLOTS = 1;
    private static final int POTION_STACK_LIMIT = 1;

    private final BlockPos pos;
    private final ContainerLevelAccess access;

    public MenuPotionSprayer(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos(), new SimpleContainer(POTION_SLOTS), ContainerLevelAccess.NULL);
    }

    public MenuPotionSprayer(int containerId, Inventory inventory, BlockEntityPotionSprayer sprayer) {
        this(containerId, inventory, sprayer.getBlockPos(), new PotionSprayerContainer(sprayer), ContainerLevelAccess.create(sprayer.getLevel(), sprayer.getBlockPos()));
    }

    private MenuPotionSprayer(int containerId, Inventory inventory, BlockPos pos, Container potionContainer, ContainerLevelAccess access) {
        super(TTMenus.POTION_SPRAYER.get(), containerId);
        this.pos = pos;
        this.access = access;
        addSlot(new PotionSlot(potionContainer, POTION_SLOT, POTION_SLOT_X, POTION_SLOT_Y));
        addInventoryExtendedSlots(inventory, INVENTORY_X, INVENTORY_Y);
        addInventoryHotbarSlots(inventory, INVENTORY_X, HOTBAR_Y);
    }

    public BlockPos sprayerPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.POTION_SPRAYER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, POTION_SLOTS, BlockEntityPotionSprayer::holdsPotionItem);
    }

    private static final class PotionSlot extends Slot {
        PotionSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return BlockEntityPotionSprayer.holdsPotionItem(stack);
        }

        @Override
        public int getMaxStackSize() {
            return POTION_STACK_LIMIT;
        }
    }
}
