package com.leclowndu93150.thaumaturge.content.device.grate;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class BlockEntityItemGrate extends BlockEntity {
    private static final double EJECT_CENTER_OFFSET = 0.5;
    private static final double EJECT_Y_OFFSET = -0.15;

    private final GrateInventory inventory = new GrateInventory();

    public BlockEntityItemGrate(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ITEM_GRATE.get(), pos, state);
    }

    public ItemStacksResourceHandler inventory() {
        return inventory;
    }

    public void eject() {
        if (level == null || level.isClientSide() || !getBlockState().getValue(BlockItemGrate.OPEN)) {
            return;
        }
        ItemResource resource = inventory.getResource(0);
        int amount = inventory.getAmountAsInt(0);
        if (resource.isEmpty() || amount <= 0) {
            return;
        }
        ItemStack stack = resource.toStack(amount);
        inventory.set(0, ItemResource.EMPTY, 0);
        Containers.dropItemStack(level, worldPosition.getX() + EJECT_CENTER_OFFSET, worldPosition.getY() + EJECT_Y_OFFSET, worldPosition.getZ() + EJECT_CENTER_OFFSET, stack);
        setChanged();
    }

    private final class GrateInventory extends ItemStacksResourceHandler {
        GrateInventory() {
            super(1);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            eject();
        }
    }
}
