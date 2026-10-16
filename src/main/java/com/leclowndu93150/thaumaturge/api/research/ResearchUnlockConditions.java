package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;

/**
 * Lets addons add extra requirements for unlocking a research entry.
 *
 * <p>Conditions are checked in {@link ResearchManager#unlock}, when siblings are auto-completed,
 * and during autounlock on login. Several can be registered per entry, all must pass.
 *
 * <p>Register during common setup, e.g. inside {@code enqueueWork} in {@code FMLCommonSetupEvent}.
 *
 * <pre>{@code
 * @SubscribeEvent
 * public static void onCommonSetup(FMLCommonSetupEvent event) {
 *     event.enqueueWork(() -> ResearchUnlockConditions.require(
 *             Identifier.fromNamespaceAndPath("thaumaturge_tinkerer", "some_entry"),
 *             Some condition...,
 *             Component.translatable("tooltip.thaumaturge_tinkerer.requires_all_research")
 *     ));
 * }
 * }</pre>
 *
 * <p>Since 1.1.0 requirements are {@link UnlockRequirement}s registered with {@link #require}. Conditions registered
 * through the deprecated {@link #register} are kept as requirements that hand the condition a 1.0.0 view of the
 * player's progress.
 */
public final class ResearchUnlockConditions {

    /**
     * @deprecated implement {@link UnlockRequirement} and register it with {@link #require}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    @FunctionalInterface
    public interface Condition {
        // Return true if the player is allowed to unlock the entry
        boolean test(Player player, IPlayerKnowledge knowledge);

        // Passes once the given research is completed
        static Condition requiresComplete(Identifier research) {
            return (player, knowledge) -> knowledge.isResearchComplete(research);
        }

        // Passes only if both this and the other condition pass
        default Condition and(Condition other) {
            return (player, knowledge) -> test(player, knowledge) && other.test(player, knowledge);
        }
    }

    private record Registered(UnlockRequirement requirement, @Nullable Component lockedMessage) {
    }

    private static final Map<Identifier, List<Registered>> CONDITIONS = new ConcurrentHashMap<>();

    private ResearchUnlockConditions() {}

    /**
     * Adds a requirement for unlocking a research entry. Every requirement registered for an entry must be met.
     *
     * @param entry the research entry id
     * @param requirement the requirement to add
     * @param lockedMessage the line shown in the Thaumonomicon while the requirement is not met, or {@code null} to
     *     show nothing
     * @throws NullPointerException when {@code entry} or {@code requirement} is {@code null}
     * @since 1.1.0
     */
    public static void require(Identifier entry, UnlockRequirement requirement, @Nullable Component lockedMessage) {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(requirement);
        CONDITIONS.computeIfAbsent(entry, id -> new CopyOnWriteArrayList<>()).add(new Registered(requirement, lockedMessage));
    }

    /**
     * @deprecated use {@link #require}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public static void register(Identifier entry, Condition condition, @Nullable Component lockedMessage) {
        Objects.requireNonNull(condition);
        require(entry, (player, research) -> condition.test(player, IPlayerKnowledge.viewOf(research)), lockedMessage);
    }

    /**
     * Tests whether a player meets every requirement registered for an entry.
     *
     * @param player the player, on either logical side
     * @param research the player's research progress
     * @param entry the research entry id
     * @return {@code true} when the entry has no requirements or the player meets all of them
     * @since 1.1.0
     */
    public static boolean passes(Player player, PlayerResearch research, Identifier entry) {
        List<Registered> conditions = CONDITIONS.get(entry);
        if (conditions == null) {
            return true;
        }
        for (Registered r : conditions) {
            if (!r.requirement().isMet(player, research)) {
                return false;
            }
        }
        return true;
    }

    // True if the entry has no conditions or all of them pass
    /**
     * @deprecated use {@link #passes(Player, PlayerResearch, Identifier)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public static boolean passes(Player player, IPlayerKnowledge knowledge, Identifier entry) {
        return passes(player, (PlayerResearch) knowledge, entry);
    }

    /**
     * Returns the locked messages of the requirements a player does not meet for an entry, in registration order.
     * Requirements registered without a message are left out.
     *
     * @param player the player, on either logical side
     * @param research the player's research progress
     * @param entry the research entry id
     * @return the messages, empty when every requirement is met or none has a message
     * @since 1.1.0
     */
    public static List<Component> lockedMessages(Player player, PlayerResearch research, Identifier entry) {
        List<Registered> conditions = CONDITIONS.get(entry);
        if (conditions == null) {
            return List.of();
        }
        List<Component> messages = new ArrayList<>();
        for (Registered r : conditions) {
            if (r.lockedMessage() != null && !r.requirement().isMet(player, research)) {
                messages.add(r.lockedMessage());
            }
        }
        return messages;
    }

    // Tooltip of the conditions that currently is not met
    /**
     * @deprecated use {@link #lockedMessages(Player, PlayerResearch, Identifier)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("removal")
    public static List<Component> lockedMessages(Player player, IPlayerKnowledge knowledge, Identifier entry) {
        return lockedMessages(player, (PlayerResearch) knowledge, entry);
    }
}
