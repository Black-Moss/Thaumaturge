package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnUpdate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandRodPrimalOnUpdate implements IWandRodOnUpdate {
    private final @Nullable ResourceKey<IAspect> fixedAspect;
    private final int periodTicks;

    private WandRodPrimalOnUpdate(@Nullable ResourceKey<IAspect> fixedAspect, int periodTicks) {
        this.fixedAspect = fixedAspect;
        this.periodTicks = periodTicks;
    }

    public WandRodPrimalOnUpdate(ResourceKey<IAspect> aspect) {
        this(aspect, WandEconomy.ROD_SELF_CHARGE_INTERVAL_TICKS);
    }

    public WandRodPrimalOnUpdate() {
        this(null, WandEconomy.PRIMAL_SELF_CHARGE_INTERVAL_TICKS);
    }

    @Override
    public void onUpdate(ItemStack wand, Player player) {
        if (player.level().getGameTime() % periodTicks == 0) {
            recharge(wand, player);
        }
    }

    private void recharge(ItemStack wand, Player player) {
        int ceiling = WandVisHelper.capacityOf(wand) / WandEconomy.ROD_SELF_CHARGE_CAP_DIVISOR;
        ResourceKey<IAspect> chosen = fixedAspect == null ? randomBelow(wand, player, ceiling) : fixedAspect;
        if (chosen == null || WandVisHelper.storedIn(wand, chosen) >= ceiling) {
            return;
        }
        WandVisHelper.topUpCentivis(wand, chosen, WandEconomy.CENTIVIS_PER_VIS, true);
    }

    private static @Nullable ResourceKey<IAspect> randomBelow(ItemStack wand, Player player, int ceiling) {
        List<ResourceKey<IAspect>> candidates = new ArrayList<>(WandEconomy.PRIMAL_COUNT);
        TTAspects.PRIMALS.stream().filter(primal -> WandVisHelper.storedIn(wand, primal) < ceiling).forEach(candidates::add);
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(player.getRandom().nextInt(candidates.size()));
    }
}
