package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public final class MenuSmelter extends AbstractTTMenu {
    public static final int ITEM_X = 80;
    public static final int ITEM_Y = 7;
    public static final int FUEL_X = 80;
    public static final int FUEL_Y = 47;

    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;

    public static final int SLOT_COUNT = 2;

    private final ItemStacksResourceHandler items;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public MenuSmelter(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        this(containerId, playerInventory, new ItemStacksResourceHandler(SLOT_COUNT), ContainerLevelAccess.create(playerInventory.player.level(), pos), pos);
    }

    public MenuSmelter(int containerId, Inventory playerInventory, BlockEntitySmelter blockEntity) {
        this(containerId, playerInventory, blockEntity.getInventory(), ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), blockEntity.getBlockPos());
    }

    private MenuSmelter(int containerId, Inventory playerInventory, ItemStacksResourceHandler items, ContainerLevelAccess access, BlockPos pos) {
        super(TTMenus.SMELTER.get(), containerId);
        this.items = items;
        this.access = access;
        this.pos = pos;

        addSlot(new ResourceHandlerSlot(items, items::set, 0, ITEM_X, ITEM_Y));
        addSlot(new ResourceHandlerSlot(items, items::set, 1, FUEL_X, FUEL_Y));

        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y + 3 * 18 + 4);
    }

    public BlockPos pos() {
        return pos;
    }

    public ItemStacksResourceHandler items() {
        return items;
    }

    public BlockEntitySmelter blockEntity() {
        return (BlockEntitySmelter) access.evaluate(Level::getBlockEntity).filter(be -> be instanceof BlockEntitySmelter).orElse(null);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.SMELTER_BASIC.get()) || AbstractContainerMenu.stillValid(access, player, TTBlocks.SMELTER_THAUMIUM.get())
                || AbstractContainerMenu.stillValid(access, player, TTBlocks.SMELTER_VOID.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(slotIndex, SLOT_COUNT, stack -> true);
    }
}
