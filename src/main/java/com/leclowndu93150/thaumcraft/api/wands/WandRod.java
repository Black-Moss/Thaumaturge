package com.leclowndu93150.thaumcraft.api.wands;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * A wand rod (or staff core) type. Rods determine how much primal vis a wand can store, and
 * optionally self-charge through an {@link IWandRodOnUpdate} callback. Staff cores are rods
 * flagged as staves: they hold more vis and cast, but cannot be used for arcane crafting.
 *
 *
 * @since 1.0.0
 */
public final class WandRod {
    /** The registry key for wand rods. */
    public static final ResourceKey<Registry<WandRod>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath("thaumcraft", "wand_rod"));

    private final int capacity;
    private final int craftCost;
    private final Identifier texture;
    private final @Nullable IWandRodOnUpdate onUpdate;
    private final boolean glow;
    private final boolean staff;
    private final boolean runes;

    /**
     * @param capacity  vis stored per primal pool, in whole vis
     * @param craftCost the crafting cost factor of this rod, multiplied with the cap's factor
     *                  to price wand assembly
     * @param texture   the texture rendered on wand models built with this rod
     * @param onUpdate  inventory tick callback, or null for none
     * @param glow      whether wand models built with this rod render fullbright
     * @param staff     whether this rod is a staff core
     * @param runes     whether this rod bears runes, granting a free potency level to any
     *                  socketed focus
     */
    public WandRod(int capacity, int craftCost, Identifier texture, @Nullable IWandRodOnUpdate onUpdate,
                   boolean glow, boolean staff, boolean runes) {
        this.capacity = capacity;
        this.craftCost = craftCost;
        this.texture = texture;
        this.onUpdate = onUpdate;
        this.glow = glow;
        this.staff = staff;
        this.runes = runes;
    }

    /**
     * @return vis stored per primal pool, in whole vis
     */
    public int capacity() {
        return capacity;
    }

    /**
     * @return the crafting cost factor used to price wand assembly recipes
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * @return the texture rendered on wand models built with this rod
     */
    public Identifier texture() {
        return texture;
    }

    /**
     * @return the inventory tick callback, or null when this rod does not self-charge
     */
    public @Nullable IWandRodOnUpdate onUpdate() {
        return onUpdate;
    }

    /**
     * @return whether wand models built with this rod render fullbright
     */
    public boolean glow() {
        return glow;
    }

    /**
     * @return whether this rod is a staff core
     */
    public boolean staff() {
        return staff;
    }

    /**
     * @return whether this rod bears runes
     */
    public boolean runes() {
        return runes;
    }
}
