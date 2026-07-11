package com.leclowndu93150.thaumcraft.api.research.scan;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching a specific item, held as a stack or lying as an item entity.
 *
 * @since 1.0.0
 */
public class ScanItem implements IScanThing {
    private final Identifier research;
    private final Item item;

    /**
     * Creates the subject. Holds the {@link Item} rather than a stack so instances can be
     * created before item components are bound.
     *
     * @param research the research key granted on scan
     * @param item the item this subject matches
     */
    public ScanItem(Identifier research, ItemLike item) {
        this.research = research;
        this.item = item.asItem();
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        ItemStack is = ItemStack.EMPTY;
        if (target instanceof ItemStack targetStack) {
            is = targetStack;
        } else if (target instanceof ItemEntity itemEntity) {
            is = itemEntity.getItem();
        }
        return !is.isEmpty() && is.is(item);
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
