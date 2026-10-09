package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ElementalPickaxeItem extends Item {
    private static final float IGNITE_SECONDS = 2.0F;

    public ElementalPickaxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        boolean protectedVictim = target instanceof Player victim && !player.canHarmPlayer(victim);
        if (protectedVictim || player.level().isClientSide()) {
            return super.onLeftClickEntity(stack, player, target);
        }
        target.igniteForSeconds(IGNITE_SECONDS);
        return super.onLeftClickEntity(stack, player, target);
    }
}
