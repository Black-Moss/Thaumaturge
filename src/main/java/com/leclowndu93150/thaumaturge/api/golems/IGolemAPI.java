package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * The surface a golem exposes to seal behaviours, tasks, part abilities and accessories.
 *
 * <p>Unless stated otherwise, mutating methods are server-side only and must be called on the server thread.
 *
 * @since 1.0.0
 */
public interface IGolemAPI {
    /**
     * The UUID of the golem's owner, available while the owner is offline.
     *
     * @return the owner's UUID, or empty when the golem has no owner
     * @since 1.0.0
     */
    Optional<UUID> ownerIdentity();

    /**
     * The state a worn accessory's behaviour currently holds on this golem. On the server this is
     * the authoritative state; on the client it is the last synced state and is only present for
     * behaviours with a {@link GolemAccessoryBehavior#syncCodec()}.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param <T>      the state type
     * @return the state, or empty when the golem does not wear an accessory with this behaviour
     * @since 1.0.0
     */
    <T> Optional<T> accessoryState(GolemAccessoryBehavior<T> behavior);

    /**
     * Replaces a worn accessory's state from outside its callbacks, for example from an addon's
     * interaction handler. Server side only; the new state is saved and, when the behaviour
     * syncs, sent to clients.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param transform maps the current state to the new one; must not return null
     * @param <T>      the state type
     * @return true when the golem wears an accessory with this behaviour and the update ran
     * @throws IllegalStateException when called on the client
     * @since 1.0.0
     */
    <T> boolean updateAccessoryState(GolemAccessoryBehavior<T> behavior, UnaryOperator<T> transform);

    /**
     * @return the level the golem lives in
     */
    Level level();

    /**
     * @return the golem as a living entity
     */
    LivingEntity asEntity();

    /**
     * Whether the golem currently has an attack target. The server answers from the golem's target; a client answers from
     * the state the server syncs, so renderers can read it.
     *
     * @return whether the golem is fighting
     * @implNote the default forwards to the deprecated {@link #isInCombat()} so implementations written against 1.0 keep
     *     working; an implementation overrides this method, and must override at least one of the two
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    default boolean isFighting() {
        return isInCombat();
    }

    /**
     * Swings the golem's main arm. Called on the server, the swing is sent to every player tracking the golem. A swing that
     * is already in its first half is not restarted.
     *
     * @implNote the default forwards to the deprecated {@link #swingArm()} so implementations written against 1.0 keep
     *     working; an implementation overrides this method, and must override at least one of the two
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    default void animateSwing() {
        swingArm();
    }

    /**
     * @return whether the golem currently has an attack target
     * @deprecated use {@link #isFighting()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default boolean isInCombat() {
        return isFighting();
    }

    /**
     * Plays the main-hand swing animation for every watcher.
     *
     * @deprecated use {@link #animateSwing()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default void swingArm() {
        animateSwing();
    }

    /**
     * @return the golem's carry slots
     */
    IGolemHands hands();

    /**
     * @return the golem's dye colour index, from 1 to 16, or 0 when it has none
     */
    byte color();

    /**
     * Awards rank experience. Server side; on a client the call does nothing.
     *
     * <p>Only golems with the smart trait keep it; for any other golem, and for a golem already at rank 10, the award is
     * ignored. Experience counts within the current rank. When it reaches the threshold for the next rank, which is 1000
     * times the square of that rank, the golem goes up one rank and the threshold is subtracted, so the surplus carries
     * over. At most one rank is gained per award. A rank up refreshes the golem's health and melee damage at once and, when
     * golem emotes are enabled, shows hearts and plays the level-up sound.
     *
     * @param amount the experience to add; zero or less is ignored
     * @implNote the default forwards to the deprecated {@link #addRankXp(int)} so implementations written against 1.0 keep
     *     working; an implementation overrides this method, and must override at least one of the two
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    default void grantRankExperience(int amount) {
        addRankXp(amount);
    }

    /**
     * Replaces the golem's build. Server side; on a client the call does nothing.
     *
     * <p>The build is synced to clients, and everything derived from it is recomputed as when the golem is placed: health,
     * armor, melee damage, step height, movement, home and follow ranges, and the golem's whole behaviour set. Whatever the
     * golem was doing is stopped. Rank experience is kept as it is.
     *
     * @param build the new build; its rank is clamped to 0 to 10
     * @implNote the default forwards to the deprecated {@link #setProperties(IGolemProperties)} so implementations written
     *     against 1.0 keep working; an implementation overrides this method, and must override at least one of the two
     * @since 1.1.0
     */
    @SuppressWarnings("removal")
    default void applyBuild(IGolemProperties build) {
        setProperties(build);
    }

    /**
     * Awards rank experience. Only golems with the smart trait accumulate it; ranking up plays a sound and an emote.
     *
     * @param amount the experience amount
     * @deprecated use {@link #grantRankExperience(int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default void addRankXp(int amount) {
        grantRankExperience(amount);
    }

    /**
     * Replaces the golem's build, syncs it to clients and refreshes attributes and goals derived from it.
     *
     * @param build the new build
     * @deprecated use {@link #applyBuild(IGolemProperties)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    default void setProperties(IGolemProperties build) {
        applyBuild(build);
    }

    /**
     * @return the golem's current build; an immutable value
     */
    IGolemProperties properties();
}
