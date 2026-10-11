package com.leclowndu93150.thaumaturge.api.capability;

import java.util.function.Function;
import net.minecraft.world.entity.player.Player;

/**
 * Static accessor for a player's {@link IPlayerKnowledge} record.
 *
 * <p>The accessor is bound at mod init time by the implementation. Addons use {@link #of(Player)}
 * to read or mutate a player's record without depending on the internal attachment registry.
 *
 * <p>Since 1.1.0 both methods forward to {@link PlayerResearchAccess}, which holds the only binding.
 *
 * @since 1.0.0
 * @deprecated use {@link PlayerResearchAccess}; this class is removed in 2.0.0.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@SuppressWarnings("removal")
public final class KnowledgeAccess {
    private KnowledgeAccess() {}

    /**
     * Returns the player's knowledge record, creating an empty one when absent.
     *
     * @param player the player; may be a client or server player
     * @return the player's knowledge record, never null; since 1.1.0 a view of the player's {@link PlayerResearch}
     *     whose changes apply to that progress directly
     * @throws IllegalStateException when called before the implementation has bound the accessor
     * @deprecated use {@link PlayerResearchAccess#of}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static IPlayerKnowledge of(Player player) {
        return IPlayerKnowledge.viewOf(PlayerResearchAccess.of(player));
    }

    /**
     * Binds the accessor's implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param impl the accessor implementation
     * @apiNote This is a one-shot binding; calling it more than once throws.
     * @throws IllegalStateException when already bound
     * @deprecated Thaumaturge binds {@link PlayerResearchAccess#bind} instead; since 1.1.0 this forwards there
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void bind(Function<Player, IPlayerKnowledge> impl) {
        PlayerResearchAccess.bind(impl::apply);
    }
}
