package com.leclowndu93150.thaumaturge.api.recipe;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge game event bus after the workbench planner computes an {@link ArcaneCraftCost} and before
 * payment is taken. Listeners may replace the cost with {@link #setCost(ArcaneCraftCost)} to make a craft cheaper,
 * free, or more expensive, or to change where it is paid from.
 *
 * <p>
 * The event fires with the same context used to plan and to pay, so a replaced cost is honored by the subsequent
 * payment. Listeners that redirect payment to an external store are responsible for draining that store themselves; for
 * coordinated, simulate-aware payment prefer registering an {@link IWorkbenchVisSource} instead.
 *
 * @since 1.0.0
 */
public final class ArcaneCraftCostEvent extends Event {
    private final IArcaneRecipe recipe;
    private final IArcaneWorkbench workbench;
    private final Player player;
    private ArcaneCraftCost cost;

    /**
     * Constructs the event. Fired by the implementation; addons receive it, they do not build it.
     *
     * @param recipe the recipe being crafted
     * @param workbench the workbench inventory
     * @param player the crafting player
     * @param cost the planner's computed cost
     */
    public ArcaneCraftCostEvent(IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player, ArcaneCraftCost cost) {
        this.recipe = recipe;
        this.workbench = workbench;
        this.player = player;
        this.cost = cost;
    }

    /**
     * The recipe being crafted.
     *
     * @return the recipe
     */
    public IArcaneRecipe getRecipe() {
        return recipe;
    }

    /**
     * The workbench inventory holding crystals and the wand.
     *
     * @return the workbench
     */
    public IArcaneWorkbench getWorkbench() {
        return workbench;
    }

    /**
     * The crafting player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The current cost, reflecting any prior listener's replacement.
     *
     * @return the current cost
     */
    public ArcaneCraftCost getCost() {
        return cost;
    }

    /**
     * Replaces the cost that payment will use.
     *
     * @param cost the new cost, never null
     */
    public void setCost(ArcaneCraftCost cost) {
        this.cost = cost;
    }
}
