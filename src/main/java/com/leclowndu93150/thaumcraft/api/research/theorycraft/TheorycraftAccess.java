package com.leclowndu93150.thaumcraft.api.research.theorycraft;

import java.util.function.Function;
import net.minecraft.world.entity.player.Player;

/**
 * Static accessor for a player's active {@link IResearchTableData research table session}.
 *
 * <p>Players carry the session as a data attachment that is reset on death and that is only
 * meaningful while the player has a research table screen open. Reading the session for a player
 * who is not in a research session returns the default empty record.
 *
 * @since 1.0.0
 */
public final class TheorycraftAccess {
    private static Function<Player, IResearchTableData> binding;

    private TheorycraftAccess() {}

    /**
     * Returns the player's research table session, creating an empty one when absent.
     *
     * @param player the player
     * @return the player's research session, never null
     * @throws IllegalStateException when called before the implementation has bound the accessor
     */
    public static IResearchTableData of(Player player) {
        if (binding == null) {
            throw new IllegalStateException("TheorycraftAccess accessed before binding");
        }
        return binding.apply(player);
    }

    /**
     * Binds the accessor implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param impl the accessor implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Function<Player, IResearchTableData> impl) {
        if (binding != null) {
            throw new IllegalStateException("TheorycraftAccess already bound");
        }
        binding = impl;
    }
}
