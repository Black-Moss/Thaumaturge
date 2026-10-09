package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.mojang.serialization.Codec;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public enum CrownRule implements StringRepresentable {
    OPEN_AIR("open_air", true, true), REPLACEABLE("replaceable", false, false);

    public static final Codec<CrownRule> CODEC = StringRepresentable.fromEnum(CrownRule::values);

    private static final double HALF = 0.5;
    private static final float HALF_SINGLE = 0.5F;

    private final String serializedName;
    private final boolean ownLeavesOnly;
    private final boolean doublePrecision;

    CrownRule(String serializedName, boolean ownLeavesOnly, boolean doublePrecision) {
        this.serializedName = serializedName;
        this.ownLeavesOnly = ownLeavesOnly;
        this.doublePrecision = doublePrecision;
    }

    public static boolean canHostLog(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.canBeReplaced();
    }

    public boolean isOpen(BlockState state, Block ownLeaves) {
        if (ownLeavesOnly) {
            return state.isAir() || state.is(ownLeaves);
        }
        return canHostLog(state);
    }

    public boolean belowCrown(int layer, int crownFloor) {
        return layer < crownFloor;
    }

    public int clusterCoordinate(int base, double offset) {
        return round(base + offset);
    }

    public int lineCoordinate(int start, int delta, int step, int span, boolean exact) {
        if (exact) {
            return start + Integer.signum(delta) * step;
        }
        return start + round((double) delta * step / span);
    }

    private int round(double value) {
        if (doublePrecision) {
            return Mth.floor(value + HALF);
        }
        return Mth.floor((float) value + HALF_SINGLE);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
