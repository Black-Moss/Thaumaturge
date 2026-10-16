package com.leclowndu93150.thaumaturge.api.capability;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.function.Function;
import net.minecraft.world.entity.player.Player;

/**
 * Static entry point to a player's {@link PlayerResearch}.
 *
 * <p>Thaumaturge binds the implementation during mod construction, so addons can call {@link #of} from any later
 * event without depending on the mod's attachment registry.
 *
 * @since 1.1.0
 */
public final class PlayerResearchAccess {
    private static final ApiBinding<Function<Player, PlayerResearch>> BINDING = new ApiBinding<>("PlayerResearchAccess");

    private PlayerResearchAccess() {}

    /**
     * Returns a player's research progress, creating empty progress when the player has none yet.
     *
     * @param player a client or server player
     * @return the player's progress, never {@code null}; the same object for the same player until the player entity is
     *     replaced, as on respawn
     * @throws IllegalStateException when called before Thaumaturge bound the accessor
     * @since 1.1.0
     */
    public static PlayerResearch of(Player player) {
        return BINDING.get().apply(player);
    }

    /**
     * Installs the implementation. Thaumaturge calls this once during mod construction; addons must not call it.
     *
     * @param impl the function returning a player's progress
     * @throws IllegalStateException when an implementation is already installed
     * @since 1.1.0
     */
    public static void bind(Function<Player, PlayerResearch> impl) {
        BINDING.bind(impl);
    }
}
