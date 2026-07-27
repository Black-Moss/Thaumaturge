package com.leclowndu93150.thaumaturge.api.recipe;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface DustTrigger extends Recipe<DustTriggerInput>, ResearchGated{

    default List<BlockPos> sparkle(Level level, Player player, BlockPos pos, DustTriggerPlacement placement) {
        return List.of(pos);
    }

    default @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) {
        return null;
    }

    default boolean isMultiblock() {
        return false;
    }

    void execute(DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace);
}
