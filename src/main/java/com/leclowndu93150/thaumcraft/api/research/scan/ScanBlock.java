package com.leclowndu93150.thaumcraft.api.research.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching one of a set of blocks in the world. Each block's item form is
 * automatically registered as a companion {@link ScanItem} so the held or dropped item grants
 * the same key.
 *
 * @since 1.0.0
 */
public class ScanBlock implements IScanThing {
    private final Identifier research;
    private final Block[] blocks;

    /**
     * Creates the subject and registers item companions for every block.
     *
     * @param research the research key granted on scan
     * @param blocks the blocks this subject matches
     */
    public ScanBlock(Identifier research, Block... blocks) {
        this.research = research;
        this.blocks = blocks;
        for (Block block : blocks) {
            ItemStack stack = new ItemStack(block);
            if (!stack.isEmpty()) {
                ScanningManager.addScannableThing(new ScanItem(research, stack));
            }
        }
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target instanceof BlockPos pos) {
            Block found = player.level().getBlockState(pos).getBlock();
            for (Block block : blocks) {
                if (found == block) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
