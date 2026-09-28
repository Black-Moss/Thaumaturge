package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Convenience lookups for the sided essentia transport capability, and access to the aspect
 * filter that labels set on jars and other items.
 *
 * <p>The transport lookups wrap {@link EssentiaCapabilities#TRANSPORT} so that tooltip, JEI, and
 * integration code can ask "does this block expose essentia transport on this face" without
 * repeating the capability query. The {@code face} argument is the side of the queried block that
 * the caller is approaching from, matching the sided-capability convention.
 *
 * <p>The filter methods are bound at mod init by Thaumaturge via {@link #bind}; addons must not
 * call {@code bind}.
 *
 * @since 1.0.0
 */
public final class EssentiaAccess {
    private static Supplier<DataComponentType<ResourceKey<IAspect>>> filterComponent;

    private EssentiaAccess() {}

    /**
     * Returns the essentia transport exposed by the block at {@code pos} on the given face.
     *
     * @param level the level
     * @param pos   the block position
     * @param face  the side of the block being approached
     * @return the transport, or {@code null} when the block exposes none on that face
     */
    public static @Nullable IEssentiaTransport transport(Level level, BlockPos pos, Direction face) {
        return level.getCapability(EssentiaCapabilities.TRANSPORT, pos, face);
    }

    /**
     * Whether the block at {@code pos} exposes essentia transport on the given face.
     *
     * @param level the level
     * @param pos   the block position
     * @param face  the side of the block being approached
     * @return {@code true} when a transport capability is present on that face
     */
    public static boolean isEssentiaTransport(Level level, BlockPos pos, Direction face) {
        return transport(level, pos, face) != null;
    }

    /**
     * The aspect a stack is labelled for, such as a labelled jar. A filter only records intent: it
     * does not make an item an essentia container or allow transfers.
     *
     * @param stack the stack
     * @return the filter aspect, or null when the stack has none
     * @throws IllegalStateException when called before the implementation has bound the facade
     * @since 1.0.0
     */
    public static @Nullable ResourceKey<IAspect> aspectFilter(ItemStack stack) {
        return stack.get(filterComponent());
    }

    /**
     * Returns a copy of a stack with its aspect filter set or removed. The given stack is not
     * changed.
     *
     * @param stack  the stack
     * @param aspect the filter aspect, or null to remove the filter
     * @return the changed copy
     * @throws IllegalStateException when called before the implementation has bound the facade
     * @since 1.0.0
     */
    public static ItemStack withAspectFilter(ItemStack stack, @Nullable ResourceKey<IAspect> aspect) {
        ItemStack copy = stack.copy();
        if (aspect == null) {
            copy.remove(filterComponent());
        } else {
            copy.set(filterComponent(), aspect);
        }
        return copy;
    }

    /**
     * Binds the aspect filter component. Called once at mod init by Thaumaturge; addons must not
     * call this.
     *
     * @param component the component holding an item's aspect filter
     * @throws IllegalStateException when already bound
     * @since 1.0.0
     */
    public static void bind(Supplier<DataComponentType<ResourceKey<IAspect>>> component) {
        if (filterComponent != null) {
            throw new IllegalStateException("EssentiaAccess already bound");
        }
        filterComponent = component;
    }

    private static DataComponentType<ResourceKey<IAspect>> filterComponent() {
        if (filterComponent == null) {
            throw new IllegalStateException("EssentiaAccess accessed before binding");
        }
        return filterComponent.get();
    }
}
