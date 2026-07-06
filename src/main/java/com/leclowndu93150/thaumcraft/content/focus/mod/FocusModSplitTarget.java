package com.leclowndu93150.thaumcraft.content.focus.mod;

import com.leclowndu93150.thaumcraft.TCIds;
import com.leclowndu93150.thaumcraft.api.casters.FocusModSplit;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class FocusModSplitTarget extends FocusModSplit {
    private static final Identifier KEY = TCIds.rl("split_target");

    private static final int COMPLEXITY = 4;

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
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }

    @Override
    public HitResult @Nullable [] supplyTargets() {
        return getParent().supplyTargets();
    }

    @Override
    public boolean execute() {
        return true;
    }
}
