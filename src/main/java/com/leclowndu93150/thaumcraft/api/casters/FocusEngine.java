package com.leclowndu93150.thaumcraft.api.casters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for resolving focus elements and executing focus packages.
 *
 * <p>Element registration moves from static maps to the {@link FocusElementType#REGISTRY_KEY}
 * registry, and the executor's shared damage-resistance list becomes per-invocation state.
 *
 * @apiNote {@link #bindRegistry(Registry)} is called exactly once by the mod during init.
 *          Addons never call it; they register {@link FocusElementType} values into the
 *          registry instead.
 * @since 1.0.0
 */
public final class FocusEngine {
    private static Registry<FocusElementType> registry;

    private FocusEngine() {}

    /**
     * Binds the focus element registry this engine resolves elements from.
     *
     * @param elementRegistry the built focus element registry
     */
    public static void bindRegistry(Registry<FocusElementType> elementRegistry) {
        registry = elementRegistry;
    }

    /**
     * Creates a fresh, initialized instance of a registered focus element.
     *
     * @param key the element id
     * @return a new element instance, or null when no element is registered under {@code key}
     */
    public static @Nullable IFocusElement getElement(Identifier key) {
        FocusElementType type = registry().getValue(key);
        if (type == null) {
            return null;
        }
        IFocusElement element = type.factory().get();
        if (element instanceof FocusNode node) {
            node.initialize();
        }
        return element;
    }

    /**
     * The icon registered for a focus element.
     *
     * @param key the element id
     * @return the icon texture, or null when no element is registered under {@code key}
     */
    public static @Nullable Identifier getElementIcon(Identifier key) {
        FocusElementType type = registry().getValue(key);
        return type != null ? type.icon() : null;
    }

    /**
     * The icon tint registered for a focus element.
     *
     * @param key the element id
     * @return the packed {@code 0xRRGGBB} color, or white when no element is registered
     *         under {@code key}
     */
    public static int getElementColor(Identifier key) {
        FocusElementType type = registry().getValue(key);
        return type != null ? type.color() : 0xFFFFFF;
    }

    /**
     * Whether a package directly contains an element, without descending into nested
     * packages or split branches.
     *
     * @param focusPackage the package to inspect
     * @param key          the element id to look for
     * @return true when a top-level node carries {@code key}
     */
    public static boolean doesPackageContainElement(FocusPackage focusPackage, Identifier key) {
        for (IFocusElement node : focusPackage.getNodes()) {
            if (node.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Casts a package: deep-copies it, binds it to the caster, assigns a fresh cast id,
     * fires {@link FocusEffect#onCast(Entity)} on every effect, then executes it.
     *
     * @param caster       the casting entity
     * @param focusPackage the package to cast; left untouched
     */
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage) {
        castFocusPackage(caster, focusPackage, false);
    }

    /**
     * Casts a package, optionally without copying it first.
     *
     * @param caster       the casting entity
     * @param focusPackage the package to cast
     * @param noCopy       true to execute {@code focusPackage} itself instead of a copy;
     *                     execution mutates the package
     */
    public static void castFocusPackage(LivingEntity caster, FocusPackage focusPackage, boolean noCopy) {
        FocusPackage active = noCopy ? focusPackage : focusPackage.copy(caster);
        active.initialize(caster);
        active.setUniqueID(UUID.randomUUID());
        for (FocusEffect effect : active.getFocusEffects()) {
            effect.onCast(caster);
        }
        runFocusPackage(active, null, null);
    }

    /**
     * Executes a package's nodes in order. A {@link FocusMediumRoot} is inserted up front
     * when absent, parents and owning packages are wired as execution advances, node power
     * multipliers fold into the package, mediums run once per incoming trajectory and halt
     * the loop when they defer to an intermediary, splits recurse into their branches, and
     * effects run once per incoming target. When several effects strike the same entity
     * during one cast, its invulnerability ticks are reset between strikes so every effect
     * lands.
     *
     * @param focusPackage the package to execute
     * @param trajectories the trajectories supplied to the first node, or null
     * @param targets      the targets supplied to the first node, or null
     */
    public static void runFocusPackage(FocusPackage focusPackage, Trajectory @Nullable [] trajectories, HitResult @Nullable [] targets) {
        runFocusPackage(focusPackage, trajectories, targets, new HashSet<>());
    }

    private static void runFocusPackage(FocusPackage focusPackage, Trajectory @Nullable [] trajectories, HitResult @Nullable [] targets, Set<String> struckThisCast) {
        Trajectory[] prevTrajectories = trajectories;
        HitResult[] prevTargets = targets;
        List<IFocusElement> nodes = focusPackage.getNodes();
        synchronized (nodes) {
            if (nodes.isEmpty()) {
                return;
            }
            if (!(nodes.get(0) instanceof FocusMediumRoot)) {
                nodes.add(0, new FocusMediumRoot(trajectories, targets));
            }
            for (int idx = 0; idx < nodes.size(); idx++) {
                focusPackage.setExecutionIndex(idx);
                IFocusElement element = nodes.get(idx);
                if (idx > 0 && element instanceof FocusNode node && node.getParent() == null
                        && nodes.get(idx - 1) instanceof FocusNode previous) {
                    node.setParent(previous);
                }
                if (element instanceof FocusNode node) {
                    if (node.getPackage() == null) {
                        node.setPackage(focusPackage);
                    }
                    focusPackage.multiplyPower(node.getPowerMultiplier());
                }
                if (element instanceof FocusPackage nested) {
                    runFocusPackage(nested, prevTrajectories, prevTargets, struckThisCast);
                    break;
                }
                if (element instanceof FocusMedium medium) {
                    if (prevTrajectories != null) {
                        for (Trajectory trajectory : prevTrajectories) {
                            medium.execute(trajectory);
                        }
                    }
                    if (medium.hasIntermediary()) {
                        break;
                    }
                } else if (element instanceof FocusMod mod) {
                    if (mod instanceof FocusModSplit split) {
                        for (FocusPackage branch : split.getSplitPackages()) {
                            split.setPackage(branch);
                            branch.multiplyPower(focusPackage.getPower());
                            split.execute();
                            runFocusPackage(branch, split.supplyTrajectories(), split.supplyTargets(), struckThisCast);
                        }
                        break;
                    }
                    mod.execute();
                } else if (element instanceof FocusEffect effect && prevTargets != null) {
                    int num = 0;
                    for (HitResult target : prevTargets) {
                        if (target instanceof EntityHitResult entityHit) {
                            Entity struck = entityHit.getEntity();
                            String strikeKey = struck.getId() + focusPackage.getUniqueID().toString();
                            if (!struckThisCast.add(strikeKey) && struck.invulnerableTime > 0) {
                                struck.invulnerableTime = 0;
                            }
                        }
                        Trajectory trajectory = prevTrajectories != null
                                ? (prevTrajectories.length == prevTargets.length ? prevTrajectories[num] : prevTrajectories[0])
                                : null;
                        effect.execute(target, trajectory, focusPackage.getPower(), num);
                        num++;
                    }
                }
                if (element instanceof FocusNode node) {
                    prevTrajectories = node.supplyTrajectories();
                    prevTargets = node.supplyTargets();
                }
            }
        }
    }

    private static Registry<FocusElementType> registry() {
        if (registry == null) {
            throw new IllegalStateException("FocusEngine used before Thaumcraft bound the focus element registry");
        }
        return registry;
    }
}
