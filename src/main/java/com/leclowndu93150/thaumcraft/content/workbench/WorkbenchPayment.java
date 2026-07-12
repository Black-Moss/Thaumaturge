package com.leclowndu93150.thaumcraft.content.workbench;

import com.leclowndu93150.thaumcraft.api.casters.ICaster;
import com.leclowndu93150.thaumcraft.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumcraft.content.wands.WandEconomy;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WorkbenchPayment {

    private WorkbenchPayment() {}

    public static int auraCost(IArcaneRecipe recipe) {
        return Mth.ceil(recipe.getBaseVis() * WandEconomy.CRAFT_AURA_SURCHARGE);
    }

    public static boolean wandCanPay(ItemStack wand, Player player, IArcaneRecipe recipe) {
        return wand.getItem() instanceof ICaster caster
                && caster.consumeVis(wand, player, recipe.getBaseVis(), true, true);
    }

    public static boolean canPay(IArcaneRecipe recipe, @Nullable BlockEntityArcaneWorkbench tile,
                                 Player player, ItemStack wand) {
        if (wandCanPay(wand, player, recipe)) {
            return true;
        }
        return tile != null && tile.auraVis >= auraCost(recipe);
    }

    public static void pay(IArcaneRecipe recipe, @Nullable BlockEntityArcaneWorkbench tile,
                           Player player, ItemStack wand) {
        if (wand.getItem() instanceof ICaster caster
                && caster.consumeVis(wand, player, recipe.getBaseVis(), true, false)) {
            return;
        }
        if (tile != null) {
            tile.spendAura(auraCost(recipe));
        }
    }
}
