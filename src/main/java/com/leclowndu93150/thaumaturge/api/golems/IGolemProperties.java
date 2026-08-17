package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

/**
 * The composition of a golem: its material, four parts and experience rank. Trait queries
 * resolve the union of all part traits with opposing pairs cancelling each other.
 *
 * @since 1.0.0
 */
public interface IGolemProperties {
    /**
     * @return the effective trait set after opposite cancellation
     */
    Set<GolemTrait> getTraits();

    /**
     * @param trait the trait to test
     * @return whether the effective trait set contains it
     */
    boolean hasTrait(GolemTrait trait);

    /**
     * @return the crafting components consumed to assemble this golem, with equal stacks merged
     */
    List<ItemStack> generateComponents();

    void setMaterial(GolemMaterial material);

    GolemMaterial getMaterial();

    void setHead(GolemHead head);

    GolemHead getHead();

    void setArms(GolemArm arms);

    GolemArm getArms();

    void setLegs(GolemLeg legs);

    GolemLeg getLegs();

    void setAddon(GolemAddon addon);

    GolemAddon getAddon();

    void setRank(int rank);

    int getRank();
}
