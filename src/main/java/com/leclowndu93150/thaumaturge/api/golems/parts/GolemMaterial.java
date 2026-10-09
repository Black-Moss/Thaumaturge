package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * A material golems are built from. Materials live in the {@link #REGISTRY_KEY} registry, so addons may contribute their own.
 *
 * <p>Instances are immutable. The registry is server data and is not synchronised to clients, so each side registers the
 * same entries from the same code. The crafting item suppliers run on demand, never during registration.
 *
 * @since 1.0.0
 */
public final class GolemMaterial {
    /** The registry key of golem materials. */
    public static final ResourceKey<Registry<GolemMaterial>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("golem_material"));

    private static final String NAME_KEY_PREFIX = "golem.material.";
    private static final String DESCRIPTION_KEY_PREFIX = "golem.material.text.";
    private static final char KEY_SEPARATOR = '.';

    private final Appearance appearance;
    private final Combat combat;
    private final Components components;
    private final List<Identifier> research;
    private final List<Holder<GolemTrait>> traits;

    /**
     * Creates a material without a head antenna.
     *
     * @param research  the research entries that unlock the material; empty when it is always available
     * @param texture   the body texture
     * @param itemColor the 24-bit RGB tint of golem placer items
     * @param healthMod the health points added to the golem's base health
     * @param armor     the armor rating
     * @param damage    the base melee damage
     * @param base      supplies the base crafting item
     * @param mechanism supplies the mechanism crafting item
     * @param traits    the traits the material grants
     */
    public GolemMaterial(List<Identifier> research, Identifier texture, int itemColor, int healthMod, int armor, int damage, Supplier<ItemStack> base, Supplier<ItemStack> mechanism, List<Holder<GolemTrait>> traits) {
        this(research, texture, itemColor, healthMod, armor, damage, base, mechanism, traits, false);
    }

    /**
     * @param research  the research entries that unlock the material; empty when it is always available
     * @param texture   the body texture
     * @param itemColor the 24-bit RGB tint of golem placer items
     * @param healthMod the health points added to the golem's base health
     * @param armor     the armor rating
     * @param damage    the base melee damage
     * @param base      supplies the base crafting item
     * @param mechanism supplies the mechanism crafting item
     * @param traits    the traits the material grants
     * @param antenna   whether the copper golem head antenna is drawn
     */
    public GolemMaterial(List<Identifier> research, Identifier texture, int itemColor, int healthMod, int armor, int damage, Supplier<ItemStack> base, Supplier<ItemStack> mechanism, List<Holder<GolemTrait>> traits, boolean antenna) {
        this.appearance = new Appearance(texture, itemColor, antenna);
        this.combat = new Combat(healthMod, armor, damage);
        this.components = new Components(base, mechanism);
        this.traits = List.copyOf(traits);
        this.research = List.copyOf(research);
    }

    /**
     * @return the research entries that unlock the material, or an empty list when it is always available
     */
    public List<Identifier> research() {
        return research;
    }

    /**
     * @return the body texture
     */
    public Identifier texture() {
        return appearance.texture();
    }

    /**
     * @return the 24-bit RGB tint of golem placer items
     */
    public int itemColor() {
        return appearance.itemColor();
    }

    /**
     * @return the health points added to the golem's base health
     */
    public int healthMod() {
        return combat.healthMod();
    }

    /**
     * @return the armor rating
     */
    public int armor() {
        return combat.armor();
    }

    /**
     * @return the base melee damage
     */
    public int damage() {
        return combat.damage();
    }

    /**
     * @return a new copy of the base crafting item
     */
    public ItemStack base() {
        return components.base().get().copy();
    }

    /**
     * @return a new copy of the mechanism crafting item
     */
    public ItemStack mechanism() {
        return components.mechanism().get().copy();
    }

    /**
     * @return the traits the material grants; unmodifiable
     */
    public List<Holder<GolemTrait>> traits() {
        return traits;
    }

    /**
     * @return whether the copper golem head antenna is drawn; a hat accessory hides it regardless
     */
    public boolean antenna() {
        return appearance.antenna();
    }

    /**
     * @param id the id of a material
     * @return the lang key of the material's display name
     */
    public static String nameKey(Identifier id) {
        return keyFor(NAME_KEY_PREFIX, id);
    }

    /**
     * @param id the id of a material
     * @return the lang key of the material's descriptive text
     */
    public static String descriptionKey(Identifier id) {
        return keyFor(DESCRIPTION_KEY_PREFIX, id);
    }

    private static String keyFor(String prefix, Identifier id) {
        return new StringBuilder(prefix).append(id.getNamespace()).append(KEY_SEPARATOR).append(id.getPath()).toString();
    }

    private record Appearance(Identifier texture, int itemColor, boolean antenna) {
    }

    private record Combat(int healthMod, int armor, int damage) {
    }

    private record Components(Supplier<ItemStack> base, Supplier<ItemStack> mechanism) {
    }
}
