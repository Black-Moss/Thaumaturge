package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Creates and reads Thaumaturge's essentia crystal item, the crystallised single-aspect item used
 * to pay arcane crafts and as infusion components.
 *
 * <p>Only the genuine crystal item counts: other items that carry aspects are reported as
 * {@link State#NOT_CRYSTAL}. The implementation is bound at mod init by Thaumaturge via
 * {@link #bind}; addons must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class EssentiaCrystalAccess {
    private static Supplier<? extends Item> crystalItem;
    private static Supplier<DataComponentType<AspectInstance>> aspectComponent;

    private EssentiaCrystalAccess() {}

    /**
     * Creates a stack of crystals of one aspect.
     *
     * @param aspect the aspect
     * @param count  the stack size, at most the crystal's maximum stack size
     * @return a new stack the caller owns, or {@link ItemStack#EMPTY} when {@code count} is not
     *         positive
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static ItemStack create(Holder<IAspect> aspect, int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item(), count);
        stack.set(component(), new AspectInstance(aspect, 1));
        return stack;
    }

    /**
     * Classifies a stack.
     *
     * @param stack the stack
     * @return whether it is a crystal, and whether its aspect is set and well formed
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static State state(ItemStack stack) {
        if (!stack.is(item())) {
            return State.NOT_CRYSTAL;
        }
        AspectInstance configured = stack.get(component());
        if (configured == null) {
            return State.UNCONFIGURED;
        }
        return configured.amount() == 1 ? State.CONFIGURED : State.MALFORMED;
    }

    /**
     * Whether a stack is a crystal with a well-formed aspect.
     *
     * @param stack the stack
     * @return true when {@link #state} is {@link State#CONFIGURED}
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static boolean isConfigured(ItemStack stack) {
        return state(stack) == State.CONFIGURED;
    }

    /**
     * The aspect of a crystal.
     *
     * @param stack the stack
     * @return the aspect, or null unless the stack is a crystal with a well-formed aspect
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static @Nullable Holder<IAspect> aspect(ItemStack stack) {
        return isConfigured(stack) ? stack.get(component()).aspect() : null;
    }

    /**
     * Binds the crystal item and its aspect component. Called once at mod init by Thaumaturge;
     * addons must not call this.
     *
     * @param item      the crystal item
     * @param component the component holding a crystal's aspect
     * @throws IllegalStateException when already bound
     */
    public static void bind(Supplier<? extends Item> item, Supplier<DataComponentType<AspectInstance>> component) {
        if (crystalItem != null) {
            throw new IllegalStateException("EssentiaCrystalAccess already bound");
        }
        crystalItem = item;
        aspectComponent = component;
    }

    private static Item item() {
        if (crystalItem == null) {
            throw new IllegalStateException("EssentiaCrystalAccess accessed before binding");
        }
        return crystalItem.get();
    }

    private static DataComponentType<AspectInstance> component() {
        if (aspectComponent == null) {
            throw new IllegalStateException("EssentiaCrystalAccess accessed before binding");
        }
        return aspectComponent.get();
    }

    /**
     * What kind of stack {@link #state} found.
     *
     * @since 1.0.0
     */
    public enum State {
        /** The stack is not the essentia crystal item. */
        NOT_CRYSTAL,
        /** The stack is a crystal with no aspect set. */
        UNCONFIGURED,
        /** The stack is a crystal whose aspect data is not a single unit of one aspect. */
        MALFORMED,
        /** The stack is a crystal with a well-formed aspect. */
        CONFIGURED
    }
}
