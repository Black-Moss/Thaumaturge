package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public final class AdvancedFurnaceNozzle extends PassiveEssentiaSource {
    private static final int NOTHING_MOVED = 0;

    private final BlockEntityAdvancedAlchemicalFurnace furnace;
    private final Direction mouth;

    AdvancedFurnaceNozzle(BlockEntityAdvancedAlchemicalFurnace furnace, Direction outputFace) {
        this.furnace = furnace;
        this.mouth = outputFace;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return furnace.aspects().totalAmount();
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return furnace.randomEssentia();
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return takeEssentia(aspect, amount, face, false);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (simulate) {
            return previewTake(aspect, amount, face);
        }
        return canOutputTo(face) ? furnace.takeEssentia(aspect, amount) : NOTHING_MOVED;
    }

    private int previewTake(Holder<IAspect> aspect, int amount, Direction face) {
        boolean blocked = amount <= 0 || !canOutputTo(face);
        return blocked ? NOTHING_MOVED : Math.min(furnace.aspects().amountOf(aspect), amount);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        if (!furnace.isAssembled()) {
            return false;
        }
        return mouth.equals(face);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return canOutputTo(face);
    }
}
