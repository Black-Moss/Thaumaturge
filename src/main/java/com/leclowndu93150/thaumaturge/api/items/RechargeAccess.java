package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Static accessor for reading and writing the vis charge stored on {@link IRechargable} stacks.
 *
 * <p>Charge is held in the {@code thaumaturge:charge} data component. All mutators operate on the
 * given stack in place. Draining from the aura is server-authoritative and routes through
 * {@link AuraHelper}; call the world-aware recharge methods only on the logical server.
 *
 * @since 1.0.0
 */
public final class RechargeAccess {
    private RechargeAccess() {}

    /**
     * Recharges a stack by draining vis from the aura at a position.
     *
     * <p>No charge is transferred when the holder is in an aura-preserving state, when the stack
     * is already full, or when the aura has no vis to give.
     *
     * @param level the world to drain from
     * @param stack the stack to recharge
     * @param pos the position whose aura chunk is drained
     * @param player the player triggering the recharge, or {@code null}
     * @param amount the requested amount, clamped to remaining capacity
     * @return the amount actually added
     */
    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, Player player, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0.0F;
        }
        if (player != null && AuraHelper.shouldPreserveAura(level, player, pos)) {
            return 0.0F;
        }
        int requested = Math.min(amount, rechargable.getMaxCharge(stack, player) - getCharge(stack));
        int drained = (int) AuraHelper.drainVis(level, pos, requested, false);
        if (drained > 0) {
            addCharge(stack, player, drained);
            return drained;
        }
        return 0.0F;
    }

    /**
     * Recharges a stack without touching the aura, up to remaining capacity.
     *
     * @param stack the stack to recharge
     * @param holder the holder, or {@code null}
     * @param amount the requested amount, clamped to remaining capacity
     * @return the amount actually added
     */
    public static float rechargeItemBlindly(ItemStack stack, LivingEntity holder, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0.0F;
        }
        int requested = Math.min(amount, rechargable.getMaxCharge(stack, holder) - getCharge(stack));
        if (requested > 0) {
            addCharge(stack, holder, requested);
        }
        return requested;
    }

    /**
     * Returns the current charge of a stack.
     *
     * @param stack the stack under query
     * @return the charge, or {@code -1} when the stack is not an {@link IRechargable}
     */
    public static int getCharge(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable)) {
            return -1;
        }
        return stack.getOrDefault(TCDataComponents.CHARGE.get(), 0);
    }

    /**
     * Returns the charge of a stack as a fraction of its capacity.
     *
     * @param stack the stack under query
     * @param holder the holder, or {@code null}
     * @return the fraction in {@code [0, 1]}, or {@code -1} when the stack is not an {@link IRechargable}
     */
    public static float getChargePercentage(ItemStack stack, LivingEntity holder) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return -1.0F;
        }
        float max = rechargable.getMaxCharge(stack, holder);
        return max <= 0 ? 0.0F : getCharge(stack) / max;
    }

    /**
     * Consumes charge from a stack when it holds at least the requested amount.
     *
     * @param stack the stack to drain
     * @param holder the holder, or {@code null}
     * @param amount the amount to consume
     * @return {@code true} when the charge was available and consumed
     */
    public static boolean consumeCharge(ItemStack stack, LivingEntity holder, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable)) {
            return false;
        }
        int charge = getCharge(stack);
        if (charge >= amount) {
            stack.set(TCDataComponents.CHARGE.get(), charge - amount);
            return true;
        }
        return false;
    }

    private static void addCharge(ItemStack stack, LivingEntity holder, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return;
        }
        int total = Math.min(rechargable.getMaxCharge(stack, holder), amount + getCharge(stack));
        stack.set(TCDataComponents.CHARGE.get(), total);
    }
}
