package com.leclowndu93150.thaumaturge.content.research.pool;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class AspectPoolBindings implements AspectPoolAccess.Bindings {
    @Override
    public boolean isDiscovered(Player player, Holder<IAspect> aspect) {
        return AspectPools.isDiscovered(player, aspect);
    }

    @Override
    public int amount(Player player, Holder<IAspect> aspect) {
        return AspectPools.amount(player, aspect);
    }

    @Override
    public boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect) {
        return AspectPools.hasDiscoveredComponents(player, aspect);
    }

    @Override
    public int grant(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        return AspectPools.grant(player, aspect, amount);
    }

    @Override
    public void grantAll(ServerPlayer player, AspectList aspects) {
        AspectPools.grantAll(player, aspects);
    }

    @Override
    public boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        return AspectPools.spend(player, aspect, amount);
    }

    @Override
    public boolean canAfford(Player player, AspectList cost) {
        return AspectPools.canAfford(player, cost);
    }

    @Override
    public boolean spendAll(ServerPlayer player, AspectList cost) {
        return AspectPools.spendAll(player, cost);
    }

    @Override
    public void refund(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        AspectPools.refund(player, aspect, amount);
    }
}
