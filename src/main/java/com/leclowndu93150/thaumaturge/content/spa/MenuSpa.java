package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuSpa extends AbstractTTMenu {
    public static final int SALTS_X = 65;
    public static final int SALTS_Y = 31;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int MIX_BUTTON_ID = 1;
    public static final int SLOT_COUNT = 1;

    private static final int SALTS_SLOT = 0;

    private final Player player;
    private final BlockPos pos;
    private final ContainerLevelAccess access;

    public MenuSpa(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos(), new ItemStacksResourceHandler(SLOT_COUNT));
    }

    public MenuSpa(int containerId, Inventory playerInventory, BlockEntitySpa blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), blockEntity.getItems());
    }

    private MenuSpa(int containerId, Inventory playerInventory, BlockPos pos, ItemStacksResourceHandler items) {
        super(TTMenus.SPA.get(), containerId);
        this.player = playerInventory.player;
        this.pos = pos;
        this.access = ContainerLevelAccess.create(player.level(), pos);
        addSlot(new SaltsSlot(items, SALTS_SLOT, SALTS_X, SALTS_Y));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    public @Nullable BlockEntitySpa blockEntity() {
        return player.level().getBlockEntity(pos) instanceof BlockEntitySpa spa ? spa : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == MIX_BUTTON_ID && !player.level().isClientSide()) {
            BlockEntitySpa spa = blockEntity();
            if (spa != null) {
                spa.toggleMix();
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.SPA.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, SLOT_COUNT, stack -> stack.is(TTItems.BATH_SALTS));
    }

    private static final class SaltsSlot extends ResourceHandlerSlot {
        SaltsSlot(ItemStacksResourceHandler items, int index, int x, int y) {
            super(items, items::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(TTItems.BATH_SALTS);
        }
    }
}
