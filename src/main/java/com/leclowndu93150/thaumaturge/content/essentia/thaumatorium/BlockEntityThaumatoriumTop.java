package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityThaumatoriumTop extends BlockEntity implements SinkOnlyEssentiaFace {
    public BlockEntityThaumatoriumTop(BlockPos pos, BlockState state) {
        super(TTBlockEntities.THAUMATORIUM_TOP.get(), pos, state);
    }

    private @Nullable BlockEntityThaumatorium lower() {
        if (level != null && level.getBlockEntity(worldPosition.below()) instanceof BlockEntityThaumatorium machine) {
            return machine;
        }
        return null;
    }

    private @Nullable BlockEntityThaumatorium lowerFor(@Nullable Direction face) {
        return face == Direction.DOWN ? null : lower();
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return Optional.ofNullable(lowerFor(face)).filter(machine -> machine.receivesOn(face)).map(machine -> machine.insert(aspect, amount, face, simulate)).orElse(NOTHING);
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        return Optional.ofNullable(lowerFor(face)).filter(machine -> machine.receivesOn(face)).map(machine -> machine.roomFor(aspect, face)).orElse(NOTHING);
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        BlockEntityThaumatorium machine = lower();
        return machine == null ? EssentiaPull.NONE : machine.pull(face);
    }

    @Override
    public boolean connectsOn(Direction face) {
        return lowerFor(face) instanceof BlockEntityThaumatorium machine && machine.connectsOn(face);
    }

    @Override
    public boolean receivesOn(Direction face) {
        return lowerFor(face) instanceof BlockEntityThaumatorium machine && machine.receivesOn(face);
    }
}
