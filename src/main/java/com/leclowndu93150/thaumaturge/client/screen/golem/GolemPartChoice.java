package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

final class GolemPartChoice<T> {
    static final int SLOT_COUNT = 5;

    private static final Identifier UNKNOWN_ID = TTIds.rl("unknown");
    private static final int[] LAST_INDEX = new int[SLOT_COUNT];

    private final int slot;
    private final Registry<T> registry;
    private final List<T> candidates = new ArrayList<>();

    GolemPartChoice(int slot, Registry<T> registry, Function<T, List<Identifier>> research, @Nullable Player player) {
        this.slot = slot;
        this.registry = registry;
        if (player != null) {
            IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
            for (T value : registry) {
                if (research.apply(value).stream().allMatch(knowledge::isResearchComplete)) {
                    candidates.add(value);
                }
            }
        }
        LAST_INDEX[slot] = candidates.isEmpty() ? 0 : Mth.clamp(LAST_INDEX[slot], 0, candidates.size() - 1);
    }

    static <V> Identifier idOf(Registry<V> registry, V value) {
        return Objects.requireNonNullElse(registry.getKey(value), UNKNOWN_ID);
    }

    boolean isEmpty() {
        return candidates.isEmpty();
    }

    boolean hasAlternatives() {
        return candidates.size() > 1;
    }

    T selected() {
        return candidates.get(LAST_INDEX[slot]);
    }

    Identifier selectedId() {
        return idOf(registry, selected());
    }

    void step(int delta) {
        if (!candidates.isEmpty()) {
            LAST_INDEX[slot] = Math.floorMod(LAST_INDEX[slot] + delta, candidates.size());
        }
    }
}
