package com.leclowndu93150.thaumcraft.content.focus.mod;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.casters.FocusModSplit;
import com.leclowndu93150.thaumcraft.api.casters.Trajectory;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class FocusModSplitTrajectory extends FocusModSplit {
    private static final Identifier KEY = TCIds.rl("split_trajectory");

    private static final int COMPLEXITY = 5;

    @Override
    public Identifier getKey() {
        return KEY;
    }

    @Override
    public int getComplexity() {
        return COMPLEXITY;
    }

    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }

    @Override
    public Trajectory @Nullable [] supplyTrajectories() {
        return getParent().supplyTrajectories();
    }

    @Override
    public boolean execute() {
        return true;
    }
}
