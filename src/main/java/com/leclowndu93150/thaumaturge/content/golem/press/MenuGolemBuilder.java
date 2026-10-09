package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuGolemBuilder extends AbstractTTMenu implements BlockMenu<BlockEntityGolemBuilder> {
    public static final int OUTPUT_X = 160;
    public static final int OUTPUT_Y = 104;
    public static final int PLAYER_GRID_X = 24;
    public static final int PLAYER_GRID_Y = 142;
    public static final int HOTBAR_Y = 200;
    public static final int SLOT_COUNT = 1;

    private final Player player;
    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final DataSlot costSlot = DataSlot.standalone();
    private final DataSlot maxCostSlot = DataSlot.standalone();

    public MenuGolemBuilder(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos(), new ItemStacksResourceHandler(SLOT_COUNT));
    }

    public MenuGolemBuilder(int containerId, Inventory playerInventory, BlockEntityGolemBuilder blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), blockEntity.outputHandler());
    }

    private MenuGolemBuilder(int containerId, Inventory playerInventory, BlockPos pos, ItemStacksResourceHandler output) {
        super(TTMenus.GOLEM_BUILDER.get(), containerId);
        this.player = playerInventory.player;
        this.pos = pos;
        this.access = ContainerLevelAccess.create(player.level(), pos);
        addSlot(new OutputSlot(output));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
        addDataSlot(costSlot);
        addDataSlot(maxCostSlot);
    }

    @Override
    public void broadcastChanges() {
        BlockEntityGolemBuilder builder = blockEntity();
        if (builder != null) {
            costSlot.set(builder.cost());
            maxCostSlot.set(builder.totalCost());
        }
        super.broadcastChanges();
    }

    public int cost() {
        return costSlot.get();
    }

    public int maxCost() {
        return maxCostSlot.get();
    }

    @Override
    public @Nullable BlockEntityGolemBuilder blockEntity() {
        return player.level().getBlockEntity(pos) instanceof BlockEntityGolemBuilder builder ? builder : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, TTBlocks.GOLEM_BUILDER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, SLOT_COUNT, stack -> stack.is(TTItems.GOLEM_PLACER));
    }

    private static final class OutputSlot extends ResourceHandlerSlot {
        OutputSlot(ItemStacksResourceHandler output) {
            super(output, output::set, BlockEntityGolemBuilder.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(TTItems.GOLEM_PLACER);
        }
    }
}
