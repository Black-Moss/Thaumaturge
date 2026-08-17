package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * Static read and write access to the vis stored on a wand stack.
 *
 * <p>
 * Wand vis lives in the {@code thaumaturge:wand_vis} data component as a {@link WandVis} record. These helpers read and
 * replace that component without exposing the component type holder, which lives in the implementation. Amounts are in
 * centivis (one hundred per vis unit).
 *
 * <p>
 * Writes here store raw amounts; they do not apply the consumption discounts that the wand's caps and the player's gear
 * grant during casting or crafting. They are the low-level storage accessor, suited to relays, chargers, and inspection
 * tools.
 *
 * <p>
 * The component-type supplier is bound once at mod init by Thaumaturge via {@link #bind(Supplier)}; addons must not
 * call {@code bind}.
 *
 * @since 1.0.0
 */
public final class WandAccess {
    private static Supplier<DataComponentType<WandVis>> component;

    private WandAccess() {}

    /**
     * Returns the full vis storage of the wand.
     *
     * @param wand the wand stack
     * @return the stored vis, or {@link WandVis#EMPTY} when the stack has none
     */
    public static WandVis getAllVis(ItemStack wand) {
        return wand.getOrDefault(componentType(), WandVis.EMPTY);
    }

    /**
     * Returns the centivis of a single aspect stored on the wand.
     *
     * @param wand the wand stack
     * @param aspect the aspect key
     * @return the centivis amount, or zero when absent
     */
    public static int getVis(ItemStack wand, ResourceKey<IAspect> aspect) {
        return getAllVis(wand).amount(aspect);
    }

    /**
     * Returns a copy of the wand stack with the given aspect set to {@code centivis}. Setting a non-positive amount removes
     * the aspect entry. The input stack is not modified.
     *
     * @param wand the wand stack
     * @param aspect the aspect key
     * @param centivis the new centivis amount for the aspect
     * @return a copied stack carrying the updated vis component
     */
    public static ItemStack withVis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis) {
        ItemStack copy = wand.copy();
        copy.set(componentType(), getAllVis(wand).with(aspect, centivis));
        return copy;
    }

    /**
     * Binds the wand-vis component type. Called once at mod init by Thaumaturge; addons must not call this.
     *
     * @param impl a supplier of the registered component type
     * @throws IllegalStateException when already bound
     */
    public static void bind(Supplier<DataComponentType<WandVis>> impl) {
        if (component != null) {
            throw new IllegalStateException("WandAccess already bound");
        }
        component = impl;
    }

    private static DataComponentType<WandVis> componentType() {
        if (component == null) {
            throw new IllegalStateException("WandAccess accessed before binding");
        }
        return component.get();
    }
}
