package com.leclowndu93150.thaumcraft.content.casters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockWorkQueues {
    private final List<BreakerTask> breakers = new ArrayList<>();
    private final List<SwapperTask> swappers = new ArrayList<>();

    public List<BreakerTask> breakers() {
        return breakers;
    }

    public List<SwapperTask> swappers() {
        return swappers;
    }

    public record SwapContext(ServerLevel level, Player player, BlockPos pos) {
    }

    public static final class BreakerTask {
        final BlockPos pos;
        final BlockState source;
        final UUID playerId;
        final boolean fx;
        final boolean silk;
        final int fortune;
        final float strength;
        final float durabilityMax;
        final float visCost;
        float durabilityCurrent;
        int delay;

        BreakerTask(BlockPos pos, BlockState source, UUID playerId, boolean fx, boolean silk, int fortune,
                float strength, float durabilityCurrent, float durabilityMax, int delay, float visCost) {
            this.pos = pos;
            this.source = source;
            this.playerId = playerId;
            this.fx = fx;
            this.silk = silk;
            this.fortune = fortune;
            this.strength = strength;
            this.durabilityCurrent = durabilityCurrent;
            this.durabilityMax = durabilityMax;
            this.delay = delay;
            this.visCost = visCost;
        }
    }

    public static final class SwapperTask {
        final BlockPos pos;
        final @Nullable BlockState source;
        final @Nullable BlockState target;
        final boolean consumeTarget;
        final int lifespan;
        final UUID playerId;
        final boolean fx;
        final boolean fancy;
        final int color;
        final boolean pickup;
        final boolean silk;
        final int fortune;
        final Predicate<SwapContext> allowSwap;
        final float visCost;

        SwapperTask(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, boolean consumeTarget,
                int lifespan, UUID playerId, boolean fx, boolean fancy, int color, boolean pickup, boolean silk,
                int fortune, Predicate<SwapContext> allowSwap, float visCost) {
            this.pos = pos;
            this.source = source;
            this.target = target;
            this.consumeTarget = consumeTarget;
            this.lifespan = lifespan;
            this.playerId = playerId;
            this.fx = fx;
            this.fancy = fancy;
            this.color = color;
            this.pickup = pickup;
            this.silk = silk;
            this.fortune = fortune;
            this.allowSwap = allowSwap;
            this.visCost = visCost;
        }
    }
}
