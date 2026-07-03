package com.leclowndu93150.thaumcraft.content.workbench;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.ThaumcraftCraftingManager;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumcraft.registry.TCBlocks;
import com.leclowndu93150.thaumcraft.registry.TCMenus;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class MenuArcaneWorkbench extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int CRYSTAL_START = 10;
    private static final int PLAYER_INV_START = 16;
    private static final int PLAYER_INV_END = 43;
    private static final int HOTBAR_START = 43;
    private static final int HOTBAR_END = 52;

    private static final int RESULT_X = 160;
    private static final int RESULT_Y = 64;
    private static final int CRAFT_ORIGIN_X = 40;
    private static final int CRAFT_ORIGIN_Y = 40;
    private static final int CRAFT_SPACING = 24;
    public static final int[] CRYSTAL_X = {64, 17, 112, 17, 112, 64};
    public static final int[] CRYSTAL_Y = {13, 35, 35, 93, 93, 115};
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_INV_Y = 151;
    private static final int HOTBAR_Y = 209;
    private static final int SLOT_SPACING = 18;

    public static final List<ResourceKey<IAspect>> PRIMAL_ORDER = List.of(
        TCAspects.AER, TCAspects.IGNIS, TCAspects.AQUA, TCAspects.TERRA, TCAspects.ORDO, TCAspects.PERDITIO
    );

    private static final int AURA_DATA_INDEX = 0;
    private static final int AURA_REFRESH_INTERVAL = 10;

    private final InventoryArcaneWorkbench craftingInventory;
    private final ResultContainer resultContainer = new ResultContainer();
    private final SimpleContainerData containerData = new SimpleContainerData(1);
    private final ContainerLevelAccess access;
    private final Player player;
    private final @Nullable BlockEntityArcaneWorkbench tile;
    private final Runnable onChange;
    private long lastAuraRefresh = Long.MIN_VALUE;
    private int lastVis = -1;

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new InventoryArcaneWorkbench(),
            ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()), null);
    }

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, BlockEntityArcaneWorkbench tile) {
        this(containerId, playerInventory, tile.getInventory(),
            ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()), tile);
    }

    private MenuArcaneWorkbench(
        int containerId, Inventory playerInventory,
        InventoryArcaneWorkbench craftingInventory, ContainerLevelAccess access,
        @Nullable BlockEntityArcaneWorkbench tile
    ) {
        super(TCMenus.ARCANE_WORKBENCH.get(), containerId);
        this.craftingInventory = craftingInventory;
        this.access = access;
        this.player = playerInventory.player;
        this.tile = tile;
        this.onChange = () -> slotsChanged(craftingInventory);

        craftingInventory.addChangedListener(onChange);
        addSlots(playerInventory);
        addDataSlots(containerData);
        slotsChanged(craftingInventory);
    }

    private void addSlots(Inventory playerInventory) {
        addSlot(new SlotArcaneResult(resultContainer, craftingInventory, tile, RESULT_X, RESULT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftingInventory, col + row * 3,
                    CRAFT_ORIGIN_X + col * CRAFT_SPACING, CRAFT_ORIGIN_Y + row * CRAFT_SPACING));
            }
        }

        for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
            addSlot(new SlotCrystalEssentia(craftingInventory, 9 + i,
                CRYSTAL_X[i], CRYSTAL_Y[i], PRIMAL_ORDER.get(i)));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                    PLAYER_INV_X + col * SLOT_SPACING, PLAYER_INV_Y + row * SLOT_SPACING));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SPACING, HOTBAR_Y));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != craftingInventory) return;
        if (tile == null) return;
        if (!(tile.getLevel() instanceof ServerLevel serverLevel)) return;
        updateResult((ServerPlayer) player, serverLevel);
    }

    private void updateResult(ServerPlayer sp, ServerLevel level) {
        ItemStack result = ItemStack.EMPTY;
        ArcaneCraftingInput input = craftingInventory.asArcaneCraftInput();
        RecipeHolder<?> recipeToStore = null;

        IArcaneRecipe arcane = ThaumcraftCraftingManager.findMatchingArcaneRecipe(level, input, sp);
        if (arcane != null) {
            tile.refreshAura();
            if (tile.auraVis >= arcane.getReducedVis(sp) && hasSufficientCrystals(arcane.getCrystals())) {
                result = arcane.assemble(input);
            }
        }

        CraftingInput vanillaInput = craftingInventory.asCraftInput();
        if (result.isEmpty()) {
            Optional<RecipeHolder<CraftingRecipe>> vanilla = findVanillaRecipe(level, vanillaInput);
            if (vanilla.isPresent()) {
                result = vanilla.get().value().assemble(vanillaInput);
                recipeToStore = vanilla.get();
            }
        }

        resultContainer.setRecipeUsed(recipeToStore);
        resultContainer.setItem(0, result);
    }

    @SuppressWarnings("unchecked")
    private Optional<RecipeHolder<CraftingRecipe>> findVanillaRecipe(ServerLevel level, CraftingInput input) {
        return level.recipeAccess().getRecipes().stream()
            .filter(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof IArcaneRecipe))
            .map(r -> (RecipeHolder<CraftingRecipe>) r)
            .filter(r -> r.value().matches(input, level))
            .findFirst();
    }

    private boolean hasSufficientCrystals(AspectList needed) {
        if (needed.isEmpty()) return true;
        for (AspectInstance entry : needed.entries()) {
            int found = 0;
            for (int i = 9; i < 15; i++) {
                ItemStack crystal = craftingInventory.getItem(i);
                if (!crystal.isEmpty() && crystal.getItem() instanceof ItemEssentiaCrystal) {
                    Holder<IAspect> holder = ItemEssentiaCrystal.aspectOf(crystal);
                    if (holder != null && holder.value().tag().equals(entry.aspect().value().tag())) {
                        found += crystal.getCount();
                    }
                }
            }
            if (found < entry.amount()) return false;
        }
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (tile != null && tile.getLevel() != null) {
            long gameTime = tile.getLevel().getGameTime();
            if (gameTime >= lastAuraRefresh + AURA_REFRESH_INTERVAL) {
                lastAuraRefresh = gameTime;
                tile.refreshAura();
            }
            if (lastVis != tile.auraVis) {
                slotsChanged(craftingInventory);
                containerData.set(AURA_DATA_INDEX, tile.auraVis);
                lastVis = tile.auraVis;
            }
        }
        super.broadcastChanges();
    }

    public int getCachedVis() {
        return containerData.get(AURA_DATA_INDEX);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) ->
            level.getBlockState(pos).is(TCBlocks.ARCANE_WORKBENCH.get())
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        craftingInventory.removeChangedListener(onChange);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == RESULT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (slotIndex >= PLAYER_INV_START && slotIndex < HOTBAR_END) {
            for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
                if (SlotCrystalEssentia.isValidCrystal(stack, PRIMAL_ORDER.get(i))) {
                    if (!this.moveItemStackTo(stack, CRYSTAL_START + i, CRYSTAL_START + i + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (stack.isEmpty()) break;
                }
            }
            if (!stack.isEmpty()) {
                if (slotIndex < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }

    public InventoryArcaneWorkbench getCraftingInventory() {
        return craftingInventory;
    }
}