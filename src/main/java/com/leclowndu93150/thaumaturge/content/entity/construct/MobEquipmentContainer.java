package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MobEquipmentContainer implements Container {
    private final @Nullable Mob mob;

    public MobEquipmentContainer(@Nullable Mob mob) {
        this.mob = mob;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return mob == null ? ItemStack.EMPTY : mob.getMainHandItem();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack held = getItem(slot);
        if (held.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = held.split(amount);
        if (held.isEmpty() && mob != null) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack held = getItem(slot);
        if (mob != null) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return held;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (mob != null) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return mob != null && mob.isAlive();
    }

    @Override
    public void clearContent() {
        if (mob != null) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }
}
