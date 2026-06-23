package com.leclowndu93150.thaumcraft.api.items;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Static accessor for detecting goggles, revealers, and vis-discount gear worn by an entity.
 *
 * <p>Side-agnostic: the head slot lookup uses {@link LivingEntity#getItemBySlot(EquipmentSlot)},
 * which works identically on client and server.
 *
 * @since 1.0.0
 */
public final class GogglesAccess {
    private GogglesAccess() {}

    /**
     * Returns whether the entity wears a head-slot stack implementing {@link IGoggles} with
     * popups enabled.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when the wearer should see in-game popups and HUD overlays
     */
    public static boolean wearsGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof IGoggles g) {
            return g.showIngamePopups(head, entity);
        }
        return false;
    }

    /**
     * Returns whether the entity wears or holds a stack implementing {@link IRevealer} that
     * reveals aura nodes.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when nodes should be revealed for the entity
     */
    public static boolean revealsNodes(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty() && head.getItem() instanceof IRevealer r && r.showNodes(head, entity)) {
            return true;
        }
        ItemStack main = entity.getMainHandItem();
        if (!main.isEmpty() && main.getItem() instanceof IRevealer r && r.showNodes(main, entity)) {
            return true;
        }
        ItemStack off = entity.getOffhandItem();
        if (!off.isEmpty() && off.getItem() instanceof IRevealer r && r.showNodes(off, entity)) {
            return true;
        }
        return false;
    }

    /**
     * Sums the vis discount percentages contributed by every piece of armor on the player.
     * Each piece implementing {@link IVisDiscountGear} contributes its reported value.
     *
     * @param player the player to query; null returns zero
     * @return the total discount in whole percent units, never negative
     */
    public static int totalVisDiscount(Player player) {
        if (player == null) {
            return 0;
        }
        int total = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IVisDiscountGear g) {
                int contribution = g.getVisDiscount(stack, player);
                if (contribution > 0) {
                    total += contribution;
                }
            }
        }
        return total;
    }
}
