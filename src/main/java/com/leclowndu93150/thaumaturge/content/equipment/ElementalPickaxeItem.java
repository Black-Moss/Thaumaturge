package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ElementalPickaxeItem extends Item {
    private static final int IGNITE_SECONDS = 2;

    public ElementalPickaxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level() instanceof ServerLevel level
                && (!(entity instanceof Player) || level.isPvpAllowed())) {
            entity.igniteForSeconds(IGNITE_SECONDS);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
