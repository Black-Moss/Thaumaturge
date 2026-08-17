package com.leclowndu93150.thaumaturge.api.capability;

import java.util.function.Function;
import net.minecraft.world.entity.player.Player;

/**
 * Static accessor for a player's {@link IPlayerKnowledge} record.
 *
 * <p>
 * The accessor is bound at mod init time by the implementation. Addons use {@link #of(Player)} to read or mutate a
 * player's record without depending on the internal attachment registry.
 *
 * @since 1.0.0
 */
public final class KnowledgeAccess {
    private static Function<Player, IPlayerKnowledge> binding;

    private KnowledgeAccess() {}

    /**
     * Returns the player's knowledge record, creating an empty one when absent.
     *
     * @param player the player; may be a client or server player
     * @return the player's knowledge record, never null
     * @throws IllegalStateException when called before the implementation has bound the accessor
     */
    public static IPlayerKnowledge of(Player player) {
        if (binding == null) {
            throw new IllegalStateException("KnowledgeAccess accessed before binding");
        }
        return binding.apply(player);
    }

    /**
     * Binds the accessor's implementation. Called once at mod init by the implementation; addons must not call this.
     *
     * @param impl the accessor implementation
     * @apiNote This is a one-shot binding; calling it more than once throws.
     * @throws IllegalStateException when already bound
     */
    public static void bind(Function<Player, IPlayerKnowledge> impl) {
        if (binding != null) {
            throw new IllegalStateException("KnowledgeAccess already bound");
        }
        binding = impl;
    }
}
