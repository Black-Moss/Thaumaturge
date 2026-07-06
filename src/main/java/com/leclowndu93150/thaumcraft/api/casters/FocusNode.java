package com.leclowndu93150.thaumcraft.api.casters;

import com.leclowndu93150.thaumcraft.api.aspect.IAspect;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * Base class for executable focus elements: mediums, effects, and modifiers. A node lives
 * inside a {@link FocusPackage}, knows the node before it, and may carry configurable
 * {@link NodeSetting settings}.
 *
 *
 * @since 1.0.0
 */
public abstract class FocusNode implements IFocusElement {
    FocusPackage pack;
    private FocusNode parent;
    private final Map<String, NodeSetting> settings = new HashMap<>();

    /**
     * Creates the node and builds its settings map from {@link #createSettings()}.
     */
    protected FocusNode() {
        initialize();
    }

    /**
     * The translation key for this node's display name.
     *
     * @return {@code focus.<namespace>.<path>.name}
     */
    public String getNameKey() {
        return "focus." + getKey().getNamespace() + "." + getKey().getPath() + ".name";
    }

    /**
     * The translation key for this node's descriptive text.
     *
     * @return {@code focus.<namespace>.<path>.text}
     */
    public String getDescriptionKey() {
        return "focus." + getKey().getNamespace() + "." + getKey().getPath() + ".text";
    }

    /**
     * The complexity this node adds to a focus, limiting what fits into one focus item.
     *
     * @return the complexity cost, zero or greater
     */
    public abstract int getComplexity();

    /**
     * The aspect associated with this node, shown in the focal manipulator.
     *
     * @return the aspect key, or null when the node has none
     */
    public abstract @Nullable ResourceKey<IAspect> getAspect();

    /**
     * What the previous node must supply for this node to function.
     *
     * @return the required supply types, or null when nothing is required
     */
    public abstract EnumSupplyType @Nullable [] mustBeSupplied();

    /**
     * What this node supplies to the node after it.
     *
     * @return the provided supply types, or null when nothing is supplied
     */
    public abstract EnumSupplyType @Nullable [] willSupply();

    /**
     * Whether this node supplies the given type to its successor.
     *
     * @param type the supply type to test
     * @return true when {@link #willSupply()} contains {@code type}
     */
    public boolean canSupply(EnumSupplyType type) {
        EnumSupplyType[] supplied = willSupply();
        if (supplied != null) {
            for (EnumSupplyType st : supplied) {
                if (st == type) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The targets this node produced during execution, handed to the next node.
     *
     * @return the produced targets, or null when none
     */
    public HitResult @Nullable [] supplyTargets() {
        return null;
    }

    /**
     * The trajectories this node produced during execution, handed to the next node.
     *
     * @return the produced trajectories, or null when none
     */
    public Trajectory @Nullable [] supplyTrajectories() {
        return null;
    }

    /**
     * Assigns the package this node executes within. Set by {@link FocusEngine} during
     * execution.
     *
     * @param pack the owning package
     */
    public final void setPackage(FocusPackage pack) {
        this.pack = pack;
    }

    /**
     * The package this node executes within.
     *
     * @return the owning package, or null before execution begins
     */
    public final FocusPackage getPackage() {
        return pack;
    }

    /**
     * Builds a sub-package of every node after this one in the owning package, sharing the
     * parent's cast id, level, power, and caster. Delayed mediums hand this package to the
     * engine when their intermediary resolves.
     *
     * @return the remaining package, or null when this node is last
     */
    public final @Nullable FocusPackage getRemainingPackage() {
        FocusPackage p = getPackage();
        List<IFocusElement> tail = p.getNodes().subList(p.getExecutionIndex() + 1, p.getNodes().size());
        if (tail.isEmpty()) {
            return null;
        }
        FocusPackage remaining = new FocusPackage();
        remaining.setUniqueID(p.getUniqueID());
        remaining.level = p.level;
        remaining.multiplyPower(p.getPower());
        for (IFocusElement element : tail) {
            remaining.addNode(element);
        }
        remaining.setCasterUUID(p.getCasterUUID());
        return remaining;
    }

    /**
     * The node executed before this one.
     *
     * @return the parent node, or null for the first node
     */
    public final FocusNode getParent() {
        return parent;
    }

    /**
     * The ids of every setting on this node.
     *
     * @return the setting keys; empty when the node has no settings
     */
    public final Set<String> getSettingList() {
        return settings.keySet();
    }

    /**
     * Looks up a setting by id.
     *
     * @param key the setting id
     * @return the setting, or null when this node has no such setting
     */
    public final @Nullable NodeSetting getSetting(String key) {
        return settings.get(key);
    }

    /**
     * The current real value of a setting.
     *
     * @param key the setting id
     * @return the setting's value, or 0 when this node has no such setting
     */
    public final int getSettingValue(String key) {
        NodeSetting setting = settings.get(key);
        return setting != null ? setting.getValue() : 0;
    }

    /**
     * Creates the settings this node exposes. Called during {@link #initialize()}.
     *
     * @return fresh settings, or null when the node has none
     */
    public NodeSetting @Nullable [] createSettings() {
        return null;
    }

    /**
     * Rebuilds the settings map from {@link #createSettings()}, resetting every setting to
     * its default.
     */
    public final void initialize() {
        NodeSetting[] created = createSettings();
        if (created != null) {
            for (NodeSetting setting : created) {
                settings.put(setting.getKey(), setting);
            }
        }
    }

    /**
     * Assigns the node executed before this one. Set by {@link FocusEngine} during execution.
     *
     * @param parent the preceding node
     */
    public void setParent(FocusNode parent) {
        this.parent = parent;
    }

    /**
     * The factor this node applies to the package's power when it executes.
     *
     * @return the power multiplier, 1.0 meaning unchanged
     */
    public float getPowerMultiplier() {
        return 1.0F;
    }

    /**
     * Whether only one node of this kind may exist in a focus.
     *
     * @return true when the node is exclusive
     */
    public boolean isExclusive() {
        return false;
    }

    /**
     * What a node can require from or provide to its neighbors.
     *
     * @since 1.0.0
     */
    public enum EnumSupplyType {
        /** A resolved hit on an entity or block. */
        TARGET,
        /** A ray still in flight. */
        TRAJECTORY
    }
}
