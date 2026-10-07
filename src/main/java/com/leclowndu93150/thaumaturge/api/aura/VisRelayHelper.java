package com.leclowndu93150.thaumaturge.api.aura;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Static accessor for drawing primal vis from the relay network fed by energized aura nodes.
 *
 * <p>A consumer at a position asks for centivis of a primal aspect; the network locates a linked
 * vis relay near that position, walks the relay chain to its energized source node, and drains the
 * node's centivis buffer. The buffer accrues per tick at a rate proportional to the node's base
 * aspect reserves, so larger nodes sustain hungrier consumers.
 *
 * <p>Server side only. The implementation binds at mod initialization; calls before binding
 * return zero.
 *
 * @since 1.0.0
 */
public final class VisRelayHelper {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("VisRelayHelper");

    private VisRelayHelper() {}

    /**
     * Internal operations the helper delegates to the implementation.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Drains centivis of the given primal from the relay network reachable from a position.
         *
         * @param level the server level
         * @param consumerPos the consuming block or player position
         * @param primal the primal aspect key
         * @param amount the requested centivis
         * @param simulate when {@code true}, reports what could be drained without draining
         * @return the centivis drained, or drainable when simulating; zero when no linked relay
         *         with an energized source is in range
         */
        int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, boolean simulate);

        /**
         * Drains centivis of the given primal from the relay network as part of a transaction.
         *
         * @param level the server level
         * @param consumerPos the consuming block or player position
         * @param primal the primal aspect key
         * @param amount the requested centivis
         * @param transaction the open transaction the drain belongs to
         * @return the centivis drained; zero when no linked relay with an energized source is in
         *         range
         */
        int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, TransactionContext transaction);
    }

    /**
     * Installs the implementation. Called once by the mod during initialization; addons must not
     * call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when an implementation is already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * Drains centivis of the given primal from the relay network reachable from a position.
     *
     * @param level the server level
     * @param consumerPos the consuming block or player position
     * @param primal the primal aspect key
     * @param amount the requested centivis
     * @param simulate when {@code true}, reports what could be drained without draining
     * @return the centivis drained, or drainable when simulating; zero when unavailable
     */
    public static int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, boolean simulate) {
        return BINDING.isBound() ? BINDING.get().drainCentivis(level, consumerPos, primal, amount, simulate) : 0;
    }

    /**
     * Drains centivis of the given primal from the relay network as part of a transaction.
     *
     * <p>The drain is undone when the transaction or any enclosing transaction aborts. The source
     * node syncs its contents and the relay chain flashes the drained aspect's colour only once
     * the outermost transaction commits.
     *
     * @param level the server level
     * @param consumerPos the consuming block or player position
     * @param primal the primal aspect key
     * @param amount the requested centivis
     * @param transaction the open transaction the drain belongs to
     * @return the centivis drained; zero when unavailable
     * @since 1.0.0
     */
    public static int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, TransactionContext transaction) {
        return BINDING.isBound() ? BINDING.get().drainCentivis(level, consumerPos, primal, amount, transaction) : 0;
    }
}
