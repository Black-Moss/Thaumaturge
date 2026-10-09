package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuVoidSiphon extends AbstractTTMenu {
    public static final int OUTPUT_X = 80;
    public static final int OUTPUT_Y = 32;

    private static final int PLAYER_GRID_X = 8;
    private static final int PLAYER_GRID_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int OUTPUT_SLOTS = 1;
    private static final int OUTPUT_SLOT = 0;
    private static final double INTERACTION_BUFFER = 4.0;

    private final @Nullable BlockEntityVoidSiphon blockEntity;
    private final DataSlot progress = DataSlot.standalone();

    public MenuVoidSiphon(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, null, clientOutput(buf));
    }

    public MenuVoidSiphon(int containerId, Inventory playerInventory, BlockEntityVoidSiphon blockEntity) {
        this(containerId, playerInventory, blockEntity, blockEntity.output());
    }

    private MenuVoidSiphon(int containerId, Inventory playerInventory, @Nullable BlockEntityVoidSiphon blockEntity, ItemStacksResourceHandler output) {
        super(TTMenus.VOID_SIPHON.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(new OutputSlot(output, OUTPUT_X, OUTPUT_Y));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
        addDataSlot(progress);
    }

    private static ItemStacksResourceHandler clientOutput(RegistryFriendlyByteBuf buf) {
        buf.readBlockPos();
        return new ItemStacksResourceHandler(OUTPUT_SLOTS);
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            progress.set(blockEntity.progress());
        }
        super.broadcastChanges();
    }

    public int progress() {
        return progress.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, OUTPUT_SLOTS, stack -> false);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved() && player.isWithinBlockInteractionRange(blockEntity.getBlockPos(), INTERACTION_BUFFER);
    }

    private static final class OutputSlot extends ResourceHandlerSlot {
        OutputSlot(ItemStacksResourceHandler output, int x, int y) {
            super(output, output::set, OUTPUT_SLOT, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
