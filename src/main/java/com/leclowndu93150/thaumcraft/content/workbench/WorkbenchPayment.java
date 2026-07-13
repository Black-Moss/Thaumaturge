package com.leclowndu93150.thaumcraft.content.workbench;

import com.leclowndu93150.thaumcraft.api.aspect.AspectInstance;
import com.leclowndu93150.thaumcraft.api.aspect.AspectList;
import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import com.leclowndu93150.thaumcraft.api.aspect.TCAspects;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.content.casters.CasterManager;
import com.leclowndu93150.thaumcraft.content.taint.item.ItemEssentiaCrystal;
import com.leclowndu93150.thaumcraft.content.wands.ItemWand;
import com.leclowndu93150.thaumcraft.content.wands.WandEconomy;
import com.leclowndu93150.thaumcraft.content.wands.WandVisHelper;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WorkbenchPayment {

    public record Plan(
            boolean fullWand,
            Map<ResourceKey<IAspect>, Integer> wandCentivis,
            AspectList crystalsToConsume,
            int auraVis,
            boolean crystalsSatisfied
    ) {}

    private WorkbenchPayment() {}

    public static Plan plan(IArcaneRecipe recipe, InventoryArcaneWorkbench inventory, Player player) {
        ItemStack wand = inventory.wandStack();
        boolean hasWand = wand.getItem() instanceof ItemWand;
        Map<ResourceKey<IAspect>, Integer> wandCentivis = new LinkedHashMap<>();
        AspectList crystalNeeds = AspectList.EMPTY;
        for (AspectInstance entry : recipe.getCrystals().entries()) {
            ResourceKey<IAspect> primal = entry.aspect().getKey();
            int centivis = entry.amount() * WandEconomy.CRYSTAL_SUBSTITUTE_VIS * WandEconomy.CENTIVIS_PER_VIS;
            Map<ResourceKey<IAspect>, Integer> single = new LinkedHashMap<>();
            single.put(primal, centivis);
            if (hasWand && WandVisHelper.consumeAllVisRaw(wand, single, false)) {
                wandCentivis.put(primal, centivis);
            } else {
                crystalNeeds = crystalNeeds.add(entry.aspect(), entry.amount());
            }
        }
        boolean fullWand = hasWand && crystalNeeds.isEmpty();
        float modifier;
        if (fullWand) {
            modifier = averageCraftModifier(wand, player);
        } else if (!crystalNeeds.isEmpty()) {
            modifier = WandEconomy.CRAFT_AURA_SURCHARGE * gearModifier(player);
        } else {
            modifier = gearModifier(player);
        }
        int auraVis = recipe.getBaseVis() <= 0 ? 0 : Math.max(1, Mth.ceil(recipe.getBaseVis() * modifier));
        return new Plan(fullWand, wandCentivis, crystalNeeds, auraVis, hasCrystals(inventory, crystalNeeds));
    }

    public static boolean canCraft(Plan plan, @Nullable BlockEntityArcaneWorkbench tile) {
        if (!plan.crystalsSatisfied()) {
            return false;
        }
        return plan.auraVis() <= 0 || (tile != null && tile.auraVis >= plan.auraVis());
    }

    public static void pay(Plan plan, @Nullable BlockEntityArcaneWorkbench tile, Player player,
                           InventoryArcaneWorkbench inventory) {
        if (!plan.wandCentivis().isEmpty()) {
            WandVisHelper.consumeAllVisRaw(inventory.wandStack(), plan.wandCentivis(), true);
        }
        if (!plan.crystalsToConsume().isEmpty()) {
            consumeCrystals(inventory, plan.crystalsToConsume());
        }
        if (plan.auraVis() > 0 && tile != null) {
            tile.spendAura(plan.auraVis());
        }
    }

    public static int crudeCost(IArcaneRecipe recipe) {
        return Mth.ceil(recipe.getBaseVis() * WandEconomy.CRAFT_AURA_SURCHARGE);
    }

    private static float averageCraftModifier(ItemStack wand, Player player) {
        float total = 0.0F;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            total += WandVisHelper.getConsumptionModifier(wand, player, primal, true);
        }
        return total / WandEconomy.PRIMAL_COUNT;
    }

    private static float gearModifier(Player player) {
        return Math.max(1.0F - CasterManager.getTotalVisDiscount(player), WandEconomy.MIN_CONSUMPTION_MODIFIER);
    }

    private static boolean hasCrystals(InventoryArcaneWorkbench inventory, AspectList needs) {
        for (AspectInstance entry : needs.entries()) {
            int found = 0;
            for (int i = InventoryArcaneWorkbench.CRAFTING_SLOTS; i < InventoryArcaneWorkbench.WAND_SLOT; i++) {
                ItemStack crystal = inventory.getItem(i);
                if (!crystal.isEmpty() && crystal.getItem() instanceof ItemEssentiaCrystal) {
                    Holder<IAspect> holder = ItemEssentiaCrystal.aspectOf(crystal);
                    if (holder != null && holder.value().tag().equals(entry.aspect().value().tag())) {
                        found += crystal.getCount();
                    }
                }
            }
            if (found < entry.amount()) {
                return false;
            }
        }
        return true;
    }

    private static void consumeCrystals(InventoryArcaneWorkbench inventory, AspectList crystals) {
        for (AspectInstance entry : crystals.entries()) {
            int needed = entry.amount();
            for (int i = InventoryArcaneWorkbench.CRAFTING_SLOTS; i < InventoryArcaneWorkbench.WAND_SLOT && needed > 0; i++) {
                ItemStack crystal = inventory.getItem(i);
                if (!crystal.isEmpty() && crystal.getItem() instanceof ItemEssentiaCrystal) {
                    Holder<IAspect> holder = ItemEssentiaCrystal.aspectOf(crystal);
                    if (holder != null && holder.value().tag().equals(entry.aspect().value().tag())) {
                        int remove = Math.min(needed, crystal.getCount());
                        inventory.removeItem(i, remove);
                        needed -= remove;
                    }
                }
            }
        }
    }
}
