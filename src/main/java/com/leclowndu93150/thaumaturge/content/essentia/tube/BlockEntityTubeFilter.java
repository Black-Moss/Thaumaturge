package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour.FilterTubeBehaviour;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeFilter extends BlockEntityTube implements IAspectQuery {
    private static final String ASPECT_FILTER_KEY = "AspectFilter";
    private static final int QUERY_AMOUNT = 1;
    private static final int RERENDER_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE;

    private final FilterTubeBehaviour filter;

    public BlockEntityTubeFilter(BlockPos pos, BlockState state) {
        this(pos, state, new FilterTubeBehaviour());
    }

    private BlockEntityTubeFilter(BlockPos pos, BlockState state, FilterTubeBehaviour filter) {
        super(TTBlockEntities.TUBE_FILTER.get(), pos, state, filter);
        this.filter = filter;
    }

    public @Nullable ResourceKey<IAspect> aspectFilter() {
        return filter.suctionFilter();
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> aspect) {
        filter.setAspectFilter(aspect);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), RERENDER_FLAGS);
        }
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        ResourceKey<IAspect> configured = filter.suctionFilter();
        return configured != null ? resolve(configured) : super.getSuctionType(face);
    }

    @Override
    public AspectList queryAspects() {
        Holder<IAspect> resolved = resolve(filter.suctionFilter());
        return resolved == null ? AspectList.EMPTY : AspectList.of(new AspectInstance(resolved, QUERY_AMOUNT));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(ASPECT_FILTER_KEY, ASPECT_KEY_CODEC, filter.suctionFilter());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ResourceKey<IAspect> previous = filter.suctionFilter();
        filter.setAspectFilter(input.read(ASPECT_FILTER_KEY, ASPECT_KEY_CODEC).orElse(null));
        Level current = level;
        if (current != null && current.isClientSide() && !Objects.equals(previous, filter.suctionFilter())) {
            current.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
