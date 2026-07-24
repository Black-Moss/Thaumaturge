package com.leclowndu93150.thaumcraft.content.focus.mod;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.casters.CastContext;
import com.leclowndu93150.thaumcraft.api.casters.CastStreams;
import com.leclowndu93150.thaumcraft.api.casters.FocusSettings;
import com.leclowndu93150.thaumcraft.api.casters.FocusSplit;
import com.leclowndu93150.thaumcraft.api.recipe.ResearchGate;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

public final class FocusModSplitTrajectory implements FocusSplit {
    private static final Identifier KEY = TCIds.rl("split_trajectory");

    private static final int COMPLEXITY = 5;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_split"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_TRAJECTORIES;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_TRAJECTORIES;
    }

    @Override
    public CastStreams branchStreams(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        return new CastStreams(incoming.trajectories(), null);
    }
}
