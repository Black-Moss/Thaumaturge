package com.leclowndu93150.thaumcraft.content.research.theorycraft;

import com.leclowndu93150.thaumcraft.api.research.theorycraft.CardFactory;
import com.leclowndu93150.thaumcraft.api.research.theorycraft.ITheorycraftAid;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public final class AidBlock implements ITheorycraftAid {
    private static final int SCAN_RADIUS_XZ = 4;
    private static final int SCAN_RADIUS_Y = 1;

    private final List<ResourceKey<CardFactory>> cards;
    private final Supplier<List<Block>> blocks;
    private final Supplier<ItemStack> display;

    public AidBlock(Supplier<List<Block>> blocks, Supplier<ItemStack> display, List<ResourceKey<CardFactory>> cards) {
        this.blocks = blocks;
        this.display = display;
        this.cards = cards;
    }

    @Override
    public List<ResourceKey<CardFactory>> cards() {
        return cards;
    }

    @Override
    public boolean matches(LevelAccessor level, BlockPos tablePos) {
        List<Block> targets = blocks.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -SCAN_RADIUS_Y; y <= SCAN_RADIUS_Y; y++) {
            for (int x = -SCAN_RADIUS_XZ; x <= SCAN_RADIUS_XZ; x++) {
                for (int z = -SCAN_RADIUS_XZ; z <= SCAN_RADIUS_XZ; z++) {
                    cursor.setWithOffset(tablePos, x, y, z);
                    if (!level.hasChunkAt(cursor)) continue;
                    if (targets.contains(level.getBlockState(cursor).getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack displayStack() {
        return display.get();
    }

    @Override
    public Component description() {
        return display.get().getHoverName();
    }
}
