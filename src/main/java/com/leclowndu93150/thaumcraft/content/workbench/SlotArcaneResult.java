package com.leclowndu93150.thaumcraft.content.workbench;

import com.leclowndu93150.thaumcraft.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.content.recipe.ThaumcraftCraftingManager;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumcraft.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemEssentiaCrystal;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class SlotArcaneResult extends Slot {
    private final InventoryArcaneWorkbench craftMatrix;
    private final @Nullable BlockEntityArcaneWorkbench tile;
    private int amountCrafted;

    public SlotArcaneResult(
        ResultContainer result,
        InventoryArcaneWorkbench craftMatrix,
        @Nullable BlockEntityArcaneWorkbench tile,
        int x, int y
    ) {
        super(result, 0, x, y);
        this.craftMatrix = craftMatrix;
        this.tile = tile;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        amountCrafted = 0;

        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player instanceof ServerPlayer serverPlayer) {
            ResearchProgressionEvents.recordCrafted(serverPlayer, stack);
        }

        ArcaneCraftingInput input = craftMatrix.asArcaneCraftInput();

        ArcaneCraftingRecipe arcane = ThaumcraftCraftingManager.findMatchingArcaneRecipe(serverLevel, input, player);


        List<ItemStack> remaining;
        if (arcane != null) {
            remaining = arcane.getRemainingItems(input);
        } else {
            ResultContainer result = (ResultContainer) this.container;
            RecipeHolder<?> stored = result.getRecipeUsed();
            if (stored != null && stored.value() instanceof CraftingRecipe cr) {
                remaining = cr.getRemainingItems(craftMatrix.asCraftInput());
            } else {
                remaining = input.items().stream().map(s -> ItemStack.EMPTY).toList();
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack existing = craftMatrix.getItem(i);
            ItemStack leftover = i < remaining.size() ? remaining.get(i) : ItemStack.EMPTY;
            if (!existing.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                existing = craftMatrix.getItem(i);
            }
            if (!leftover.isEmpty()) {
                if (existing.isEmpty()) {
                    craftMatrix.setItem(i, leftover);
                } else if (ItemStack.isSameItemSameComponents(existing, leftover)) {
                    leftover.grow(existing.getCount());
                    craftMatrix.setItem(i, leftover);
                } else if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }

        if (arcane != null) {
            int vis = arcane.getReducedVis(player);
            if (vis > 0 && tile != null) {
                tile.spendAura(vis);
            }
            AspectList crystals = arcane.getCrystals();
            if (!crystals.isEmpty()) {
                consumeCrystals(crystals);
            }
        }
    }

    private void consumeCrystals(AspectList crystals) {
        for (AspectInstance entry : crystals.entries()) {
            int needed = entry.amount();
            for (int i = 9; i < 15 && needed > 0; i++) {
                ItemStack crystal = craftMatrix.getItem(i);
                if (!crystal.isEmpty() && crystal.getItem() instanceof ItemEssentiaCrystal) {
                    Holder<IAspect> holder = ItemEssentiaCrystal.aspectOf(crystal);
                    if (holder != null && holder.value().tag().equals(entry.aspect().value().tag())) {
                        int remove = Math.min(needed, crystal.getCount());
                        craftMatrix.removeItem(i, remove);
                        needed -= remove;
                    }
                }
            }
        }
    }
}