package com.leclowndu93150.thaumaturge.api.items;

import java.util.Objects;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * Immutable rules for deciding whether a candidate stack counts as a reference stack. Counts never take part.
 *
 * <p>An empty stack matches only another empty stack. Otherwise a candidate matches when any of these holds:
 * <ul>
 * <li>{@code sameMod} is set and both items come from the same namespace;</li>
 * <li>{@code sharedTag} is set and the items are the same or the reference item shares at least one item tag with the
 * candidate;</li>
 * <li>the items are the same, the damage values agree unless {@code anyDamage} is set, and the data components agree under the
 * component rule.</li>
 * </ul>
 *
 * <p>The component rule: with {@code anyComponents} set, components are not compared. Otherwise, with {@code componentSubset} set,
 * every component on the reference must be present with an equal value on the candidate; without it the two component maps must be
 * equal. The damage component is left out of either comparison when {@code anyDamage} is set.
 *
 * <p>The tag and subset rules are directional: the reference is the tested side. Instances are safe to share between threads.
 *
 * @param anyDamage       whether differing damage values still match
 * @param anyComponents   whether differing data components still match
 * @param componentSubset whether the reference's components need only be a subset of the candidate's
 * @param sharedTag       whether items sharing an item tag match
 * @param sameMod         whether items from the same namespace match
 * @since 1.1.0
 */
public record ItemMatch(boolean anyDamage, boolean anyComponents, boolean componentSubset, boolean sharedTag, boolean sameMod) {
    /** Same item, same damage, equal components. */
    public static final ItemMatch EXACT = new ItemMatch(false, false, false, false, false);

    /** {@link #EXACT}, widened to any item that is the same item or shares an item tag with the reference. */
    public static final ItemMatch ITEM_OR_TAG = EXACT.byTag(true);

    /**
     * @param ignore whether differing damage values still match
     * @return a copy with the damage rule replaced
     */
    public ItemMatch ignoringDamage(boolean ignore) {
        return new ItemMatch(ignore, anyComponents, componentSubset, sharedTag, sameMod);
    }

    /**
     * @param ignore whether differing data components still match
     * @return a copy with the component rule replaced
     */
    public ItemMatch ignoringComponents(boolean ignore) {
        return new ItemMatch(anyDamage, ignore, componentSubset, sharedTag, sameMod);
    }

    /**
     * @param subset whether the reference's components need only be a subset of the candidate's
     * @return a copy with the subset rule replaced
     */
    public ItemMatch withComponentSubset(boolean subset) {
        return new ItemMatch(anyDamage, anyComponents, subset, sharedTag, sameMod);
    }

    /**
     * @param enabled whether items sharing an item tag match
     * @return a copy with the tag rule replaced
     */
    public ItemMatch byTag(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, enabled, sameMod);
    }

    /**
     * @param enabled whether items from the same namespace match
     * @return a copy with the namespace rule replaced
     */
    public ItemMatch byMod(boolean enabled) {
        return new ItemMatch(anyDamage, anyComponents, componentSubset, sharedTag, enabled);
    }

    /**
     * Tests a candidate against a reference. Neither stack is modified.
     *
     * @param reference the stack being looked for; the tested side of the tag and subset rules
     * @param candidate the stack being judged
     * @return whether the candidate counts as the reference under these rules
     */
    public boolean test(ItemStack reference, ItemStack candidate) {
        if (reference.isEmpty() || candidate.isEmpty()) {
            return reference.isEmpty() && candidate.isEmpty();
        }
        if (sameMod && namespace(reference).equals(namespace(candidate))) {
            return true;
        }
        if (sharedTag && sameItemOrTag(reference, candidate)) {
            return true;
        }
        if (reference.getItem() != candidate.getItem()) {
            return false;
        }
        if (!anyDamage && reference.getDamageValue() != candidate.getDamageValue()) {
            return false;
        }
        if (anyComponents) {
            return true;
        }
        if (componentSubset) {
            return contained(reference.getComponents(), candidate.getComponents());
        }
        if (!anyDamage) {
            return ItemStack.isSameItemSameComponents(reference, candidate);
        }
        return contained(reference.getComponents(), candidate.getComponents()) && contained(candidate.getComponents(), reference.getComponents());
    }

    private boolean contained(DataComponentMap subset, DataComponentMap superset) {
        for (TypedDataComponent<?> component : subset) {
            if (anyDamage && component.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!Objects.equals(component.value(), superset.get(component.type()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameItemOrTag(ItemStack reference, ItemStack candidate) {
        return reference.getItem() == candidate.getItem() || reference.typeHolder().tags().anyMatch(candidate::is);
    }

    private static String namespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }
}
