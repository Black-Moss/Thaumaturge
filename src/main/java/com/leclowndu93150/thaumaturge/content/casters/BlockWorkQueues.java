package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockWorkQueues {
    private static final int DEFAULT_SWAP_COLOR = 0xFFFFFF;
    private static final float DEFAULT_STRENGTH = 1.0F;
    private static final float DEFAULT_DURABILITY_MAX = 1.0F;
    private static final Predicate<SwapContext> ALWAYS_ALLOWED = context -> true;
    private static final VisCharge NO_CHARGE = new VisCharge(0.0F, null);

    private final List<BreakerTask> breakers = new ArrayList<>();
    private final List<SwapperTask> swappers = new ArrayList<>();

    private static BlockWorkQueues forLevel(ServerLevel level) {
        return level.getData(TTAttachments.BLOCK_WORK_QUEUES);
    }

    public List<BreakerTask> breakers() {
        return breakers;
    }

    public List<SwapperTask> swappers() {
        return swappers;
    }

    private record VisCharge(float cost, @Nullable ResourceKey<IAspect> aspect) {
    }

    public record SwapContext(ServerLevel level, Player player, BlockPos pos) {
    }

    public record BreakerTask(BlockPos pos, BlockState source, UUID playerId, boolean fx, boolean silk, int fortune, float strength, float durability, float durabilityMax, int delay, float visCost,
            @Nullable ResourceKey<IAspect> visAspect) {
        BreakerTask withDelay(int newDelay) {
            return new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, durability, durabilityMax, newDelay, visCost, visAspect);
        }

        BreakerTask withDurability(float newDurability) {
            return new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, newDurability, durabilityMax, delay, visCost, visAspect);
        }

        public static final class Builder {
            private final BlockPos pos;
            private final BlockState source;
            private final UUID playerId;
            private boolean fx;
            private boolean silk;
            private int fortune;
            private float strength = DEFAULT_STRENGTH;
            private float durability;
            private float durabilityMax = DEFAULT_DURABILITY_MAX;
            private int delay;
            private VisCharge charge = NO_CHARGE;

            Builder(BlockPos pos, BlockState source, UUID playerId) {
                this.pos = pos.immutable();
                this.source = source;
                this.playerId = playerId;
            }

            public Builder showFx() {
                fx = true;
                return this;
            }

            public Builder silkTouch(boolean enabled) {
                silk = enabled;
                return this;
            }

            public Builder fortune(int level) {
                fortune = level;
                return this;
            }

            public Builder strength(float strength) {
                this.strength = strength;
                return this;
            }

            public Builder durability(float durability) {
                this.durability = durability;
                this.durabilityMax = durability;
                return this;
            }

            public Builder delay(int delay) {
                this.delay = delay;
                return this;
            }

            public Builder visCost(float amount, @Nullable ResourceKey<IAspect> type) {
                charge = new VisCharge(amount, type);
                return this;
            }

            public void queue(ServerLevel level) {
                BreakerTask task = build();
                forLevel(level).breakers.add(task);
            }

            private BreakerTask build() {
                return new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, durability, durabilityMax, delay, charge.cost(), charge.aspect());
            }
        }
    }

    public record SwapperTask(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, boolean consumeTarget, int lifespan, UUID playerId, boolean fx, boolean fancy, int color,
            boolean pickup, boolean silk, int fortune, Predicate<SwapContext> allowSwap, float visCost, @Nullable ResourceKey<IAspect> visAspect) {
        SwapperTask propagatedTo(BlockPos newPos) {
            int remainingLife = lifespan - 1;
            return new SwapperTask(newPos, source(), target(), consumeTarget(), remainingLife, playerId(), fx(), fancy(), color(), pickup(), silk(), fortune(), allowSwap(), visCost(), visAspect());
        }

        public static final class Builder {
            private final BlockPos pos;
            private final @Nullable BlockState source;
            private final @Nullable BlockState target;
            private final UUID playerId;
            private boolean consumeTarget;
            private int lifespan;
            private boolean fx;
            private boolean fancy;
            private int color = DEFAULT_SWAP_COLOR;
            private boolean pickup;
            private boolean silk;
            private int fortune;
            private Predicate<SwapContext> allowSwap = ALWAYS_ALLOWED;
            private VisCharge charge = NO_CHARGE;

            Builder(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, UUID playerId) {
                this.pos = pos.immutable();
                this.source = source;
                this.target = target;
                this.playerId = playerId;
            }

            public Builder consumeTarget() {
                consumeTarget = true;
                return this;
            }

            public Builder lifespan(int lifespan) {
                this.lifespan = lifespan;
                return this;
            }

            public Builder showFx(int rgb, boolean sparkle) {
                fancy = sparkle;
                color = rgb;
                fx = true;
                return this;
            }

            public Builder pickupDrops() {
                pickup = true;
                return this;
            }

            public Builder silkTouch(boolean enabled) {
                silk = enabled;
                return this;
            }

            public Builder fortune(int level) {
                fortune = level;
                return this;
            }

            public Builder allowSwap(Predicate<SwapContext> allowSwap) {
                this.allowSwap = allowSwap;
                return this;
            }

            public Builder visCost(float amount, @Nullable ResourceKey<IAspect> type) {
                charge = new VisCharge(amount, type);
                return this;
            }

            public void queue(ServerLevel level) {
                SwapperTask task = build();
                forLevel(level).swappers.add(task);
            }

            private SwapperTask build() {
                return new SwapperTask(this.pos, this.source, this.target, this.consumeTarget, this.lifespan, this.playerId, this.fx, this.fancy, this.color, this.pickup, this.silk, this.fortune,
                        this.allowSwap, charge.cost(), charge.aspect());
            }
        }
    }
}
