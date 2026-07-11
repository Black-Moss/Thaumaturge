package com.leclowndu93150.thaumcraft.api.research.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching one exact block state in the world.
 *
 * @since 1.0.0
 */
public class ScanBlockState implements IScanThing {
    private final Identifier research;
    private final BlockState blockState;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param blockState the exact state this subject matches
     */
    public ScanBlockState(Identifier research, BlockState blockState) {
        this(research, blockState, false);
    }

    /**
     * Creates the subject, optionally registering the block's item form as a companion
     * {@link ScanItem}.
     *
     * @param research the research key granted on scan
     * @param blockState the exact state this subject matches
     * @param item whether to also match the block's item form
     */
    public ScanBlockState(Identifier research, BlockState blockState, boolean item) {
        this.research = research;
        this.blockState = blockState;
        if (item) {
            if (blockState.getBlock().asItem() != Items.AIR) {
                ScanningManager.addScannableThing(new ScanItem(research, blockState.getBlock()));
            }
        }
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return target instanceof BlockPos pos && player.level().getBlockState(pos) == blockState;
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
