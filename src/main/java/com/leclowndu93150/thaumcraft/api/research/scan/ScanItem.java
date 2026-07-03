package com.leclowndu93150.thaumcraft.api.research.scan;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching a specific item, held as a stack or lying as an item entity.
 *
 * @since 1.0.0
 */
public class ScanItem implements IScanThing {
    private final Identifier research;
    private final ItemStack stack;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param stack the stack whose item this subject matches
     */
    public ScanItem(Identifier research, ItemStack stack) {
        this.research = research;
        this.stack = stack;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        ItemStack is = ItemStack.EMPTY;
        if (target instanceof ItemStack targetStack) {
            is = targetStack;
        } else if (target instanceof ItemEntity itemEntity) {
            is = itemEntity.getItem();
        }
        return !is.isEmpty() && ItemStack.isSameItem(is, stack);
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
