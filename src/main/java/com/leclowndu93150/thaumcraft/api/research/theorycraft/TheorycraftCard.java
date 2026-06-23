package com.leclowndu93150.thaumcraft.api.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.IResearchCategory;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A theorycraft card. Cards are the unit of choice in the research table minigame; the player is
 * dealt a hand of cards each round and plays one to advance their category totals or alter the
 * game state.
 *
 * <p>Cards are registered to the {@code thaumcraft:theorycraft_card} registry. Each card
 * registration is a {@link CardFactory} that produces fresh instances; instances are stateful and
 * must not be shared across players or draws.
 *
 * <p>A card carries a {@link #seed() seed} that fixes any randomness needed during
 * {@link #initialize initialize} and {@link #activate activate}. The seed survives serialization
 * so a saved card re-rolls identically on reload.
 *
 * @since 1.0.0
 */
public abstract class TheorycraftCard {
    private long seed = -1L;

    /**
     * Stable seed used to drive deterministic random choices. Reading the seed before it has
     * been set assigns a value derived from {@link System#nanoTime()} so unseeded cards still
     * behave consistently across a single session.
     *
     * @return the seed, always non-negative
     */
    public final long seed() {
        if (seed < 0L) {
            seed = Math.abs(System.nanoTime());
        }
        return seed;
    }

    /**
     * Sets the seed. Called by the manager before {@link #initialize initialize}.
     *
     * @param newSeed the new seed; the absolute value is stored
     */
    public final void setSeed(long newSeed) {
        this.seed = Math.abs(newSeed);
    }

    /**
     * Prepares this card for the given session. May read {@code data} and the player's knowledge
     * to pick a category or compute a payload. Returning {@code false} rejects the draw and the
     * manager picks another card.
     *
     * @param player the player drawing the card; server-side
     * @param data the active table session
     * @return {@code true} when the card is valid for this draw
     */
    public boolean initialize(ServerPlayer player, IResearchTableData data) {
        return true;
    }

    /**
     * Whether this card can only appear when drawn from an aid pool. Aid-only cards are never
     * shuffled into the base deck.
     *
     * @return {@code true} when aid-exclusive
     */
    public boolean isAidOnly() {
        return false;
    }

    /**
     * Inspiration cost to play this card. The session's remaining inspiration must be at least
     * this value.
     *
     * @return the cost, positive
     */
    public abstract int inspirationCost();

    /**
     * Optional category this card targets. When non-null the card is only drawn when the
     * category is unlocked and unblocked in the current session. Cards that pick their category
     * during {@link #initialize initialize} may return null until then.
     *
     * @return the category key, or {@code null} when category-agnostic
     */
    public @Nullable ResourceKey<IResearchCategory> category() {
        return null;
    }

    /**
     * Display name of this card in the research table UI.
     *
     * @return the localized name
     */
    public abstract Component name();

    /**
     * Body text shown on the card face.
     *
     * @return the localized body text
     */
    public abstract Component description();

    /**
     * Items the player must hold to play this card. Each requirement may flag whether the item is
     * consumed on play. Cards with no item requirement return an empty list.
     *
     * @return the item requirements, never null
     */
    public List<CardItemRequirement> requiredItems() {
        return List.of();
    }

    /**
     * Executes this card's effect. Called server-side after the manager has verified the player
     * meets the inspiration and item requirements. Returning {@code false} reports the card was
     * unable to apply its effect; the manager refunds the inspiration cost.
     *
     * @param player the player playing the card; server-side
     * @param data the active table session
     * @return {@code true} when the effect applied
     */
    public abstract boolean activate(ServerPlayer player, IResearchTableData data);

    /**
     * Persists this card's per-instance state. Subclasses should call {@code super.write(output, registries)}
     * to store the seed before writing their own fields.
     *
     * @param output the value sink
     * @param registries the registry access for codec-based fields
     */
    public void write(ValueOutput output, HolderLookup.Provider registries) {
        output.putLong("seed", seed);
    }

    /**
     * Restores this card's per-instance state. Subclasses should call {@code super.read(input, registries)}
     * to load the seed before reading their own fields.
     *
     * @param input the value source
     * @param registries the registry access for codec-based fields
     */
    public void read(ValueInput input, HolderLookup.Provider registries) {
        input.getLong("seed").ifPresent(value -> seed = value);
    }

    /**
     * Per-item requirement entry. Records the matching item stack and whether playing the card
     * consumes that stack.
     *
     * @param stack the item stack required; quantity is the minimum the player must hold
     * @param consumed whether the stack is removed from the player's inventory on play
     * @since 1.0.0
     */
    public record CardItemRequirement(ItemStack stack, boolean consumed) {}
}
