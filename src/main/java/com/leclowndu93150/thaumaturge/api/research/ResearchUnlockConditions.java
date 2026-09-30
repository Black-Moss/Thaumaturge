package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
 *     event.enqueueWork(() -> ResearchUnlockConditions.register(
 *             Identifier.fromNamespaceAndPath("thaumaturge_tinkerer", "some_entry"),
 *             Some condition...,
 *             Component.translatable("tooltip.thaumaturge_tinkerer.requires_all_research")
 *     ));
 * }
 * }</pre>
 */
public final class ResearchUnlockConditions {

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

    private record Registered(Condition condition, @Nullable Component lockedMessage) {
    }

    private static final Map<Identifier, List<Registered>> CONDITIONS = new ConcurrentHashMap<>();

    private ResearchUnlockConditions() {}

    public static void register(Identifier entry, Condition condition, @Nullable Component lockedMessage) {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(condition);
        CONDITIONS.computeIfAbsent(entry, id -> new CopyOnWriteArrayList<>()).add(new Registered(condition, lockedMessage));
    }

    // True if the entry has no conditions or all of them pass
    public static boolean passes(Player player, IPlayerKnowledge knowledge, Identifier entry) {
        List<Registered> conditions = CONDITIONS.get(entry);
        if (conditions == null) {
            return true;
        }
        for (Registered r : conditions) {
            if (!r.condition().test(player, knowledge)) {
                return false;
            }
        }
        return true;
    }

    // Tooltip of the conditions that currently is not met
    public static List<Component> lockedMessages(Player player, IPlayerKnowledge knowledge, Identifier entry) {
        List<Registered> conditions = CONDITIONS.get(entry);
        if (conditions == null) {
            return List.of();
        }
        List<Component> messages = new ArrayList<>();
        for (Registered r : conditions) {
            if (r.lockedMessage() != null && !r.condition().test(player, knowledge)) {
                messages.add(r.lockedMessage());
            }
        }
        return messages;
    }
}
