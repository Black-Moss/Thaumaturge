package com.leclowndu93150.thaumcraft.api.casters;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Registration entry for a focus element: how to create fresh instances plus the icon and
 * color used to draw the element in the focal manipulator.
 *
 * <p>Register instances under {@link #REGISTRY_KEY}; {@link FocusEngine} resolves elements from
 * that registry once it is bound.
 *
 * @param factory creates a fresh, unconfigured element instance per lookup; instances are
 *                mutable and never shared
 * @param icon    the texture drawn for this element in casting UIs
 * @param color   the packed {@code 0xRRGGBB} tint applied to the icon
 * @since 1.0.0
 */
public record FocusElementType(Supplier<? extends IFocusElement> factory, Identifier icon, int color) {
    /** The registry key for focus element types. */
    public static final ResourceKey<Registry<FocusElementType>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath("thaumcraft", "focus_element"));
}
