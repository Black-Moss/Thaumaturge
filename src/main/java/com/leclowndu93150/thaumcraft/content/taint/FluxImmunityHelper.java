package com.leclowndu93150.thaumcraft.content.taint;

import com.leclowndu93150.thaumcraft.api.entity.IFluxImmune;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FluxImmunityHelper {
    private FluxImmunityHelper() {}

    public static boolean isImmune(LivingEntity living) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = living.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IFluxImmune) {
                return true;
            }
        }
        return false;
    }
}
