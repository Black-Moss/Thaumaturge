package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Lets addons contribute recipe-derived aspect strategies. Posted once during common setup to every
 * mod's event bus, after all mods are constructed. Contributors registered here run after the
 * built-in crucible, infusion, and crafting contributors, in registration order, every time the
 * aspect index is built.
 *
 * <p>Contributors must be deterministic and must not depend on world state, matching the
 * {@link IAspectRecipeContributor} contract.
 *
 * @since 1.0.0
 */
public final class RegisterAspectContributorsEvent extends Event implements IModBusEvent {
    private final List<IAspectRecipeContributor> contributors = new ArrayList<>();

    /**
     * Registers a contributor to run after the built-in strategies.
     *
     * @param contributor the contributor to add
     */
    public void register(IAspectRecipeContributor contributor) {
        contributors.add(contributor);
    }

    /**
     * Returns the contributors registered by this event, in registration order. Intended for the
     * implementation that fires the event; addons register through {@link #register}.
     *
     * @return an unmodifiable view of the registered contributors
     */
    public List<IAspectRecipeContributor> contributors() {
        return List.copyOf(contributors);
    }
}
