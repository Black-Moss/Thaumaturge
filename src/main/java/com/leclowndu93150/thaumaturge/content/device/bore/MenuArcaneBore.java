package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class MenuArcaneBore extends AbstractContainerMenu {
    public static final int PICK_X = 80;
    public static final int PICK_Y = 29;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;

    private static final int TOOL_SLOT = 0;
    private static final int TOOL_SLOTS = 1;

    private final @Nullable ArcaneBoreHost bore;

    public MenuArcaneBore(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, resolveHost(playerInventory.player.level(), buf));
    }

    private MenuArcaneBore(int containerId, Inventory playerInventory, @Nullable ArcaneBoreHost bore) {
        super(TTMenus.ARCANE_BORE.get(), containerId);
        this.bore = bore;
        addSlot(new ToolSlot(new BoreToolContainer(bore), PICK_X, PICK_Y));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    public static void open(Player player, ArcaneBoreHost host) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new BoreMenuProvider(host), host::writeBoreRef);
        }
    }

    public @Nullable ArcaneBoreHost bore() {
        return bore;
    }

    @Override
    public boolean stillValid(Player player) {
        return bore != null ? bore.boreValid() : false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        ItemStack stack = source.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack before = stack.copy();
        boolean leavingTool = index == TOOL_SLOT;
        int rangeStart = leavingTool ? TOOL_SLOTS : TOOL_SLOT;
        int rangeEnd = leavingTool ? slots.size() : TOOL_SLOTS;
        if (!moveItemStackTo(stack, rangeStart, rangeEnd, leavingTool)) {
            return ItemStack.EMPTY;
        }
        refreshSource(source, stack);
        return before;
    }

    private static void refreshSource(Slot source, ItemStack remainder) {
        if (remainder.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
            return;
        }
        source.setChanged();
    }

    private static @Nullable ArcaneBoreHost resolveHost(Level level, RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return level.getBlockEntity(buf.readBlockPos()) instanceof ArcaneBoreHost host ? host : null;
        }
        return level.getEntity(buf.readVarInt()) instanceof ArcaneBoreHost host ? host : null;
    }

    private static final class ToolSlot extends Slot {
        ToolSlot(BoreToolContainer container, int x, int y) {
            super(container, TOOL_SLOT, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ArcaneBoreTool.isPickaxe(stack);
        }
    }

    private static final class BoreMenuProvider implements MenuProvider {
        private final ArcaneBoreHost host;

        BoreMenuProvider(ArcaneBoreHost host) {
            this.host = host;
        }

        @Override
        public Component getDisplayName() {
            return host.boreDisplayName();
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new MenuArcaneBore(containerId, playerInventory, host);
        }
    }
}
