package com.leclowndu93150.thaumcraft.api.casters;

import java.util.ArrayList;
import java.util.List;

/**
 * A modifier that forks the spell into independent branches. Each branch is a
 * {@link FocusPackage} the engine executes in order, feeding it this node's supplied
 * trajectories and targets.
 *
 * <p>Branch serialization lives in {@link FocusPackage#CODEC}.
 *
 * @since 1.0.0
 */
public abstract class FocusModSplit extends FocusMod {
    private final List<FocusPackage> packages = new ArrayList<>();

    /**
     * The branch packages this split executes. The returned list is live; mutate it to
     * configure the branches.
     *
     * @return the mutable branch list, never null
     */
    public final List<FocusPackage> getSplitPackages() {
        return packages;
    }

    @Override
    public float getPowerMultiplier() {
        return 0.75F;
    }
}
