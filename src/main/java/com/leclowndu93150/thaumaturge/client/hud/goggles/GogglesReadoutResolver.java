package com.leclowndu93150.thaumaturge.client.hud.goggles;

import com.leclowndu93150.thaumaturge.api.items.IGogglesReadout;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class GogglesReadoutResolver {
    private GogglesReadoutResolver() {}

    public static Optional<GogglesReadout> resolve(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IGogglesReadout provider) {
            return read(provider, StackOrder.TOP_FIRST);
        }
        if (level.getBlockState(pos).getBlock() instanceof IGogglesReadout provider) {
            return read(provider, StackOrder.BOTTOM_FIRST);
        }
        return Optional.empty();
    }

    private static Optional<GogglesReadout> read(IGogglesReadout provider, StackOrder order) {
        List<Component> lines = provider.readout();
        if (lines.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new GogglesReadout(lines, order, provider.readoutAnchor()));
    }
}
