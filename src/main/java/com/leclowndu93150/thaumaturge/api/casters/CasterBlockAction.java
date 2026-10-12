package com.leclowndu93150.thaumaturge.api.casters;

/**
 * Something that happens when a caster is used on a block registered with {@link CasterBlockActions}, such as turning
 * a structure into a device.
 *
 * <p>An action that serves several blocks tells them apart from {@link CasterBlockUse#state()}, or is registered once
 * per block as a separate instance.
 *
 * @implNote Thaumaturge's wands dispatch actions on both the logical client and the logical server when used on a
 * block, after targets that implement {@link IInteractWithCaster} and before any other caster behaviour for the click.
 * @since 1.1.0
 */
@FunctionalInterface
public interface CasterBlockAction {
    /**
     * Runs the action for one caster use.
     *
     * @param use the caster use
     * @return {@code true} when the action handled the click, which stops any further actions and caster behaviour for
     *     it; {@code false} to let the next matching action try. An action that returns {@code false} should leave
     *     the world unchanged, since later actions still run for the same click
     * @since 1.1.0
     */
    boolean handle(CasterBlockUse use);
}
