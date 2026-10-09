package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
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
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return Optional.ofNullable(lowerFor(face)).filter(machine -> machine.canInputFrom(face)).map(machine -> machine.addEssentia(aspect, amount, face)).orElse(NOTHING);
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return Optional.ofNullable(lower()).map(machine -> machine.getSuctionType(face)).orElse(null);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return lowerFor(face) instanceof BlockEntityThaumatorium machine && machine.isConnectable(face);
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return Optional.ofNullable(lower()).map(machine -> machine.getSuctionAmount(face)).orElse(NOTHING);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return lowerFor(face) instanceof BlockEntityThaumatorium machine && machine.canInputFrom(face);
    }
}
