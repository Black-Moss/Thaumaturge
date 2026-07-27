package com.leclowndu93150.thaumaturge.api.golems.accessory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Registry of {@link GolemAccessory} definitions. Accessories register during mod
 * construction or common setup and are looked up by id when golems load or render.
 *
 * <p>Registration is not thread safe; register from a single mod initialization path.
 *
 * @since 1.0.0
 */
public final class GolemAccessories {
    private static final Map<Identifier, GolemAccessory> REGISTRY = new LinkedHashMap<>();

    private GolemAccessories() {}

    /**
     * Registers an accessory definition.
     *
     * @param accessory the accessory to register
     * @return the registered accessory
     * @throws IllegalArgumentException if an accessory with the same id is already registered
     */
    public static GolemAccessory register(GolemAccessory accessory) {
        if (REGISTRY.putIfAbsent(accessory.id(), accessory) != null) {
            throw new IllegalArgumentException("Duplicate golem accessory " + accessory.id());
        }
        return accessory;
    }

    /**
     * Looks up an accessory by id.
     *
     * @param id the accessory id
     * @return the accessory, or null if none is registered under the id
     */
    public static @Nullable GolemAccessory get(Identifier id) {
        return REGISTRY.get(id);
    }

    /**
     * Returns all registered accessories in registration order.
     *
     * @return an unmodifiable view of the registered accessories
     */
    public static Collection<GolemAccessory> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
