package com.leclowndu93150.thaumaturge.content.spell.casting;

import com.leclowndu93150.thaumaturge.api.casters.FocusCaster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CasterHands {
    private CasterHands() {}

    public static ItemStack held(@Nullable LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        if (entity.getMainHandItem().getItem() instanceof FocusCaster) {
            return entity.getMainHandItem();
        }
        if (entity.getOffhandItem().getItem() instanceof FocusCaster) {
            return entity.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }
}
