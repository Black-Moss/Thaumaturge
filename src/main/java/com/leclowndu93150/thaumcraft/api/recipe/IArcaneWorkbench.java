package com.leclowndu93150.thaumcraft.api.recipe;

import com.leclowndu93150.thaumcraft.api.aspect.AspectList;

/**
 * Contract for the arcane workbench inventory, exposing the crystal payment currently loaded in
 * its crystal slots.
 *
 * <p>It now carries the one piece of state the workbench container can honestly answer: the aspects
 * available as loaded essentia crystals. Vis and discount are functions of the player and the block
 * entity rather than the container, so they live on {@link ArcaneCraftCost} instead.
 *
 * @since 1.0.0
 */
public interface IArcaneWorkbench {
    /**
     * The aspects currently available as essentia crystals loaded in the crystal slots, summed by
     * aspect. Each unit corresponds to one crystal that can pay part of a recipe's crystal cost.
     *
     * @return the available crystal aspects, or {@link AspectList#EMPTY} when no crystals are loaded
     */
    AspectList availableCrystals();
}
