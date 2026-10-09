package com.leclowndu93150.thaumaturge.content.research.table;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

record TableBinding(@Nullable BlockEntityResearchTable table, ItemStacksResourceHandler handler, BlockPos origin, ContainerLevelAccess access) {
    static TableBinding of(@Nullable BlockEntityResearchTable table) {
        if (table == null) {
            return new TableBinding(null, new ItemStacksResourceHandler(BlockEntityResearchTable.SLOT_COUNT), BlockPos.ZERO, ContainerLevelAccess.NULL);
        }
        return new TableBinding(table, table.items(), table.getBlockPos(), ContainerLevelAccess.create(table.getLevel(), table.getBlockPos()));
    }

    static TableBinding at(Level level, BlockPos pos) {
        return of(level.getBlockEntity(pos) instanceof BlockEntityResearchTable found ? found : null);
    }

    ItemStack stackAt(int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }
}
