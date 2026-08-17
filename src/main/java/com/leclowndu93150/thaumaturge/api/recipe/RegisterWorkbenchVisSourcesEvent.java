package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired on the mod event bus so addons can register {@link IWorkbenchVisSource}s that help pay for
 * arcane crafts. Sources are consulted in registration order.
 *
 * @since 1.0.0
 */
public final class RegisterWorkbenchVisSourcesEvent extends Event implements IModBusEvent {
    private final List<IWorkbenchVisSource> sources = new ArrayList<>();

    /**
     * Registers a vis source.
     *
     * @param source the source to add
     */
    public void register(IWorkbenchVisSource source) {
        sources.add(source);
    }

    /**
     * Returns the registered sources in registration order. Intended for the implementation that
     * fires the event; addons register through {@link #register}.
     *
     * @return an unmodifiable view of the registered sources
     */
    public List<IWorkbenchVisSource> sources() {
        return List.copyOf(sources);
    }
}
