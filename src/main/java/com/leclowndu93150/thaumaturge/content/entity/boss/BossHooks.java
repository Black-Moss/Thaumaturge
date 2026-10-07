package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

final class BossHooks {
    private BossHooks() {}

    static void dropPearl(ServerLevel level, LivingEntity boss) {
        level.addFreshEntity(new EntitySpecialItem(level, boss.getX(), boss.getY() + boss.getBbHeight() / 2.0F, boss.getZ(), new ItemStack(TTItems.PRIMORDIAL_PEARL.get())));
    }
}
