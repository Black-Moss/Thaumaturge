package com.leclowndu93150.thaumcraft.content.research.table;

import com.leclowndu93150.thaumcraft.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityResearchTable extends BlockEntity implements MenuProvider {
    public static final int SLOT_SCRIBE_TOOLS = 0;
    public static final int SLOT_PAPER = 1;
    public static final int SLOT_COUNT = 2;

    private static final Component TITLE = Component.translatable("gui.thaumcraft.research_table.title");

    private final TableInventory inventory = new TableInventory();

    public BlockEntityResearchTable(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStacksResourceHandler items() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuResearchTable(containerId, playerInventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Container toContainer() {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemResource resource = inventory.getResource(i);
            int amount = inventory.getAmountAsInt(i);
            if (!resource.isEmpty() && amount > 0) {
                container.setItem(i, resource.toStack(amount));
            }
        }
        return container;
    }

    public void dropContents(Level level, BlockPos pos) {
        net.minecraft.world.Containers.dropContents(level, pos, toContainer());
    }

    public boolean consumeInk() {
        ItemStack tools = inventory.getResource(SLOT_SCRIBE_TOOLS).toStack(inventory.getAmountAsInt(SLOT_SCRIBE_TOOLS));
        if (tools.isEmpty()) return false;
        int damage = tools.getDamageValue();
        int max = tools.getMaxDamage();
        if (max <= 0 || damage >= max) return false;
        tools.setDamageValue(damage + 1);
        inventory.set(SLOT_SCRIBE_TOOLS, ItemResource.of(tools), tools.getCount());
        setChanged();
        return true;
    }

    public boolean consumePaper() {
        ItemResource resource = inventory.getResource(SLOT_PAPER);
        int amount = inventory.getAmountAsInt(SLOT_PAPER);
        if (resource.isEmpty() || amount <= 0) return false;
        if (resource.getItem() != Items.PAPER) return false;
        inventory.set(SLOT_PAPER, resource, amount - 1);
        setChanged();
        return true;
    }

    public boolean hasInkReady() {
        ItemStack tools = inventory.getResource(SLOT_SCRIBE_TOOLS).toStack(inventory.getAmountAsInt(SLOT_SCRIBE_TOOLS));
        return !tools.isEmpty() && tools.isDamageableItem() && tools.getDamageValue() < tools.getMaxDamage();
    }

    public boolean hasPaperReady() {
        ItemResource resource = inventory.getResource(SLOT_PAPER);
        return !resource.isEmpty() && resource.getItem() == Items.PAPER && inventory.getAmountAsInt(SLOT_PAPER) > 0;
    }

    private final class TableInventory extends ItemStacksResourceHandler {
        TableInventory() {
            super(NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return switch (index) {
                case SLOT_SCRIBE_TOOLS -> resource.toStack(1).isDamageableItem();
                case SLOT_PAPER -> resource.getItem() == Items.PAPER;
                default -> false;
            };
        }
    }
}
