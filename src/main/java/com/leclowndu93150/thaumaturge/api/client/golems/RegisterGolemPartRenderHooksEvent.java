package com.leclowndu93150.thaumaturge.api.client.golems;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Lets addons attach a {@link GolemPartRenderHook} to the mesh of a golem part model. Posted once to
 * every mod's event bus during client setup, on the main thread. Client only.
 *
 * <p>A hook is attached to a mesh location, the value a part model returns from
 * {@link GolemPartModel#objModel()}, and applies to every part model that uses that mesh. Each mesh
 * takes at most one hook. Thaumaturge attaches the hooks of its own parts through this event.
 *
 * @since 1.1.0
 */
public final class RegisterGolemPartRenderHooksEvent extends Event implements IModBusEvent {
    private final Map<Identifier, GolemPartRenderHook> hooks = new LinkedHashMap<>();

    /**
     * Attaches a hook to a mesh.
     *
     * @param mesh the location of the mesh, as given to the part model
     * @param hook the hook that animates the mesh
     * @throws NullPointerException     when an argument is null
     * @throws IllegalArgumentException when the mesh already has a hook
     * @since 1.1.0
     */
    public void register(Identifier mesh, GolemPartRenderHook hook) {
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(hook, "hook");
        if (hooks.putIfAbsent(mesh, hook) != null) {
            throw new IllegalArgumentException("Golem part mesh " + mesh + " already has a render hook");
        }
    }

    /**
     * Returns the hooks registered by this event. Intended for the implementation that posts the
     * event; addons register through {@link #register}.
     *
     * @return an unmodifiable copy of the hooks, keyed by mesh location
     * @since 1.1.0
     */
    public Map<Identifier, GolemPartRenderHook> hooks() {
        return Map.copyOf(hooks);
    }
}
