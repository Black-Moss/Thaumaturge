package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Static facade over the world's effective aspect index. This is the addon-facing way to read the
 * aspect composition of any item; addons must not reach into the implementation package.
 *
 * <p>The backing {@link IAspectIndex} is rebuilt on every server resource reload and synced to
 * clients, so both sides may call {@link #of(Item)} and {@link #of(ItemStack)} directly. Reads
 * before the first index build return {@link AspectList#EMPTY} rather than throwing, because the
 * empty index is bound as the initial value.
 *
 * <p>The implementation is bound once at mod init by Thaumaturge via {@link #bind(Supplier)}; addons
 * must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class AspectIndexAccess {
    private static Supplier<IAspectIndex> binding;

    private AspectIndexAccess() {}

    /**
     * Returns the current aspect index.
     *
     * @return the live index; never null once bound
     * @throws IllegalStateException when accessed before the implementation has bound the facade
     */
    public static IAspectIndex index() {
        if (binding == null) {
            throw new IllegalStateException("AspectIndexAccess accessed before binding");
        }
        return binding.get();
    }

    /**
     * Looks up the aspect composition of the given item type.
     *
     * @param item the item to query
     * @return the aspect list, or {@link AspectList#EMPTY} when the item is unknown
     */
    public static AspectList of(Item item) {
        return index().of(item);
    }

    /**
     * Looks up the effective aspect composition of the given stack, honoring per-stack component
     * overrides.
     *
     * @param stack the stack to query
     * @return the aspect list, or {@link AspectList#EMPTY} when none
     */
    public static AspectList of(ItemStack stack) {
        return index().of(stack);
    }

    /**
     * Binds the facade's implementation. Called once at mod init by Thaumaturge; addons must not
     * call this.
     *
     * @param impl a supplier of the live index; must return the current index on every call
     * @throws IllegalStateException when already bound
     */
    public static void bind(Supplier<IAspectIndex> impl) {
        if (binding != null) {
            throw new IllegalStateException("AspectIndexAccess already bound");
        }
        binding = impl;
    }
}
