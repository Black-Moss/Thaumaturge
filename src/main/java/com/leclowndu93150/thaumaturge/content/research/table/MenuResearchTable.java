package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuResearchTable extends AbstractTTMenu implements BlockMenu<BlockEntityResearchTable> {
    public static final int SCRIBE_TOOLS_X = 14;
    public static final int SCRIBE_TOOLS_Y = 10;
    public static final int NOTE_X = 70;
    public static final int NOTE_Y = 10;
    public static final int PLAYER_GRID_X = 48;
    public static final int PLAYER_GRID_Y = 175;
    public static final int HOTBAR_Y = 233;
    public static final int TABLE_SLOT_COUNT = BlockEntityResearchTable.SLOT_COUNT;

    private static final List<TableSlotDescriptor> TABLE_SLOTS = List.of(new TableSlotDescriptor(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, SCRIBE_TOOLS_X, SCRIBE_TOOLS_Y),
            new TableSlotDescriptor(BlockEntityResearchTable.SLOT_NOTE, NOTE_X, NOTE_Y));

    private final TableBinding binding;
    private final TableChangeWatcher watcher;

    public MenuResearchTable(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, TableBinding.at(playerInventory.player.level(), buf.readBlockPos()));
    }

    public MenuResearchTable(int containerId, Inventory playerInventory, @Nullable BlockEntityResearchTable blockEntity) {
        this(containerId, playerInventory, TableBinding.of(blockEntity));
    }

    private MenuResearchTable(int containerId, Inventory playerInventory, TableBinding binding) {
        super(TTMenus.RESEARCH_TABLE.get(), containerId);
        this.binding = binding;
        this.watcher = new TableChangeWatcher(binding, BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, BlockEntityResearchTable.SLOT_NOTE);
        ItemStacksResourceHandler handler = binding.handler();
        for (TableSlotDescriptor descriptor : TABLE_SLOTS) {
            addSlot(new ResourceHandlerSlot(handler, handler::set, descriptor.index(), descriptor.x(), descriptor.y()));
        }
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        BlockEntityResearchTable table = binding.table();
        if (table == null) {
            return;
        }
        Level level = table.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (watcher.absorbDifferences()) {
            publishTableChange(table, level);
        }
    }

    private void publishTableChange(BlockEntityResearchTable table, Level level) {
        table.ensureNotePuzzle();
        table.setChanged();
        BlockState state = table.getBlockState();
        level.sendBlockUpdated(binding.origin(), state, state, Block.UPDATE_ALL);
    }

    public BlockPos pos() {
        return binding.origin();
    }

    @Override
    public @Nullable BlockEntityResearchTable blockEntity() {
        return binding.table();
    }

    public ItemStacksResourceHandler tableItems() {
        return binding.handler();
    }

    public BlockPos tablePos() {
        return binding.origin();
    }

    public boolean hasUsableScribeTools() {
        ItemStack tools = binding.stackAt(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS);
        return !tools.isEmpty() && tools.isDamageableItem() && tools.getDamageValue() < tools.getMaxDamage();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(binding.access(), player, TTBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, TABLE_SLOT_COUNT, stack -> true);
    }
}
