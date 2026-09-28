package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Lets addons register {@link IWorkbenchAuraSource}s that pay the aura part of crafts at hosts
 * other than a Thaumaturge arcane workbench. Posted once during common setup to every mod's event
 * bus, after all mods are constructed. Sources are consulted in registration order.
 *
 * @since 1.0.0
 */
public final class RegisterWorkbenchAuraSourcesEvent extends Event implements IModBusEvent {
    private final List<IWorkbenchAuraSource> sources = new ArrayList<>();

    /**
     * Registers an aura source.
     *
     * @param source the source to add
     */
    public void register(IWorkbenchAuraSource source) {
        sources.add(source);
    }

    /**
     * Returns the registered sources in registration order. Intended for the implementation that
     * posts the event; addons register through {@link #register}.
     *
     * @return an unmodifiable copy of the registered sources
     */
    public List<IWorkbenchAuraSource> sources() {
        return List.copyOf(sources);
    }
}
