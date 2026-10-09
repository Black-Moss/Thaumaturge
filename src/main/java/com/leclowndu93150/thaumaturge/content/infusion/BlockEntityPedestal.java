package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityPedestal extends AbstractSyncedBlockEntity implements Clearable {
    private static final String ITEM_KEY = "Item";
    private static final int MITIGATOR_SEARCH_DEPTH = 32;
    private static final double DROP_CENTER_OFFSET = 0.5;
    private static final double DROP_HEIGHT = 1.0;

    private ItemStack item = ItemStack.EMPTY;

    public BlockEntityPedestal(BlockPos pos, BlockState state) {
        this(TTBlockEntities.PEDESTAL.get(), pos, state);
    }

    protected BlockEntityPedestal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack stack) {
        item = stack;
        setChangedAndSync();
    }

    @Override
    public void clearContent() {
        setItem(ItemStack.EMPTY);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && !item.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + DROP_CENTER_OFFSET, pos.getY() + DROP_HEIGHT, pos.getZ() + DROP_CENTER_OFFSET, item));
        }
        item = ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!item.isEmpty()) {
            output.store(ITEM_KEY, ItemStack.CODEC, item);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        item = input.read(ITEM_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public @Nullable BlockPos findInstabilityMitigator() {
        Level world = level;
        BlockState state = getBlockState();
        int charge = state.hasProperty(BlockPedestal.CHARGE) ? state.getValue(BlockPedestal.CHARGE) : 0;
        if (world == null || charge <= 0) {
            return null;
        }
        Set<BlockPos> visited = new HashSet<>();
        visited.add(worldPosition);
        List<BlockPos> frontier = List.of(worldPosition);
        for (int depth = 0; depth < MITIGATOR_SEARCH_DEPTH && !frontier.isEmpty(); depth++) {
            List<BlockPos> next = new ArrayList<>();
            for (BlockPos node : frontier) {
                int nodeCharge = BlockInlay.chargeAt(world, node);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbour = node.relative(direction);
                    if (!world.hasChunkAt(neighbour)) {
                        continue;
                    }
                    if (BlockInlay.sourceStrengthAt(world, neighbour) >= BlockInlay.MITIGATOR_MIN_ENERGY) {
                        return neighbour;
                    }
                    if (BlockInlay.chargeAt(world, neighbour) > nodeCharge && visited.add(neighbour)) {
                        next.add(neighbour);
                    }
                }
            }
            frontier = next;
        }
        return null;
    }
}
