package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class MenuArcaneWorkbench extends AbstractContainerMenu {
    public static final List<ResourceKey<IAspect>> PRIMAL_ORDER = List.of(TTAspects.AER, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.TERRA, TTAspects.ORDO, TTAspects.PERDITIO);
    public static final int WAND_X = 160;
    public static final int WAND_Y = 100;
    public static final int[] CRYSTAL_Y = {13, 35, 35, 94, 94, 116};
    public static final int[] CRYSTAL_X = {64, 16, 112, 16, 112, 64};
    public static final int CRYSTAL_SLOT_START = InventoryArcaneWorkbench.CRAFTING_SLOTS;

    private static final int AURA_REFRESH_INTERVAL = 10;
    private static final double VALID_DISTANCE_SQUARED = 64.0;
    private static final int GRID_SIZE = 3;
    private static final int GRID_ORIGIN = 41;
    private static final int GRID_SPACING = 23;
    private static final int RESULT_X = 160;
    private static final int RESULT_Y = 64;
    private static final int SLOT_SPACING = 18;
    private static final int INVENTORY_X = 16;
    private static final int INVENTORY_Y = 151;
    private static final int HOTBAR_Y = 209;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;
    private static final int RESULT_SLOT = 0;
    private static final int GRID_START = 1;
    private static final int GRID_END = GRID_START + InventoryArcaneWorkbench.CRAFTING_SLOTS;
    private static final int CRYSTAL_MENU_START = GRID_END;
    private static final int WAND_MENU_SLOT = CRYSTAL_MENU_START + InventoryArcaneWorkbench.CRYSTAL_SLOTS;
    private static final int PLAYER_START = WAND_MENU_SLOT + 1;
    private static final int MAIN_END = PLAYER_START + INVENTORY_ROWS * INVENTORY_COLUMNS;
    private static final int PLAYER_END = MAIN_END + INVENTORY_COLUMNS;
    private static final int PLAYER_MAIN_FIRST = INVENTORY_COLUMNS;
    private static final int HOTBAR_LAST_KEY = 8;
    private static final int OFFHAND_KEY = 40;
    private static final int AURA_DATA_SLOT = 0;
    private static final String STAFF_MESSAGE = "message.thaumaturge.arcane_workbench.staff";

    private final InventoryArcaneWorkbench inventory;
    private final ContainerLevelAccess access;
    private final ResultContainer result = new ResultContainer();
    private final SimpleContainerData auraData = new SimpleContainerData(1);
    private final SlotArcaneResult resultSlot;
    private final Player player;
    private final @Nullable BlockEntityArcaneWorkbench workbench;
    private final @Nullable ServerPlayer serverPlayer;
    private final @Nullable ServerLevel serverLevel;
    private final Runnable changeListener = this::inventoryChanged;
    private @Nullable RecipeHolder<CraftingRecipe> usedRecipe;
    private boolean arcaneResult;
    private boolean auraRead;
    private long lastAuraRefresh;

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, new InventoryArcaneWorkbench(), ContainerLevelAccess.create(playerInventory.player.level(), buffer.readBlockPos()), null);
    }

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, BlockEntityArcaneWorkbench workbench) {
        this(containerId, playerInventory, workbench.getInventory(), accessFor(workbench), workbench);
    }

    private MenuArcaneWorkbench(int containerId, Inventory playerInventory, InventoryArcaneWorkbench inventory, ContainerLevelAccess access, @Nullable BlockEntityArcaneWorkbench workbench) {
        super(TTMenus.ARCANE_WORKBENCH.get(), containerId);
        this.inventory = inventory;
        this.access = access;
        this.workbench = workbench;
        this.player = playerInventory.player;
        this.serverPlayer = player instanceof ServerPlayer sp ? sp : null;
        this.serverLevel = workbench != null && workbench.getLevel() instanceof ServerLevel sl ? sl : null;
        this.resultSlot = new SlotArcaneResult(result, this, RESULT_X, RESULT_Y);
        addSlot(resultSlot);
        addCraftingGrid();
        addCrystalSlots();
        addSlot(new SlotWorkbenchWand(inventory, InventoryArcaneWorkbench.WAND_SLOT, WAND_X, WAND_Y));
        addPlayerSlots(playerInventory);
        addDataSlots(auraData);
        if (craftsOnServer()) {
            inventory.addChangedListener(changeListener);
            refreshResult();
        }
    }

    private void addCraftingGrid() {
        for (int cell = 0; cell < GRID_SIZE * GRID_SIZE; cell++) {
            addSlot(new Slot(inventory, cell, GRID_ORIGIN + cell % GRID_SIZE * GRID_SPACING, GRID_ORIGIN + cell / GRID_SIZE * GRID_SPACING));
        }
    }

    private void addCrystalSlots() {
        int index = 0;
        for (ResourceKey<IAspect> primal : PRIMAL_ORDER) {
            addSlot(new SlotCrystalEssentia(inventory, CRYSTAL_SLOT_START + index, CRYSTAL_X[index], CRYSTAL_Y[index], primal));
            index++;
        }
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int cell = 0; cell < INVENTORY_ROWS * INVENTORY_COLUMNS; cell++) {
            int x = INVENTORY_X + cell % INVENTORY_COLUMNS * SLOT_SPACING;
            int y = INVENTORY_Y + cell / INVENTORY_COLUMNS * SLOT_SPACING;
            addSlot(new Slot(playerInventory, PLAYER_MAIN_FIRST + cell, x, y));
        }
        for (int hotbar = 0; hotbar < INVENTORY_COLUMNS; hotbar++) {
            addSlot(new Slot(playerInventory, hotbar, INVENTORY_X + hotbar * SLOT_SPACING, HOTBAR_Y));
        }
    }

    private static ContainerLevelAccess accessFor(BlockEntityArcaneWorkbench workbench) {
        Level level = workbench.getLevel();
        return level == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(level, workbench.getBlockPos());
    }

    boolean craftsOnServer() {
        return workbench != null && serverPlayer != null && serverLevel != null;
    }

    private void inventoryChanged() {
        slotsChanged(inventory);
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == inventory) {
            refreshResult();
        }
    }

    private ArcaneWorkbenchContext context() {
        return ArcaneWorkbenchContext.placed(serverPlayer, workbench.getBlockPos(), workbench.hostIdentity(), null);
    }

    private void refreshResult() {
        if (!craftsOnServer()) {
            return;
        }
        arcaneResult = false;
        usedRecipe = null;
        ItemStack output = ItemStack.EMPTY;
        ArcaneCraftingInput arcaneInput = inventory.asPositionedArcaneCraftInput().input();
        if (arcaneInput.width() > 0) {
            ArcaneCraftingTransaction.Result preview = ArcaneCraftingTransaction.preview(context(), serverPlayer, arcaneInput.withPlayer(serverPlayer));
            if (preview.successful() && !preview.output().isEmpty()) {
                arcaneResult = true;
                output = preview.output();
            }
        }
        if (!arcaneResult) {
            CraftingInput craftingInput = inventory.asPositionedCraftInput().input();
            if (craftingInput.width() > 0) {
                Optional<RecipeHolder<CraftingRecipe>> match = serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftingInput, serverLevel);
                if (match.isPresent()) {
                    usedRecipe = match.get();
                    output = usedRecipe.value().assemble(craftingInput);
                }
            }
        }
        result.setItem(0, output);
    }

    ItemStack takeCraft() {
        if (!craftsOnServer()) {
            return ItemStack.EMPTY;
        }
        ItemStack crafted;
        try (Transaction transaction = Transaction.openRoot()) {
            crafted = craft(transaction);
            if (!crafted.isEmpty()) {
                transaction.commit();
            }
        }
        if (crafted.isEmpty()) {
            refreshResult();
        }
        return crafted;
    }

    private ItemStack craft(TransactionContext transaction) {
        ItemStack shown = result.getItem(0).copy();
        if (shown.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack crafted = arcaneResult ? craftArcane(transaction) : craftVanilla(transaction);
        return !crafted.isEmpty() && ItemStack.matches(crafted, shown) ? crafted : ItemStack.EMPTY;
    }

    private ItemStack craftArcane(TransactionContext transaction) {
        ArcaneCraftingInput.Positioned positioned = inventory.asPositionedArcaneCraftInput();
        ArcaneCraftingInput input = positioned.input().withPlayer(serverPlayer);
        if (input.width() == 0) {
            return ItemStack.EMPTY;
        }
        WorkbenchCraftingStore store = new WorkbenchCraftingStore(inventory, serverPlayer, positioned.left(), positioned.top(), input.width(), input.height());
        ArcaneCraftingTransaction.Result crafted = ArcaneCraftingTransaction.craft(context(), serverPlayer, input, store, transaction);
        return crafted.successful() ? crafted.output() : ItemStack.EMPTY;
    }

    private ItemStack craftVanilla(TransactionContext transaction) {
        if (usedRecipe == null) {
            return ItemStack.EMPTY;
        }
        CraftingInput.Positioned positioned = inventory.asPositionedCraftInput();
        CraftingInput input = positioned.input();
        CraftingRecipe recipe = usedRecipe.value();
        if (input.width() == 0 || !recipe.matches(input, serverLevel)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = recipe.assemble(input);
        WorkbenchCraftingStore store = new WorkbenchCraftingStore(inventory, serverPlayer, positioned.left(), positioned.top(), input.width(), input.height());
        IArcaneCraftingStore.Consumption consumption = new IArcaneCraftingStore.Consumption(input.items(), recipe.getRemainingItems(input), AspectList.EMPTY, inventory.wandStack());
        if (!store.consume(consumption, transaction)) {
            return ItemStack.EMPTY;
        }
        ItemStack recorded = output.copy();
        ServerPlayer crafter = serverPlayer;
        new RootCommitJournal(() -> ResearchProgressionEvents.recordCrafted(crafter, recorded)).updateSnapshots(transaction);
        return output;
    }

    @Override
    public void broadcastChanges() {
        if (craftsOnServer()) {
            long time = serverLevel.getGameTime();
            if (!auraRead || time < lastAuraRefresh || time - lastAuraRefresh >= AURA_REFRESH_INTERVAL) {
                workbench.refreshAura();
                boolean first = !auraRead;
                auraRead = true;
                lastAuraRefresh = time;
                if (first || workbench.auraVis != auraData.get(AURA_DATA_SLOT)) {
                    auraData.set(AURA_DATA_SLOT, workbench.auraVis);
                    refreshResult();
                }
            }
        }
        super.broadcastChanges();
    }

    public int getCachedVis() {
        return auraData.get(AURA_DATA_SLOT);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> isReachable(player, level, pos), true);
    }

    private static boolean isReachable(Player user, Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(TTBlocks.ARCANE_WORKBENCH)) {
            return false;
        }
        return user.distanceToSqr(pos.getCenter()) <= VALID_DISTANCE_SQUARED;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.removeChangedListener(changeListener);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot slot) {
        return slot.container != result && super.canTakeItemForPickAll(carried, slot);
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput input, Player clicker) {
        if (slotIndex == WAND_MENU_SLOT && clicker instanceof ServerPlayer && isStaff(getCarried())) {
            TTActionBar.sendPurple(clicker, STAFF_MESSAGE);
        }
        if (input == ContainerInput.SWAP && slotIndex == RESULT_SLOT && craftsOnServer()) {
            swapCraft(button, clicker);
            return;
        }
        super.clicked(slotIndex, button, input, clicker);
    }

    private static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof ItemWand wand && wand.usesStaffRod(stack);
    }

    private void swapCraft(int key, Player clicker) {
        Inventory carrier = clicker.getInventory();
        ItemStack crafted = canSwapInto(carrier, key) ? takeCraft() : ItemStack.EMPTY;
        if (crafted.isEmpty()) {
            return;
        }
        carrier.setItem(key, crafted);
        resultSlot.onSwapCraft(crafted.getCount());
        resultSlot.onTake(clicker, crafted);
    }

    private boolean canSwapInto(Inventory carrier, int key) {
        boolean hotbarKey = key >= 0 && key <= HOTBAR_LAST_KEY;
        if (!hotbarKey && key != OFFHAND_KEY) {
            return false;
        }
        return carrier.getItem(key).isEmpty() && resultSlot.hasItem();
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (index == RESULT_SLOT) {
            return craftsOnServer() ? quickCraft(clicker) : quickCraftPrediction(clicker, slot);
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index >= PLAYER_START ? moveFromPlayer(stack, index) : moveItemStackTo(stack, PLAYER_START, PLAYER_END, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        return finishMove(clicker, slot, stack, original);
    }

    private boolean moveFromPlayer(ItemStack stack, int index) {
        boolean moved = SlotWorkbenchWand.isUsableWand(stack) && moveItemStackTo(stack, WAND_MENU_SLOT, PLAYER_START, false);
        moved |= routeCrystals(stack);
        moved |= !stack.isEmpty() && moveItemStackTo(stack, GRID_START, GRID_END, false);
        if (moved) {
            return true;
        }
        boolean inMain = index < MAIN_END;
        return inMain ? moveItemStackTo(stack, MAIN_END, PLAYER_END, false) : moveItemStackTo(stack, PLAYER_START, MAIN_END, false);
    }

    private boolean routeCrystals(ItemStack stack) {
        boolean moved = false;
        int target = CRYSTAL_MENU_START;
        for (ResourceKey<IAspect> primal : PRIMAL_ORDER) {
            if (stack.isEmpty()) {
                break;
            }
            if (SlotCrystalEssentia.isValidCrystal(stack, primal)) {
                moved |= moveItemStackTo(stack, target, target + 1, false);
            }
            target++;
        }
        return moved;
    }

    private ItemStack finishMove(Player clicker, Slot slot, ItemStack stack, ItemStack original) {
        settle(slot, stack);
        if (stack.getCount() != original.getCount()) {
            slot.onTake(clicker, stack);
            return original;
        }
        return ItemStack.EMPTY;
    }

    private static void settle(Slot slot, ItemStack remaining) {
        if (!remaining.isEmpty()) {
            slot.setChanged();
            return;
        }
        slot.setByPlayer(ItemStack.EMPTY);
    }

    private ItemStack quickCraftPrediction(Player clicker, Slot slot) {
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
            return ItemStack.EMPTY;
        }
        slot.onQuickCraft(stack, original);
        return finishMove(clicker, slot, stack, original);
    }

    private ItemStack quickCraft(Player clicker) {
        ItemStack crafted;
        ItemStack remainder;
        try (Transaction transaction = Transaction.openRoot()) {
            crafted = craft(transaction);
            remainder = crafted.copy();
            if (!crafted.isEmpty() && moveItemStackTo(remainder, PLAYER_START, PLAYER_END, true)) {
                transaction.commit();
            } else {
                crafted = ItemStack.EMPTY;
            }
        }
        if (crafted.isEmpty()) {
            refreshResult();
            return ItemStack.EMPTY;
        }
        resultSlot.onQuickCraft(remainder, crafted);
        if (!remainder.isEmpty()) {
            clicker.drop(remainder, false);
        }
        resultSlot.onTake(clicker, crafted);
        return crafted.copy();
    }

    public InventoryArcaneWorkbench getCraftingInventory() {
        return inventory;
    }
}
