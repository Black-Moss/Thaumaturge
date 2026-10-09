package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Immutable description of a wand rod or staff core type. A rod sets the vis capacity per primal
 * pool, the cost factor used to price assembly recipes, the model texture and optional behaviour hooks.
 *
 * <p>Instances carry no identifier. The id is the key under which the instance is registered in the
 * registry identified by {@link #REGISTRY_KEY}. Equality is identity. All state is final, so
 * instances are safe to read from any thread. The tick callback, assembly callback and storage override
 * are invoked on the server thread, and the storage override may also be read from client code.
 *
 * @since 1.0.0
 */
public final class WandRod {
    /**
     * Key of the rod registry. The registry is not synchronised to clients, so both sides must hold
     * identical content.
     *
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<WandRod>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("wand_rod"));

    private final int capacity;
    private final int craftCost;
    private final Identifier texture;
    private final Identifier assemblyResearch;
    private final Flags flags;
    private final Hooks hooks;

    /**
     * Creates a rod without assembly research, storage override or assembly callback.
     *
     * @param capacity  vis capacity per primal pool, in whole vis
     * @param craftCost cost factor used to price assembly recipes
     * @param texture   model texture identifier
     * @param onUpdate  inventory tick callback, null for none
     * @param glow      whether the rod model renders fullbright
     * @param staff     whether the rod is a staff core
     * @param runes     whether the rod bears runes
     * @since 1.0.0
     */
    public WandRod(int capacity, int craftCost, Identifier texture, IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes) {
        this(capacity, craftCost, texture, onUpdate, glow, staff, runes, null);
    }

    /**
     * Creates a rod with an assembly research gate.
     *
     * @param capacity         vis capacity per primal pool, in whole vis
     * @param craftCost        cost factor used to price assembly recipes
     * @param texture          model texture identifier
     * @param onUpdate         inventory tick callback, null for none
     * @param glow             whether the rod model renders fullbright
     * @param staff            whether the rod is a staff core
     * @param runes            whether the rod bears runes
     * @param assemblyResearch research required to assemble, null for the base auromancy research
     * @since 1.0.0
     */
    public WandRod(int capacity, int craftCost, Identifier texture, IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes, Identifier assemblyResearch) {
        this(capacity, craftCost, texture, onUpdate, glow, staff, runes, assemblyResearch, null);
    }

    /**
     * Creates a rod with an assembly research gate and a vis storage override.
     *
     * @param capacity         vis capacity per primal pool, in whole vis
     * @param craftCost        cost factor used to price assembly recipes
     * @param texture          model texture identifier
     * @param onUpdate         inventory tick callback, null for none
     * @param glow             whether the rod model renders fullbright
     * @param staff            whether the rod is a staff core
     * @param runes            whether the rod bears runes
     * @param assemblyResearch research required to assemble, null for the base auromancy research
     * @param visStorage       vis storage override, null to use the wand vis data component
     * @since 1.0.0
     */
    public WandRod(int capacity, int craftCost, Identifier texture, IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes, Identifier assemblyResearch, IWandVisStorage visStorage) {
        this(capacity, craftCost, texture, onUpdate, glow, staff, runes, assemblyResearch, visStorage, null);
    }

    /**
     * Creates a rod with every option.
     *
     * @param capacity         vis capacity per primal pool, in whole vis
     * @param craftCost        cost factor used to price assembly recipes
     * @param texture          model texture identifier
     * @param onUpdate         inventory tick callback, null for none
     * @param glow             whether the rod model renders fullbright
     * @param staff            whether the rod is a staff core
     * @param runes            whether the rod bears runes
     * @param assemblyResearch research required to assemble, null for the base auromancy research
     * @param visStorage       vis storage override, null to use the wand vis data component
     * @param onAssemble       callback run on the freshly assembled wand, null for none
     * @since 1.0.0
     */
    public WandRod(int capacity, int craftCost, Identifier texture, IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes, Identifier assemblyResearch, IWandVisStorage visStorage, IWandRodOnAssemble onAssemble) {
        this.capacity = capacity;
        this.craftCost = craftCost;
        this.texture = texture;
        this.assemblyResearch = assemblyResearch;
        this.flags = new Flags(glow, staff, runes);
        this.hooks = new Hooks(onUpdate, onAssemble, visStorage);
    }

    /**
     * Returns the vis storage override.
     *
     * @return the override, or null when the wand vis data component is used
     * @since 1.0.0
     */
    public IWandVisStorage visStorage() {
        return hooks.visStorage;
    }

    /**
     * Returns the callback run once on a freshly assembled wand.
     *
     * @return the callback, or null for none
     * @since 1.0.0
     */
    public IWandRodOnAssemble onAssemble() {
        return hooks.onAssemble;
    }

    /**
     * Returns the research that gates assembly of wands with this rod.
     *
     * @return the research id, or null to fall back to the base auromancy research
     * @since 1.0.0
     */
    public Identifier assemblyResearch() {
        return assemblyResearch;
    }

    /**
     * Returns the vis capacity per primal pool.
     *
     * @return the capacity in whole vis
     * @since 1.0.0
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Returns the cost factor that is multiplied with the cap factor to price assembly recipes.
     *
     * @return the craft cost factor
     * @since 1.0.0
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * Returns the model texture.
     *
     * @return the texture identifier, never null
     * @since 1.0.0
     */
    public Identifier texture() {
        return texture;
    }

    /**
     * Returns the inventory tick callback.
     *
     * @return the callback, or null when the rod does not self-charge
     * @since 1.0.0
     */
    public IWandRodOnUpdate onUpdate() {
        return hooks.onUpdate;
    }

    /**
     * Returns whether the rod model renders at a pulsing fullbright light.
     *
     * @return true when the rod glows
     * @since 1.0.0
     */
    public boolean glow() {
        return flags.glow;
    }

    /**
     * Returns whether the rod is a staff core.
     *
     * @return true for staves
     * @since 1.0.0
     */
    public boolean staff() {
        return flags.staff;
    }

    /**
     * Returns whether the rod bears runes.
     *
     * @return true when runes are drawn and granted
     * @since 1.0.0
     */
    public boolean runes() {
        return flags.runes;
    }

    private record Flags(boolean glow, boolean staff, boolean runes) {
    }

    private record Hooks(IWandRodOnUpdate onUpdate, IWandRodOnAssemble onAssemble, IWandVisStorage visStorage) {
    }
}
